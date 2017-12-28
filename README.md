happylifeplat-tcc
================

碧桂园旺生活平台解决分布式事务方案之tcc开源框架。基于java语言来开发（JDK1.8），支持dubbo，springcloud等rpc框架进行分布式事务。

 # Features

 * **框架特性**

     * 支持dubbo,motan,springcloud等rpc框架进行分布式事务。

     * 采用Aspect AOP 切面思想与Spring无缝集成，天然支持集群。

     * 配置简单，集成简单，源码简洁，稳定性高，已在生产环境使用。

     * 内置经典的分布式事务场景demo工程，并有swagger-ui可视化界面可以快速体验。


 * **SPI扩展**
     * 本地事务存储，支持redis，mongodb，zookeeper，file，mysql等关系型数据库

     * 序列化方式，支持java，hessian，kryo，protostuff


# Prerequisite

  *   #### JDK 1.8+

  *   #### Maven 3.2.x

  *   #### Git

  *   ####  RPC framework dubbo or motan or springcloud。

  *   #### Message Oriented Middleware

# TCC原理介绍
  ####  [原理介绍](https://github.com/yu199195/happylifeplat-tcc/wiki/TCC%E5%8E%9F%E7%90%86%E4%BB%8B%E7%B4%B9)


# Quick Start

* #### Clone & Build
   ```
   > git clone https://github.com/yu199195/happylifeplat-tcc.git

   > cd happylifeplat-tcc

   > mvn -DskipTests clean install -U
   ```

   ### [快速体验(dubbo)](https://github.com/yu199195/happylifeplat-tcc/wiki/%E5%BF%AB%E9%80%9F%E4%BD%93%E9%AA%8C%EF%BC%88dubbo%EF%BC%89)

   ### [快速体验(springcloud)](https://github.com/yu199195/happylifeplat-tcc/wiki/%E5%BF%AB%E9%80%9F%E4%BD%93%E9%AA%8C%EF%BC%88springcloud%EF%BC%89)



#   Configuration

  ####  [配置详解](https://github.com/yu199195/happylifeplat-tcc/wiki/%E9%85%8D%E7%BD%AE%E8%AF%A6%E8%A7%A3)



# User Guide

###  [dubbo 用户](https://github.com/yu199195/happylifeplat-tcc/wiki/dubbo%E7%94%A8%E6%88%B7%E6%8C%87%E5%8D%97)


###  [springcloud 用户](https://github.com/yu199195/happylifeplat-tcc/wiki/springcloud%E7%94%A8%E6%88%B7%E6%8C%87%E5%8D%97)







# FAQ

* ### 为什么我下载的代码后，用idea打开没有相应的get set 方法呢？
   ##### 答：因为框架使用了Lombok包，它是在编译的时期，自动生成get set方法，并不影响运行，如果觉得提示错误难受，请自行下载lombok包插件，[lombok官网](http://projectlombok.org/)

* ### 为什么我运行demo工程，找不到applicationContent.xml呢？
  ##### 答：请设置项目的资源文件夹。

 # Support

 ### 如有任何问题欢迎加入QQ群：162614487 进行讨论 
  ![](https://yu199195.github.io/images/weixin.jpg)

 # Contribution
