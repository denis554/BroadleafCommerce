package org.broadleafcommerce.common.persistence.transaction;

import org.broadleafcommerce.common.event.BroadleafApplicationListener;
import org.broadleafcommerce.common.exception.ExceptionHelper;
import org.broadleafcommerce.common.logging.SupportLogManager;
import org.broadleafcommerce.common.logging.SupportLogger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.thymeleaf.util.ArrayUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * Responsible for most of the work related to monitoring and logging transaction state. This class is notified via a
 * {@link LifecycleAwareJpaTransactionManager} when a key {@link TransactionLifecycleEvent} occurs. Based on this information,
 * this monitor attempts to detect transaction fault states and report them as part of SUPPORT level logging ({@link SupportLogger}).
 * </p>
 * Currently, the monitor attempts to recognize the following fault states:
 * <ul>
 *     <li>TRANSACTIONMONITOR(1) - Leaked Resource: The transaction thread is not considered stuck, but no queries have been issued againsted the
 *     tracked EntityManager in {@link #loggingReportingLagThreshold} and the transaction info has been alive for {@link #loggingThreshold}.
 *     This could indicate the thread has moved on and the transaction was not correctly finalized.</li>
 *
 *     <li>TRANSACTIONMONITOR(2) - Long Running Transaction: The transaction thread is not considered stuck, but the transaction info has been alive
 *     for {@link #loggingThreshold}. This is not necessarily a fault, but may warrant review. Long running or stuck
 *     transactions can account for long resource lock times. Note, this case can later become a stuck thread if the {@link #stuckThreshold}
 *     has not elapsed.</li>
 *
 *     <li>TRANSACTIONMONITOR(3) - Exception During Finalization: The transaction is attempting to finalize normally, but has
 *     experienced an exception during that finalization attempt. This could indicate a problem communicating with the backing
 *     database and may result in orphaned resources in the data tier.</li>
 *
 *     <li>TRANSACTIONMONITOR(4) - Stuck Thread: The transaction thread is considered stuck with a transaction info alive
 *     for {@link #loggingThreshold} and no change in thread stack for {@link #stuckThreshold}. Long running or stuck transactions
 *     can account for long resource lock times.</li>
 *
 *     <li>TRANSACTIONMONITOR(5) - Alive At Shutdown: The transaction info is considered active at the time of container
 *     shutdown. This is not necessarily a fault, but may warrant review. Items at shutdown may simply be waiting for
 *     final closure. However, subsequent system kill calls (if applicable) could prematurely exit these connections and
 *     they may be interesting for review.</li>
 * </ul>
 * </p>
 * This monitor is enabled or disabled per {@link LifecycleAwareJpaTransactionManager} via {@link LifecycleAwareJpaTransactionManager#isEnabled()}.
 * If no enabled transaction manager is found, the monitor is fully disabled.
 * </p>
 * The {@link #loggingThreshold} variable can be controlled via the 'log.transaction.lifecycle.logging.threshold.millis' property.
 * The default value is 600000.
 * </p>
 * The {@link #stuckThreshold} variable can be controlled via the 'log.transaction.lifecycle.stuck.threshold.millis' property.
 * The default values is 300000.
 * </p>
 * The {@link #loggingPollingResolution} variable can be controlled via the 'log.transaction.lifecycle.logging.polling.resolution.millis' property.
 * The default value is 30000.
 * </p>
 * The {@link #loggingReportingLagThreshold} variable can be controlled via the 'log.transaction.lifecycle.reporting.lag.threshold.millis' property.
 * The default value is 300000.
 * </p>
 * The {@link #countMax} variable can be controlled via the 'log.transaction.lifecycle.info.count.max' property. The default value is 5000.
 * </p>
 * The {@link #useCompression} variable can be controlled via the 'log.transaction.lifecycle.use.compression' property. The default value is true.
 * </p>
 * This monitor is intended for temporary usage as part of transaction related debugging efforts. From both a heap utilization
 * and performance standpoint, this monitor is suitable for production with default settings. Performance impacts should be minor and will generally
 * be related to creation of the intial call stack, compression of that call stack and subsequent compression of sql statements.
 * Compression can be turned off via the 'log.transaction.lifecycle.use.compression' for a minor performance benefit at the cost
 * of additional heap usage. Heap usage can be capped via the 'log.transaction.lifecycle.info.count.max' property, but if the max
 * happens to be reached, new transactions will not be monitored. This is more a safety net feature than anything and it's not
 * anticipated that the default max count will be reached during normal usage. Set 'log.transaction.lifecycle.info.count.max' to
 * -1 to uncap growth.
 *
 * @author Jeff Fischer
 */
public class TransactionLifecycleMonitor implements BroadleafApplicationListener<TransactionLifecycleEvent>, ApplicationContextAware, SmartLifecycle, SqlStatementLoggable {

    private static SupportLogger logger = SupportLogManager.getLogger("TransactionLogging", TransactionLifecycleMonitor.class);
    private static ApplicationContext context = null;
    private static TransactionLifecycleMonitor instance = null;

    public static TransactionLifecycleMonitor getInstance() {
        return instance;
    }

    @Autowired(required = false)
    protected List<LifecycleAwareJpaTransactionManager> transactionManagers = null;

    @Autowired(required = false)
    protected List<TransactionInfoCustomModifier> modifiers = null;

    //10 minutes
    @Value("${log.transaction.lifecycle.logging.threshold.millis:600000}")
    protected long loggingThreshold = 600000L;

    //5 minutes
    @Value("${log.transaction.lifecycle.stuck.threshold.millis:300000}")
    protected long stuckThreshold = 300000L;

    //30 seconds
    @Value("${log.transaction.lifecycle.logging.polling.resolution.millis:30000}")
    protected long loggingPollingResolution = 30000L;

    //5 minutes
    @Value("${log.transaction.lifecycle.reporting.lag.threshold.millis:300000}")
    protected long loggingReportingLagThreshold = 300000L;

    @Value("${log.transaction.lifecycle.info.count.max:5000}")
    protected int countMax = 5000;

    @Value("${log.transaction.lifecycle.use.compression:true}")
    protected boolean useCompression = true;

    protected Map<Integer, TransactionInfo> infos = new ConcurrentHashMap<Integer, TransactionInfo>();
    protected boolean isStarted = false;
    protected boolean enabled = false;
    protected Timer timer = new Timer("TransactionLifecycleMonitorThread", true);

    @PostConstruct
    public synchronized void init() {
        if (!isStarted) {
            if (instance == null) {
                instance = (TransactionLifecycleMonitor) context.getBean("blTransactionLifecycleMonitor");
            }
            if (isAtLeastOneTransactionManagerEnabled()) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        groomInProgressTransactionInfos();
                    }
                }, loggingPollingResolution, loggingPollingResolution);
                enabled = true;
            }
            isStarted = true;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void start() {
        //do nothing - we start earlier in init();
    }

    @Override
    public void stop() {
        if (enabled) {
            timer.cancel();
            if (!infos.isEmpty()) {
                logger.support("Logging any in-progress TransactionInfo instances at the time of container shutdown");
                Long currentTime = System.currentTimeMillis();
                for (Map.Entry<Integer, TransactionInfo> entry : infos.entrySet()) {
                    TransactionInfo info = entry.getValue();
                    logger.support(String.format("TRANSACTIONMONITOR(5) - This transaction was detected as in-progress at the time " +
                        "of shutdown. The TransactionInfo has been alive for %s milliseconds. Logging TransactionInfo: \n%s",
                        currentTime - info.getStartTime(), info.toString()));
                }
            }
        }
    }

    @Override
    public boolean isRunning() {
        return isStarted;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAsynchronous() {
        return false;
    }

    @Override
    public void onApplicationEvent(TransactionLifecycleEvent event) {
        if (enabled) {
            switch (event.getLifecycle()) {
                case BEGIN: {
                    EntityManager em = getEntityManagerFromTransactionObject(event.getParams()[0]);
                    if (em != null) {
                        if (countMax == -1 || infos.size() <= countMax) {
                            TransactionInfo info = new TransactionInfo(em, (TransactionDefinition) event.getParams()[1], useCompression);
                            if (modifiers != null) {
                                for (TransactionInfoCustomModifier modifier : modifiers) {
                                    modifier.modify(info);
                                }
                            }
                            infos.put(em.hashCode(), info);
                        } else {
                            logger.debug(String.format("Not monitoring new transaction. Current monitored transaction count exceeds maximum: %s", countMax));
                        }
                    }
                    break;
                }
                case COMMIT: {
                    finalizeTransaction(event);
                    break;
                }
                case ROLLBACK: {
                    finalizeTransaction(event);
                    break;
                }
                case GET_TRANSACTION:
                    //do nothing
                    break;
                default:
                    throw new UnsupportedOperationException(event.getLifecycle().toString() + "not supported");
            }
        }
    }

    @Override
    public void log(String statement) {
        if (enabled) {
            TransactionInfo info = getCurrentTransactionInfo();
            if (info != null) {
                info.logStatement(statement);
            }
        }
    }

    protected void groomInProgressTransactionInfos() {
        List<Integer> infosToRemove = new ArrayList<Integer>();
        try {
            Map<Integer, TransactionInfo> shallow = new HashMap<Integer, TransactionInfo>();
            shallow.putAll(infos);
            for (Map.Entry<Integer, TransactionInfo> entry : shallow.entrySet()) {
                long currentTime = System.currentTimeMillis();
                TransactionInfo info = entry.getValue();
                Thread thread = info.getThread();
                StackTraceElement[] elements = compileThreadInformation(currentTime, info, thread);
                detectExpiry(infosToRemove, entry.getKey(), currentTime, info, elements);
                detectLeakage(infosToRemove, entry.getKey(), currentTime, info);
            }
        } finally {
            for (Integer key : infosToRemove) {
                infos.remove(key);
            }
        }
    }

    protected void detectLeakage(List<Integer> infosToRemove, Integer key, long currentTime, TransactionInfo info) {
        boolean isPossiblyLeaked = currentTime - info.getLastLogTime() >= loggingReportingLagThreshold;
        if (isPossiblyLeaked) {
            try {
                info.setFaultStateDetected(true);
                logger.support(String.format("TRANSACTIONMONITOR(1) - The thread associated with the tested TransactionInfo is not " +
                    "considered stuck, but the TransactionInfo has been alive for %s milliseconds and a SQL " +
                    "statement has not been reported against the tracked EntityManager in %s milliseconds. " +
                    "This could indicate the thread has moved on and the transaction was not properly finalized. " +
                    "Logging TransactionInfo: \n%s",
                    currentTime - info.getStartTime(), currentTime - info.getLastLogTime(), info.toString()));
            } finally {
                infosToRemove.add(key);
            }
        }
    }

    protected void detectExpiry(List<Integer> infosToRemove, Integer key, long currentTime, TransactionInfo info, StackTraceElement[] elements) {
        boolean isExpired = currentTime - info.getStartTime() >= loggingThreshold;
        if (isExpired) {
            if (info.getStuckThreadStartTime() != null) {
                boolean isStuck = currentTime - info.getStuckThreadStartTime() >= stuckThreshold;
                if (isStuck) {
                    try {
                        String currentStack = "UNKNOWN";
                        if (!ArrayUtils.isEmpty(elements)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Stack\n");
                            for (StackTraceElement element : elements) {
                                sb.append("\tat ");
                                sb.append(element);
                                sb.append("\n");
                            }
                            currentStack = sb.toString();
                        }
                        logger.support(String.format("TRANSACTIONMONITOR(4) - The thread associated with the tested TransactionInfo may be " +
                            "stuck. The TransactionInfo has been alive for %s milliseconds and the associated thread stack " +
                            "has not changed in %s milliseconds. Logging TransactionInfo and current stack: \n%s currentStack=\'%s\'",
                            currentTime - info.getStartTime(), currentTime - info.getStuckThreadStartTime(), info.toString(), currentStack));
                    } finally {
                        infosToRemove.add(key);
                    }
                }
            } else {
                if (!info.getFaultStateDetected()){
                    logger.support(String.format("TRANSACTIONMONITOR(2) - The thread associated with the tested TransactionInfo is not " +
                        "considered stuck yet, but the TransactionInfo has been alive for %s milliseconds. " +
                        "This could indicate a overly long transaction time. Logging TransactionInfo: \n%s",
                            currentTime - info.getStartTime(), info.toString()));
                }
            }
            info.setFaultStateDetected(true);
        }
    }

    protected StackTraceElement[] compileThreadInformation(long currentTime, TransactionInfo info, Thread thread) {
        StackTraceElement[] elements = null;
        if (thread != null && thread.isAlive()) {
            elements = thread.getStackTrace();
            if (!ArrayUtils.isEmpty(elements)) {
                StackTraceElement top = elements[0];
                String currentStackElement = top.toString();
                //don't detect a waiting thread as stuck - could be a parked thread in a container thread pool, for example
                boolean isWaiting = thread.getState() == Thread.State.WAITING || thread.getState() == Thread.State.TIMED_WAITING;
                if (info.getCurrentStackElement() != null && info.getCurrentStackElement().equals(currentStackElement) && !isWaiting) {
                    if (info.getStuckThreadStartTime() == null) {
                        info.setStuckThreadStartTime(currentTime);
                    }
                } else {
                    if (info.getStuckThreadStartTime() != null) {
                        info.setStuckThreadStartTime(null);
                    }
                    info.setCurrentStackElement(currentStackElement);
                }
            }
        }
        return elements;
    }

    protected void finalizeTransaction(TransactionLifecycleEvent event) {
        String finalizationType = event.getLifecycle().name();
        DefaultTransactionStatus status = (DefaultTransactionStatus) event.getParams()[0];
        EntityManager em = getEntityManagerFromTransactionObject(status.getTransaction());
        if (em != null) {
            Integer hashcode = em.hashCode();
            TransactionInfo info = infos.get(hashcode);
            if (info != null) {
                try {
                    Throwable finalizationException = event.getException();
                    if (finalizationException != null) {
                        info.setFaultStateDetected(true);
                        //An exception took place during finalization. Log our info to support for review.
                        StringWriter sw = new StringWriter();
                        finalizationException.printStackTrace(new PrintWriter(sw));
                        logger.support(String.format("TRANSACTIONMONITOR(3) - Exception during "+finalizationType+" finalization. Logging " +
                            "TransactionInfo and finalization exception: \n%s finalizationStack=\'%s\'", info.toString(), sw.toString()));
                    }
                } finally {
                    infos.remove(hashcode);
                }
            }
        }
    }

    protected EntityManager getEntityManagerFromTransactionObject(Object transactionObject) {
        EntityManager response;
        try {
            Class<?> jpaTransactionObject = transactionObject.getClass();
            Method getEntityManagerHolder = jpaTransactionObject.getMethod("getEntityManagerHolder");
            getEntityManagerHolder.setAccessible(true);
            EntityManagerHolder emh = (EntityManagerHolder) getEntityManagerHolder.invoke(transactionObject);
            response = emh.getEntityManager();
        } catch (Exception e) {
            throw ExceptionHelper.refineException(e);
        }
        return response;
    }

    protected TransactionInfo getCurrentTransactionInfo() {
        TransactionInfo response = null;
        if (transactionManagers != null) {
            for (LifecycleAwareJpaTransactionManager transactionManager : transactionManagers) {
                if (transactionManager.isEnabled()) {
                    EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(transactionManager.getEntityManagerFactory());
                    if (emHolder != null && emHolder.isOpen() && emHolder.isSynchronizedWithTransaction()) {
                        response = infos.get(emHolder.getEntityManager().hashCode());
                        if (response != null) {
                            break;
                        }
                    }
                }
            }
        }
        return response;
    }

    protected boolean isAtLeastOneTransactionManagerEnabled() {
        boolean enabled = false;
        if (transactionManagers != null) {
            for (LifecycleAwareJpaTransactionManager transactionManager : transactionManagers) {
                if (transactionManager.isEnabled()) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }

}
