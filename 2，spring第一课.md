1，Spring体系架构（基于4.x）

![](C:\Users\ningbo\Documents\spring.png)

**2，*常见*的加载Spring容器的方式有两种**

**1）通过 xml 配置文件形式**

ApplicationContext ac = new ClassPathXmlApplicationContext("xxx.xml");

**2）通过配置类的形式**

ApplicationContext ac = new AnnotationConfigApplicationContext(XxxConfig.class);



**3，BeanDefinitionReader、BeanDefinitionScanner 、BeanDefinitionMap、BefinDefinitionRegistry 、BeanDefinition 之间的关系以及和bean信息读取的大致过程。先大致理解一下，后续根据源码解释更详细的步骤。**

如上两种不同配置bean的方式，Spring读取Bean的方式也是不一样。后面会详细介绍两种读取的详细源码。这里要讲的是Spring会将不同的bean配置方式读取为一个统一的Bean定义（BeanDefinition）。最终spring的工厂会根据这个Bean定义来生成Bean。结合起来可以这样理解

1，spring 中会由 **BeanDefinitionReader** 去读取到Bean配置信息(注解或xml配置)，如下

@Component

public class Person{......}

或者

<bean id="xxx" class="com.xxx.Person" scope="xxx"/>

注意，**BeanDefinitionReader** 也是一个接口。可以理解到，读取注解的bean 和 读取xml配置的会是不同的实现类。可以理解顺序是：

a.**BeanDefinitionReader** 回去读取配置类 或 xml配置文件。

b.**BeanDefinitionScanner** 会根据配置类 或 xml上配置的扫描包路径，去扫描对应的包及其子包。

c.扫描到后，会将扫描到的Bean配置封装成BeanDefinition 对象，然后由 **BeanDefinitionRegistry** 注册到 **BeanDefinitionMap**容器中。

2，读取到Bean的信息后，将其封装为 BeanDefinition 对象。BeanDefinition 包含了该bean的所有信息，如：

bean的类信息的Class 对象、bean的scope、是否懒加载、是否抽象bean、等等一些列bean的定义信息，都在里面。注意，BeanDefinition 是一个接口，封装了生产bean的所有配置信息。

3，封装为BeanDefinition 对象后，会由 **BeanDefinitionRegistry** 将其注册到 BeanDefinitionMap（是一个BeanDefinition的容器）中。注意，**BefinDefinitionRegistry** 也是一个接口。



**4，bean 的大致生命周期**

1）实例化，**根据上面讲的 BeanDefinition** ，根据**反射**或**工厂方式**生成一个Bean的实例对象。这里的工厂方式，一个显著的特征就是工厂方式我们可以自己编写生成bean实例的逻辑，如new 对象（当然也能通过反射的形式）。比如 @Bean 标注的方法通过new 生成bean实例 或 在方法里通过反射来生成对象，其实这就算是一种工厂方法生成bean。这个要灵活理解一下。而通过@Component 注解 或 xml 配置则是Spring自己通过反射的方式生成的。

2）给bean填充属性，如**填充@Autowired 、@Value 标注的属性**。

3）调用初始化方法 **initMethod** 指定的方法

4）将这个**bean存放到一个** **"单例池 或者叫 一级缓存**" 中。这个**“单例池” 是一个 Map**，key是bean的名称，value就是bean的实例。

```java
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
   /** Cache of singleton objects: bean name to bean instance. */
   //这个就是单例池Map对象。
   private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
```

5）调用销毁方法，**destroyMethod** 指定的方法



**5，扩展点，spring生态之所以能集成其他框架，这些扩展点的功能便是重点支撑之一****。**

**-- Bean工厂的相关后置处理器**（BeanFactoryPostProcessor、BeanDefinitionRegistryPostProcessor）

Spring 提供了 **BeanFactoryPostProcessor** （后置处理器） ，可以用来修改 Bean 的定义（即：BeanDefinition 对象的信息）如：将Bean 修改为单例或多例等。如：

```
@Component //需要将该后置处理器定义为Bean组件。
public class PersonBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    /**
     * postProcessBeanFactory 该方法只会执行一次。
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        /*
        利用该后置处理器，修改bean定义
         */
        //拿到名称为 car 的bean 的bean定义对象（BeanDefinition）
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition)beanFactory.getBeanDefinition("car");
        //修改该bean定义信息，可以直接修改对应的类对象
        beanDefinition.setBeanClass(Person.class);
        //修改初始化方法
        beanDefinition.setInitMethodName("init");
    }
}
```

**BeanFactoryPostProcessor** 还有一个子接口：**BeanDefinitionRegistryPostProcessor** ，实现该接口可以进行**bean定义的注册**。如下：

