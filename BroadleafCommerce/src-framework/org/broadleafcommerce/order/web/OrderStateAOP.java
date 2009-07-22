package org.broadleafcommerce.order.web;

import org.aspectj.lang.ProceedingJoinPoint;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.web.OrderState;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class OrderStateAOP implements ApplicationContextAware {
	
    private ApplicationContext applicationContext;

	public Object processOrderRetrieval(ProceedingJoinPoint call) throws Throwable {
		Object returnValue;
		/*
		 * we retrieve the OrderState instance directly from the application context, as this
		 * bean has a request scope.
		 */
		OrderState orderState = (OrderState) applicationContext.getBean("blOrderState");
		Order order = orderState.getOrder();
		if (order != null) {
			returnValue = order;
		} else {
			returnValue = call.proceed();
			orderState.setOrder((Order) returnValue);
		}

        return returnValue;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
