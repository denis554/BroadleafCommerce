/*
 * #%L
 * BroadleafCommerce Framework
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.workflow.state.ActivityStateManager;
import org.broadleafcommerce.core.workflow.state.ActivityStateManagerImpl;
import org.broadleafcommerce.core.workflow.state.RollbackStateLocal;

import java.util.List;

public class SequenceProcessor extends BaseProcessor {

    private static final Log LOG = LogFactory.getLog(SequenceProcessor.class);

    private ProcessContextFactory<Object, Object> processContextFactory;

    @Override
    public boolean supports(Activity<? extends ProcessContext<?>> activity) {
        return true;
    }

    @Override
    public ProcessContext<?> doActivities() throws WorkflowException {
        return doActivities(null);
    }

    @Override
    public ProcessContext<?> doActivities(Object seedData) throws WorkflowException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(getBeanName() + " processor is running..");
        }
        ActivityStateManager activityStateManager = ((ActivityStateManager) getBeanFactory().getBean("blActivityStateManager"));
        if (activityStateManager == null) {
            throw new IllegalStateException("Unable to find an instance of ActivityStateManager registered under bean id blActivityStateManager");
        }
        ProcessContext<?> context = null;
        
        RollbackStateLocal rollbackStateLocal = new RollbackStateLocal();
        rollbackStateLocal.setThreadId(String.valueOf(Thread.currentThread().getId()));
        rollbackStateLocal.setWorkflowId(getBeanName());
        RollbackStateLocal.setRollbackStateLocal(rollbackStateLocal);
        
        try {
            //retrieve injected by Spring
            List<Activity<ProcessContext<?>>> activities = getActivities();

            //retrieve a new instance of the Workflow ProcessContext
            context = createContext(seedData);

            for (Activity<ProcessContext<?>> activity : activities) {
                if (activity.shouldExecute(context)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("running activity:" + activity.getBeanName() + " using arguments:" + context);
                    }
    
                    try {
                        context = activity.execute(context);
                    } catch (Throwable th) {
                        try {
                            if (getAutoRollbackOnError()) {
                                LOG.info("Automatically rolling back state for any previously registered " +
                                        "RollbackHandlers. RollbackHandlers may be registered for workflow activities" +
                                        " in appContext.");
                                ActivityStateManagerImpl.getStateManager().rollbackAllState();
                            }
                            ErrorHandler errorHandler = activity.getErrorHandler();
                            if (errorHandler == null) {
                                LOG.info("no error handler for this action, run default error" + "handler and abort " +
                                        "processing ");
                                getDefaultErrorHandler().handleError(context, th);
                                break;
                            } else {
                                LOG.info("run error handler and continue");
                                errorHandler.handleError(context, th);
                            }
                        } catch (RuntimeException e) {
                            LOG.error("An exception was caught while attempting to handle an activity generated exception", e);
                            throw e;
                        } catch (WorkflowException e) {
                            LOG.error("An exception was caught while attempting to handle an activity generated exception", e);
                            throw e;
                        }
                    }
    
                    //ensure its ok to continue the process
                    if (processShouldStop(context, activity)) {
                        break;
                    }
    
                    //register the RollbackHandler
                    if (activity.getRollbackHandler() != null && activity.getAutomaticallyRegisterRollbackHandler()) {
                        ActivityStateManagerImpl.getStateManager().registerState(activity, context, activity.getRollbackRegion(), activity.getRollbackHandler(), activity.getStateConfiguration());
                    }
                } else {
                    LOG.debug("Not executing activity: " + activity.getBeanName() + " based on the context: " + context);
                }
            }
        } finally {
            rollbackStateLocal = RollbackStateLocal.getRollbackStateLocal();
            if (rollbackStateLocal != null && rollbackStateLocal.getWorkflowId().equals(getBeanName())) {
                activityStateManager.clearAllState();
            }
        }
        LOG.debug(getBeanName() + " processor is done.");

        return context;
    }

    /**
     * Determine if the process should stop
     *
     * @param context
     *            the current process context
     * @param activity
     *            the current activity in the iteration
     */
    protected boolean processShouldStop(ProcessContext<?> context, Activity<? extends ProcessContext<?>> activity) {
        if (context == null || context.isStopped()) {
            LOG.info("Interrupted workflow as requested by:" + activity.getBeanName());
            return true;
        }
        return false;
    }

    protected ProcessContext<Object> createContext(Object seedData) throws WorkflowException {
        return processContextFactory.createContext(seedData);
    }

    @Override
    public void setProcessContextFactory(ProcessContextFactory<Object, Object> processContextFactory) {
        this.processContextFactory = processContextFactory;
    }

}