```
@Component
public class PersonBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    /**
     * postProcessBeanFactory 该方法只会执行一次。
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        /*
        利用该后置处理器，修改bean定义
         */
        //拿到名称为 car 的bean 的bean定义对象（BeanDefinition）
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition)beanFactory.getBeanDefinition("car");
        //修改该bean定义信息，可以直接修改对应的类对象
        beanDefinition.setBeanClass(Person.class);
        //修改初始化方法
        beanDefinition.setInitMethodName("init");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //通过 registry 向容器中注册bean定义
        registry.registerBeanDefinition("", new BeanDefinition() {......});
    }
}
```

**-- Bean的后置处理器（BeanPostProcessor）**

spring提供了**很多bean的后置处理器**来实现一系列特定功能。比如，AOP功能就是在后置处理器中实现的。

具体用法后面再补充

**-- Aware 接口**

aware 可以翻译为 “感知”。spring提供了很多Aware来实现特定功能，即在spring bean生命周期中“感知”一些东西，如感知spring给bean的名称、感知spring应用上下文等。

具体用法后面再补充

**-- BeanFactory 和 ApplicationContext 介绍**

1，BeanFactory 是Spring的顶层接口，表示Bean工厂接口。提供了一些 获取Bean的对象的方法：

```java
public interface BeanFactory {
    String FACTORY_BEAN_PREFIX = "&";
    Object getBean(String name) throws BeansException;
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
    Object getBean(String name, Object... args) throws BeansException;
    <T> T getBean(Class<T> requiredType) throws BeansException;
    <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
    <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);
    <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);
    boolean containsBean(String name);
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;
    boolean isPrototype(String name) throws NoSuchBeanDefinitionException;
    boolean isTypeMatch(String name, ResolvableType typeToMatch) 
        throws NoSuchBeanDefinitionException;
    boolean isTypeMatch(String name, Class<?> typeToMatch) 
        throws NoSuchBeanDefinitionException;
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;
    Class<?> getType(String name, boolean allowFactoryBeanInit) 
        throws NoSuchBeanDefinitionException;
    String[] getAliases(String name);
    
}
```

BeanFactory 有很多子接口和实现类，如下：

**DefaultListableBeanFactory**  **默认**的工厂实现类。该子类实现了很多BeanFactory 接口的子接口 和 子类，所以具备了很多不同的bean工厂的能力，目前所知，它是最下层的工厂实现类，功能最强。并且它还实现了**BeanDefinitionRegistry** 接口等，**所以具备了 Bean定义的注册功能**。



**-- Spring 启动流程，按如下代码**

```java
ApplicationContext applicationContext = new AnnotationConfigApplicationContext(BeanConfig.class);
Person person = (Person) applicationContext.getBean("person");
System.out.println(person);
```

1，创建应用上下文时，会创建**工厂对象**，默认的工厂类是 **DefaultListableBeanFactory**

```
this.beanFactory = new DefaultListableBeanFactory();
```

2，创建一个读取注解的**Bean定义**读取器。**完成了spring内部BeanDefinition的注册**（主要是后置处理器）

```
this.reader = new AnnotatedBeanDefinitionReader(this);
```

- 给工厂对象设置实现了 Order 接口的排序器
- 给工厂对象设置@AutoWired 的候选解析器 **ContextAnnotationAutowireCandidateResolver**。如果字段上带有@Lazy注解，表示进行懒加载 Spring 不会立即创建注入属性的实例，而是生成代理对象
- 给容器中注册了**解析配置类**的后置处理器 **ConfigurationClassPostProcessor**，该后置处理器会去解析加了@Configuration 的配置类，还会解析@ComponentScan、@ComponentScans注解指定的包，还会解析@Import，@Bean 等注解。该配置类后置处理器还实现了 **BeanFactoryPostProcessor** 接口
- 给容器中注册了**处理 @Autowired 注解**的处理器 **AutowiredAnnotationBeanPostProcessor**，该后置处理器会解析@Autowired、@Value 等注解。
- **等等还有很多后置处理器**。

   这些后置处理器有特殊的用途。

3，**创建 BeanDefinition扫描器** **ClassPathBeanDefinitionScanner**，可以用来扫描包或者类，继而将扫描到的Bean转换为BeanDefinition。spring在执行工程后置处理器 ConfigurationClassPostProcessor 时，会去扫描包时会new 一个 ClassPathBeanDefinitionScanner，这里的scanner仅仅是为了程序员可以手动调用AnnotationConfigApplicationContext 对象的scan 方法

```
this.scanner = new ClassPathBeanDefinitionScanner(this);
```

4，使用bean定义的读取器 AnnotatedBeanDefinitionReader 去读取所有配置类，并将配置类封装为BeanDefinition 对象。

**经过以上四步，将spring的很多解析器，后置处理器，扫描器，读取器等组件封装为 BeanDefinition 对象存入了 BeanDefinitionMap 中。**

5，调用 invokeBeanFactoryPostProcessors(beanFactory) 方法，去获取所有的@ComponentScan 指定的包，识别 @Bean 等注解，将所有项目中的Bean 都封装成Bean定义，存放到BeanDefinitionMap  中。

