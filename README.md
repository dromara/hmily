Hmily
================

### 高性能分布式事务tcc方案开源框架。基于java语言来开发（JDK1.8）,支持dubbo，springcloud,motan等rpc框架进行分布式事务。
 
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


# 官网

 ## http://dromara.org  或者 https://dromara.org 有时候https打不开。
 

# 文档 
 
 ##  http://dromara.org/website/zh-cn/docs/hmily/index.html

#  为什么高性能？
  
  * ## https://mp.weixin.qq.com/s/Eh9CKTU0nwLZ1rl3xmaZGA
  

#  视频详解

  * ## 环境搭建以及运行 : http://www.iqiyi.com/w_19rwkrfu69.html#vfrm=16-1-1-1
 
  * ## 源码详解以及调试 : http://www.iqiyi.com/w_19rwkreutt.html


# FAQ

* ### 为什么我下载的代码后，用idea打开没有相应的get set 方法呢？
   ##### 答：因为框架使用了Lombok包，它是在编译的时期，自动生成get set方法，并不影响运行，如果觉得提示错误难受，请自行下载lombok包插件，[lombok官网](http://projectlombok.org/)

* ### 为什么我运行demo工程，找不到applicationContent.xml呢？
  ##### 答：请设置项目的资源文件夹。

* ### 为什么我启动hmily-admin项目的时候，会报mongo 集群连接错误呢？
  ##### 答：这是因为项目里面有mongo代码，spring boot会自动配置，该错误没有关系，只要admin项目能正常启动就行。


# Support

 * ###  如有任何问题欢迎加入QQ群进行讨论
   ![](https://yu199195.github.io/images/qq.png)


 * ###  微信公众号
   ![](https://yu199195.github.io/images/public.jpg)

 # Contribution
