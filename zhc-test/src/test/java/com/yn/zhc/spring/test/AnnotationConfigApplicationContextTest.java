package com.yn.zhc.spring.test;

import com.yn.zhc.spring.AppConfig;
import com.yn.zhc.spring.component.ZhcApplicationContextAware;
import com.yn.zhc.spring.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AnnotationConfigApplicationContextTest {
	public static void main(String[] args) {

		/**
		 * AnnotationConfigApplicationContext 初始化的时候做了些什么？
		 * 1、会先调用父类 GenericApplicationContext 的 构造方法：实例化一个 DefaultListableBeanFactory
		 *     DefaultListableBeanFactory 中的重点属性包括：
		 *         dependencyComparator（排序）
		 *         beanDefinitionMap（一个存<beanName,beanDefinition>的Map）
		 *                BeanDefinition 中的属性举例：ParentName、BeanClassName、Scope、LazyInit、DependsOn......
		 *         beanDefinitionNames（一个存放 beanName 的list）
		 * 2、实例化一个 AnnotatedBeanDefinitionReader （bean的读取器），主要内容如下：
		 *     AnnotationConfigApplicationContext 是作为 AnnotatedBeanDefinitionReader 的入参register（BeanDefinitionRegistry）的
		 *     调用 AnnotationConfigUtils#registerAnnotationConfigProcessors(BeanDefinitionRegistry)
		 *     往 beanDefinitionMap 中注册bean (调用的是 register#registerBeanDefinition )
		 * 3、实例化一个 ClassPathBeanDefinitionScanner （解析/扫描基本信息，比如新加的包/类）
		 *     初始化时扫描包并不是使用的这个对象，这个对象是手动扫描包（即调用applicationContext的scan方法）时使用的
		 * 4、当以Class类型作为AnnotationConfigApplicationContext入参时，会调用 register方法，将此类注册到 beanDefinitionMap中
		 * 5、重点方法：refresh()
		 *    （01）prepareRefresh方法: 准备
		 *    （02）obtainFreshBeanFactory方法：获取beanFactory，默认DefaultListableBeanFactory
		 *    （03）prepareBeanFactory(beanFactory)方法：增加一些spring自己的一些后置处理器
		 *    （04）postProcessBeanFactory方法：保留方法，目前空实现
		 *    （05）invokeBeanFactoryPostProcessors方法
		 *        getBeanFactoryPostProcessors() 是获取自己定义的 BeanFactoryPostProcessor
		 *        调用 PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors
		 *            实现将自己定义的和spring定义的 BeanFactoryPostProcessor 都注册进容器
		 *            （注意，spring的是BeanDefinitionRegistryPostProcessor，为BeanFactoryPostProcessor子接口）
		 *            此方法中的重点方法：
		 *                invokeBeanDefinitionRegistryPostProcessors：
		 *                    循环调用spring定义的BeanDefinitionRegistryPostProcessor方法
		 *                    此方法循环调用的类中，需要注意一个名为 ConfigurationClassPostProcessor 的类【重点】：
		 *                        ConfigurationClassUtils.checkConfigurationClassCandidate：
		 *                            取出所有的BeanDefinition
		 *                            判断类是否有Configuration注解
		 *                            判断类是否有Component、ComponentScan、Import、ImportResource注解
		 *                            为什么判断了Component而没有判断Service、Repository、Controller呢？
		 *                                仔细看一下后四者的源码，都有一个AliasFor注解指向了Component
		 *                        如果上一步判断为有，则将bean定义加入到 configCandidates 中，进行排序，然后解析
		 *                        解析方法 ConfigurationClassParser#parse -> processConfigurationClass -> doProcessConfigurationClass
		 *                            doProcessConfigurationClass方法说明：
		 *                                如果有 ComponentScans 或者 ComponentScan 注解
		 *                                则循环 扫描，将对应的类生成 BeanDefinition
		 *                                扫描包使用的是方法 ClassPathBeanDefinitionScanner#doScan()【重点】
		 *                                    在实例化ClassPathBeanDefinitionScanner时，会将register作为入参
		 *                                    在doScan()中，会将扫描出来的BeanDefinition进行 registerBeanDefinition
		 *                                    （我本以为doScan返回BeanDefinition列表后，才存入spring容器map中，其实是在doScan里面就存入了）
		 *    （6）registerBeanPostProcessors方法：
		 *        注册 BeanPostProcessor，注册流程是从 beanDefinitionMap 中获取类型为 BeanPostProcessor 的定义，然后存到 beanPostProcessors 列表中
		 *        其中 beanDefinitionMap 为 DefaultListableBeanFactory 的属性，beanPostProcessors 为其父类的属性
		 *    （7）initMessageSource方法：国际化方法，不重要
		 *    （8）initApplicationEventMulticaster方法：spring 事件
		 *    （9）onRefresh方法：预留方法，方法体为空
		 *    （10）registerListeners方法：非重点
		 *    （11）finishBeanFactoryInitialization方法：重点方法之一
		 *        实现bean的实例化，并执行相关的 BeanPostProcessor 方法，
		 *        然后将bean交给容器管理（即存到 singletonObjects 中）
		 *        singletonObjects 也属于 BeanFactory 的属性，因为它存在于 DefaultSingletonBeanRegistry类中
		 *        而 DefaultListableBeanFactory 继承自 AbstractBeanFactory，
		 *            AbstractBeanFactory继承自 DefaultSingletonBeanRegistry
		 *    （13）finishRefresh方法：做一些后续处理
		 *
		 * */

		AnnotationConfigApplicationContext annotationConfigApplicationContext
				= new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.register(AppConfig.class);
		// 如果要添加自定义的 BeanFactoryPostProcessor，
		// 使用@Component不谢，需要手动调用 annotationConfigApplicationContext.addBeanFactoryPostProcessor(...);
		annotationConfigApplicationContext.refresh();
		/** // 可用 一条语句代替上面的3条： 无参构造 + register + refresh
		 *  AnnotationConfigApplicationContext annotationConfigApplicationContext
		 = new AnnotationConfigApplicationContext(AppConfig.class);
		 */

		IndexDao indexDao = annotationConfigApplicationContext.getBean(IndexDao.class);
		indexDao.sayHello();

		ZhcApplicationContextAware zhcApplicationContextAware = annotationConfigApplicationContext.getBean(ZhcApplicationContextAware.class);

		Object indexDao2 = zhcApplicationContextAware.getApplicationContext().getBean("indexDao");
		System.out.println("indexDao2==indexDao --> "+(indexDao2==indexDao));

	}
}

