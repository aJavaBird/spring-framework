package com.yn.zhc.spring.aop;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

//@Component
public class ZhcBeanPostProcessor02 implements BeanPostProcessor, Ordered {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("ZhcBeanPostProcessor02 before -- "+beanName);
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("ZhcBeanPostProcessor02 after -- "+beanName);
		return bean;
	}

	@Override
	public int getOrder() {
		// BeanPostProcessor 的执行顺序，越小越靠前执行
		return 102;
	}
}
