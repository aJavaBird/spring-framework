package com.yn.zhc.spring;

import com.yn.zhc.spring.dao.AppDao;
import com.yn.zhc.spring.dao.AppDao2;
import org.springframework.context.annotation.*;

/**
 *  @Configuration 这个注解不写（仅写@ComponentScan就行），代码也可以正常运行
 *  @Configuration 表示的是配置类，既然此注解没有什么用，那为什么还要加呢？
 *  其实，@Configuration 是有用的，而且作用很大，使用了 @Configuration 的类会被spring进行cglib代理。
 *  代理的原因是，可以实现 EnhancedConfiguration接口（BeanFactoryAware子接口），可以拿到beanFactory
 *  这样，AppConfig 中的@Bean在获取的时候，就能从 beanFactory 的map中进行获取，就不用再次创建
 *
 *  @Configuration 官方说明：
 *  用@Conﬁguration注释类表明其主要目的是作为bean定义的源
 *  @Conﬁguration类允许通过调用同一类中的其他@Bean方法来定义bean之间的依赖关系
 *  */
@Configuration
@ComponentScan("com.yn.zhc.spring")
//@ImportResource("classpath:spring.xml")
public class AppConfig {

	@Bean
	public AppDao getAppDao(){
		return new AppDao();
	}

	@Bean
	public AppDao2 getAppDao2(){
		getAppDao();
		return new AppDao2();
	}
}
