happylifeplat-tcc
================

### 碧桂园旺生活平台解决分布式事务方案之tcc开源框架。基于java语言来开发（JDK1.8），支持dubbo，springcloud等rpc框架进行分布式事务。

 # Features

 * **框架特性**

     * 支持dubbo,motan,springcloud等rpc框架进行分布式事务。

     * 采用Aspect AOP 切面思想与Spring无缝集成，天然支持集群。

     * 配置简单，集成简单，源码简洁，稳定性高，已在生产环境使用。

     * 内置经典的分布式事务场景demo工程，并有swagger-ui可视化界面可以快速体验。


 * **SPI扩展**
     * 本地事务存储，支持redis，mongodb，zookeeper，file，mysql等关系型数据库

     * 序列化方式，支持java，hessian，kryo，protostuff


#  源码解析博客

  * ## https://yu199195.github.io/categories/happylifeplat-tcc/
  

#  视频详解

  * ## 环境搭建以及运行 : http://www.iqiyi.com/w_19rwkrfu69.html#vfrm=16-1-1-1
  * ## 源码详解以及调试 : http://www.iqiyi.com/w_19rwkreutt.html


# Prerequisite

  *   #### JDK 1.8+

  *   #### Maven 3.2.x

  *   #### Git

  *   ####  RPC framework dubbo or motan or springcloud。


# TCC原理介绍
* ###  https://github.com/yu199195/happylifeplat-tcc/wiki/Theory

#   Configuration

  * ###  https://github.com/yu199195/happylifeplat-tcc/wiki/Configuration


# Quick Start

 * #### Clone & Build
   ```
   > git clone https://github.com/yu199195/happylifeplat-tcc.git

   > cd happylifeplat-tcc

   > mvn -DskipTests clean install -U
   ```

* #### execute this sql       
    https://github.com/yu199195/happylifeplat-tcc/blob/master/happylifeplat-tcc-demo/sql/tcc-demo.sql

* #### Find the RPC framework that works for you
    https://github.com/yu199195/happylifeplat-tcc/tree/master/happylifeplat-tcc-demo
* ### [Dubbo-Quick-Start](https://github.com/yu199195/happylifeplat-tcc/wiki/Dubbo-Quick-Start)

* ### [SpringCloud-Quick-Start](https://github.com/yu199195/happylifeplat-tcc/wiki/SpringCloud-Quick-Start)





# User Guide

* #### 关于jar包引用问题，现在jar包还未上传到maven的中央仓库，所以使用者需要自行获取代码，然后打包上传到自己maven私服

   ```
   > git clone https://github.com/yu199195/happylifeplat-tcc.git

   > mvn -DskipTests clean deploy -U
   ```
* #### 关于jar包版本问题 ，现在因为没有传到中央仓库，如果引用的话，请自行设置相应的版本。


*  ## [Dubbo User Guide](https://github.com/yu199195/happylifeplat-tcc/wiki/Dubbo-User-Guide)

*  ## [SpringCloud User Guide](https://github.com/yu199195/happylifeplat-tcc/wiki/SpringCloud-User-Gruid)



# FAQ

* ### 为什么我下载的代码后，用idea打开没有相应的get set 方法呢？
   ##### 答：因为框架使用了Lombok包，它是在编译的时期，自动生成get set方法，并不影响运行，如果觉得提示错误难受，请自行下载lombok包插件，[lombok官网](http://projectlombok.org/)

* ### 为什么我运行demo工程，找不到applicationContent.xml呢？
  ##### 答：请设置项目的资源文件夹。

* ### 为什么我启动tcc-admin项目的时候，会报mongo 集群连接错误呢？
  ##### 答：这是因为项目里面有mongo代码，spring boot会自动配置，该错误没有关系，只要admin项目能正常启动就行。
  
  

# Support

 * ###  如有任何问题欢迎加入QQ群进行讨论
   ![](https://yu199195.github.io/images/qq.png)

 # Contribution
