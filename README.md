Hmily
================
[![Total lines](https://tokei.rs/b1/github/yu199195/hmily?category=lines)](https://github.com/yu199195/hmily)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license)](https://github.com/yu199195/hmily/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.dromara/hmily.svg?label=maven%20central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.dromara%22%20AND%20hmily)
[![Javadocs](http://www.javadoc.io/badge/org.dromara/hmily-aop.svg)](http://www.javadoc.io/doc/org.dromara/hmily)
[![Build Status](https://travis-ci.org/yu199195/hmily.svg?branch=master)](https://travis-ci.org/yu199195/hmily)

### 高性能分布式事务tcc开源框架。基于java语言来开发（JDK1.8），支持dubbo，springcloud,motan等rpc框架进行分布式事务。

 # Features

 * **框架特性**
 
     * ##### 支持嵌套事务(Nested transaction support).

     * ##### 采用disruptor框架进行事务日志的异步读写，与RPC框架的性能毫无差别。

     * ##### 支持SpringBoot-starter 项目启动，使用简单。

     * ##### RPC框架支持 : dubbo,motan,springcloud。

     * ##### 本地事务存储支持 : redis,mongodb,zookeeper,file,mysql。

     * ##### 事务日志序列化支持 ：java，hessian，kryo，protostuff。

     * ##### 采用Aspect AOP 切面思想与Spring无缝集成，天然支持集群。

     * ##### 内置经典的分布式事务场景demo工程，并有swagger-ui可视化界面可以快速体验。

#  为什么高性能？
  
  * ## https://mp.weixin.qq.com/s/Eh9CKTU0nwLZ1rl3xmaZGA
  
#  源码解析博客

  * ## https://yu199195.github.io/categories/hmily-hmily/


#  视频详解

  * ## 环境搭建以及运行 : http://www.iqiyi.com/w_19rwkrfu69.html#vfrm=16-1-1-1
  * ## 源码详解以及调试 : http://www.iqiyi.com/w_19rwkreutt.html


# Prerequisite

  *   #### JDK 1.8+

  *   #### Maven 3.2.x

  *   #### Git

  *   ####  RPC framework dubbo or motan or springcloud。


# Spring-Boot-Starter-Support

   * ### [Spring-Boot-Starter](https://github.com/yu199195/hmily/wiki/Spring-Boot-Starter)


# TCC原理介绍

  * ###  https://github.com/yu199195/hmily/wiki/Theory

#   Configuration

  * ###  https://github.com/yu199195/hmily/wiki/Configuration


# Quick Start

 * #### Clone & Build
   ```
   > git clone https://github.com/yu199195/hmily.git

   > cd hmily

   > mvn -DskipTests clean install -U
   ```

* #### execute this sql       
    https://github.com/yu199195/hmily/blob/master/hmily-hmily-demo/sql/hmily-demo.sql

* #### Find the RPC framework that works for you
    https://github.com/yu199195/hmily/tree/master/hmily-hmily-demo
* ### [Dubbo-Quick-Start](https://github.com/yu199195/hmily/wiki/Dubbo-Quick-Start)

* ### [SpringCloud-Quick-Start](https://github.com/yu199195/hmily/wiki/SpringCloud-Quick-Start)





# User Guide

* #### 关于jar包引用问题，现在jar包还未上传到maven的中央仓库，所以使用者需要自行获取代码，然后打包上传到自己maven私服

   ```
   > git clone https://github.com/yu199195/hmily.git

   > mvn -DskipTests clean deploy -U
   ```
* #### 关于jar包版本问题 ，现在因为没有传到中央仓库，如果引用的话，请自行设置相应的版本。


*  ## [Dubbo User Guide](https://github.com/yu199195/hmily/wiki/Dubbo-User-Guide)

*  ## [SpringCloud User Guide](https://github.com/yu199195/hmily/wiki/SpringCloud-User-Gruid)



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


 * ###  微信公众号
   ![](https://yu199195.github.io/images/public.jpg)

 # Contribution
