/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.camel.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 
 * This is to access from non-spring classes
 *
 */
@Component
public class SpringContext implements ApplicationContextAware {

	@Autowired
	static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	public static ApplicationContext getAppContext() {
		return context;
	}
}