/**
 * Spring 中的几个需要注意的类型
 *
 * 1、BeanDefinitionRegistryPostProcessor
 *       这个是 BeanFactoryPostProcessor 的子接口，在 BeanFactoryPostProcessor 之前执行
 *       因为 源码中先遍历的 BeanDefinitionRegistryPostProcessor
 *       （有Spring定义的，还有自定义的，其中自定义的先执行）
 *       这里注意 ConfigurationClassPostProcessor，
 *           它实现 包/类扫描、3中import扫描、@Bean扫描，
 *           以及判断一个类是否是一个完整的配置类（即是否使用了@Configuration）
 * 2、BeanFactoryPostProcessor
 *       springbean容器当中任意一个bean被new出来之前执行的，针对beanFactory来建设
 *       这里还是注意 ConfigurationClassPostProcessor，
 *           它实现针对配置类（使用了@Configuration的类），加上cglib代理
 *           为什么要加cglib代理呢？
 *             详情请参考
 *             ConfigurationClassPostProcessor#enhanceConfigurationClasses 中的注释
 *             AppConfig 类中注释
 * 3、BeanPostProcessor
 *       插手bean的实例化过程，
 *       bean实例化之后，在bean没有被spring的bean容器管理前插手干活
 *       经典场景@PostConstruct、aop
 * 4、ImportSelector
 *       通过 selectImports 方法返回类全路径名，可以动态添加需要实例化的类
 *       spring在 ConfigurationClassPostProcessor 将这些类变成 BeanDefinition
 * 5、ImportBeanDefinitionRegistrar
 *       内部可以获得 registry，可以自定义初始化和管理bean，比 ImportSelector 更强
 *
 * */
