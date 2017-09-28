happylifeplat-tcc
================
碧桂园旺生活平台解决分布式事务方案之tcc开源框架(try,confirm,cancel)。
基于java语言来开发（JDK1.8），支持dubbo，springcloud等rpc框架进行分布式事务。
因为文件名太长，大家在拉取代码的时候执git命令：git config core.longpaths true

 # Features

 * ***框架特性***

  * 支持dubbo，springcloud等rpc框架进行分布式事务。

  * 采用Aspect AOP 切面思想与Spring无缝集成，天然支持集群。

  * 配置简单，集成简单，源码简洁，稳定性高，已在生产环境使用。

  * 内置经典的分布式事务场景demo工程，并有swagger-ui可视化界面可以快速体验。


 * ***SPI扩展***
     * 本地事务存储，支持redis，mogondb，zookeeper，file，mysql等关系型数据库
     * 序列化方式，支持java，hessian，kryo，protostuff


# TCC原理介绍



#   Configuration

### @Tcc详解

*  @Tcc  该注解为分布式事务的切面（AOP point）

*  注解中confirmMethod="xxx" 为在tcc分布式事务中confirm角色的方法名称。

*  注解中cancelMethod="xxx" 为在tcc分布式事务中cancel角色的方法名称。

*  注解中TccPatternEnum 为在tcc分布式事务中的模式，现在有tcc，和cc2种。

######  特别注意：try, confirm,cancel 3个方法的参数类型必须一致。

######  cc模式含义为confrim，cancel，即在try中没有任何数据的操作，只有对数据的校验，在try阶段发生异常，不会进行cancel方法的调用。

###  使用配置：
* 在接口上添加@Tcc注解（dubbo则需要填加在api接口上，springcloud则需要加在feignClient上），具体参考demo工程。

* 在接口实现上 添加@Tcc(confirmMethod = "方法名称", cancelMethod = "方法名称"),并提供confrim，cancel方法名称，具体参考demo工程


###  applicationContext.xml 详解：
```
  <!-- Aspect 切面配置，是否开启AOP切面-->
  <aop:aspectj-autoproxy expose-proxy="true"/>
  <!--扫描框架的包-->
  <context:component-scan base-package="com.happylifeplat.tcc.*"/>
  <!--启动类属性配置-->
  <bean id="tccTransactionBootstrap" class="com.happylifeplat.tcc.core.bootstrap.TccTransactionBootstrap">
         <property name="serializer" value="kryo"/>
         <property name="coordinatorQueueMax" value="5000"/>
         <property name="coordinatorThreadMax" value="4"/>
         <property name="recoverDelayTime" value="120"/>
         <property name="retryMax" value="3"/>
         <property name="rejectPolicy" value="Abort"/>
         <property name="blockingQueueType" value="Linked"/>
         <property name="scheduledDelay" value="120"/>
         <property name="scheduledThreadMax" value="4"/>
         <property name="repositorySupport" value="db"/>
         <property name="tccDbConfig">
             <bean class="com.happylifeplat.tcc.common.config.TccDbConfig">
                 <property name="url"
                           value="jdbc:mysql://192.168.1.68:3306/account?useUnicode=true&amp;characterEncoding=utf8"/>
                 <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
                 <property name="password" value="Wgj@555888"/>
                 <property name="username" value="xiaoyu"/>
             </bean>
         </property>
     </bean>

```
###### TccTransactionBootstrap 详解（具体参见com.happylifeplat.tcc.common.config.TccConfig）：
```
  <!--数据保存序列化方式  spi扩展支持 java kroy，hessian protostuff 推荐使用kroy-->
  <property name="serializer" value="kryo"/>

  <!--协调资源线程池最大队列-->
  <property name="coordinatorQueueMax" value="5000"/>

  <!--协调资源线程池最大线程-->
  <property name="coordinatorThreadMax" value="4"/>

  <!--事务延迟多少时间恢复，单位秒-->
  <property name="recoverDelayTime" value="120"/>

  <!--事务执行失败最大重试次数-->
  <property name="retryMax" value="3"/>

  <!--  线程池中的队列类型 spi扩展支持 Linked Array SynchronousQueue-->
  <property name="blockingQueueType" value="Linked"/>

  <!--线程池中的拒绝策略 spi扩展支持 Abort Blocking CallerRuns Discarded Rejected-->
  <property name="rejectPolicy" value="Abort"/>

  <!--调度线程池间隔时间 单位秒-->
  <property name="scheduledDelay" value="120"/>

  <!--调度线程池最大线程数-->
  <property name="scheduledThreadMax" value="4"/>


```
###### 本地数据保存配置与详解(spi扩展支持db，redis，zookeeper，mongodb，file),详情配置请参照demo工程:
*  数据存储为数据库（数据库支持mysql，oracle ，sqlServer），当业务模块为集群时，推荐使用
  会自动创建表，表名称为 tcc_transaction_模块名称（applicationName）
