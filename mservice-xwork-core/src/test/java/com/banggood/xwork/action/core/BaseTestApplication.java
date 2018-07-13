package com.banggood.xwork.action.core;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = XWorkApp.class)
public abstract class BaseTestApplication {
	
	@Autowired
    private ConfigurableListableBeanFactory context;
	
    @Autowired
    ConfigurableApplicationContext configurableApplicationContext;
    
    public BaseTestApplication()
    {
    	
    }

}