```
       <!--配置补偿类型为db-->
       <property name="repositorySupport" value="db"/>
        <property name="tccDbConfig">
            <bean class="com.happylifeplat.tcc.common.config.TccDbConfig">
                <property name="url"
                          value="jdbc:mysql://192.168.1.68:3306/account?useUnicode=true&amp;characterEncoding=utf8"/>
                <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
                <property name="password" value="Wgj@555888"/>
                <property name="username" value="xiaoyu"/>
            </bean>
        </property>
```
*  数据存储为redis，当业务模块为集群时，推荐使用
```
   <!--配置资源存储类型为reids-->
     <property name="repositorySupport" value="redis" />
        <property name="tccRedisConfig">
            <bean class="com.happylifeplat.tcc.common.config.TccRedisConfig">
                <property name="hostName"
                          value="192.168.1.68"/>
                <property name="port" value="6379"/>
                <!--redis 密码 （有密码就配置，无密码则不需要配置）-->
                <property name="password" value=""/>
            </bean>
        </property>
```

*  本地数据存储为zookeeper，当业务模块为集群时，推荐使用
```
     <!--配置补偿类型为zookeeper-->
     <property name="repositorySupport" value="zookeeper"/>
     <property name="tccZookeeperConfig">
         <bean class="com.happylifeplat.tcc.common.config.TccZookeeperConfig">
             <!--zookeeper host：port-->
             <property name="host"  value="192.168.1.66:2181"/>
             <!--zookeeper  session过期时间-->
             <property name="sessionTimeOut" value="2000"/>
             <!--zookeeper  根节点路径-->
             <property name="rootPath" value="/tx"/>
         </bean>
     </property>
```
* 本地数据存储为mongodb，当业务模块为单节点时，可以使用。会自动创建集合，集合名称为 tx_transaction_模块名称（applicationName）
  这里mongdb连接方式采用3.4.0版本推荐使用的Sha1,不是CR模式，同时mongdb应该开启权限认证，使用者需要注意
```
       <!--配置补偿类型为mongodb-->
       <property name="repositorySupport" value="mongodb"/>
        <property name="tccMongoConfig">
           <bean class="com.happylifeplat.tcc.common.config.TccMongoConfig">
               <!--mongodb url-->
               <property name="mongoDbUrl"  value="192.168.1.78:27017"/>
               <!--mongodb 数据库-->
               <property name="mongoDbName" value="happylife"/>
               <!--mongodb 用户名-->
               <property name="mongoUserName" value="xiaoyu"/>
               <!--mongodb 密码-->
               <property name="mongoUserPwd" value="123456"/>
           </bean>
       </property>
```
*  本地数据存储为file，当业务模块为单节点时，可以使用。创建的文件名称tcc_  +  prefix配置 + 模块名称
```
     <!--配置补偿类型为file-->
     <property name="repositorySupport" value="file"/>
     <property name="tccFileConfig">
            <bean class="com.happylifeplat.tcc.common.config.TccFileConfig">
                <!--指定文件路径（可填可不填，不填时候，默认就是当前项目所在的路径）-->
                <property name="path"  value=""/>
                <!--指定文件前缀，生成文件名称-->
                <property name="prefix" value="consume"/>
            </bean>
      </property>
```

# Usage

### 快速体检，运行[dubbo-demo](https://github.com/yu199195/happylifeplat-transaction/tree/master/happylifeplat-transaction-tx-sample/happylifeplat-transaction-tx-dubbo-sample)（ 使用者JDK必须为1.8）

* 执行工程文件sql文件夹下的tcc-demo.sql

* 在每个工程下的更改application.yml 中数据库连接(host，用户名，密码等)

* 在每个工程下 更改applicationContext.xml中的tccDbConfig 数据库连接(host，用户名，密码等)，最好与模块数据库一致

* 在每工程下的spring-dubbo.xml 中配置您的zookeeper注册中心

* 依次启动account模块，inventory模块 ,order模块（运行springboot的启动类中的main方法）

* 访问http://localhost:8083/swagger-ui.html 进入体验。

### 快速体检，运行[springcloud-demo](https://github.com/yu199195/happylifeplat-transaction/tree/master/happylifeplat-transaction-tx-sample/happylifeplat-transaction-tx-dubbo-sample)（ 使用者JDK必须为1.8）

* 执行工程文件sql文件夹下的tcc-demo.sql

* 在每个工程下的更改application.yml 中数据库连接(host，用户名，密码等)

* 在每个工程下 更改applicationContext.xml中的tccDbConfig 数据库连接(host，用户名，密码等)，最好与模块数据库一致

* 启动springcloud-eureka项目

* 依次启动account模块，inventory模块 ,order模块（运行springboot的启动类中的main方法）

* 访问http://localhost:8884/swagger-ui.html 进入体验。



 # Support
   ##### 如有任何问题欢迎加入QQ群：162614487 进行讨论


 # Contribution
