Hmily
================
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2f0a0191b02448e6919aca6ce12a1584)](https://app.codacy.com/app/Dromara/hmily?utm_source=github.com&utm_medium=referral&utm_content=Dromara/hmily&utm_campaign=Badge_Grade_Settings)
[![Total lines](https://tokei.rs/b1/github/Dromara/hmily?category=lines)](https://github.com/yu199195/hmily)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license)](https://github.com/Dromara/hmily/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.dromara/hmily.svg?label=maven%20central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.dromara%22%20AND%20hmily)
[![QQ群](https://img.shields.io/badge/chat-on%20QQ-ff69b4.svg?style=flat-square)](https://shang.qq.com/wpa/qunwpa?idkey=2e9e353fa10924812bc58c10ab46de0ca6bef80e34168bccde275f7ca0cafd85)

#### 分布式事务解决方案全景图
 ![](https://yu199195.github.io/images/hmily/hmily.png) 

#  功能
   
   *  支持 `Dubbo`, `SpringCloud`,`Montan` 等RPC框架进行分布式事务
   
   *  支持事务异常回滚，超时异常恢复，防止事务悬挂
   
   *  支持嵌套事务，支持RPC嵌套调用
   
   *  提供零侵入的`spring namespace`, `springboot` 快速集成方式, 简单易用
   
   *  事务日志存储支持 `mysql`, `oracle`, `mongodb`, `redis`, `zookeeper` 等方式
   
   *  高性能，支持微服务集群部署
   
   *  提供后台管理可视化,以及metrics相关性能监控
   

# 必要前提 

  * 必须使用 `JDK8+` 
  
  * 必须要使用一款 `RPC` 框架, 比如 : `Dubbo`, `SpringCloud`,`Montan`
  

# TCC模式

 ![](https://yu199195.github.io/images/hmily/hmily-tcc.png) 
 
   当使用`TCC`模式的时候,用户根据自身业务需求提供 `try`, `confirm`, `cancel` 等三个方法，
   并且 `confirm`, `cancel` 方法由自身完成实现，框架只是负责来调用，来达到事务的一致性。

# TAC模式  

   当用户使用`TAC`模式的时候，用户必须使用关系型数据库来进行业务操作，框架会自动生成`回滚SQL`,
   当业务异常的时候，会执行`回滚SQL`来达到事务的一致性。
   
# 文档

   如果你想使用，你可以参考[Quick Start](http://dromara.org/website/zh-cn/docs/hmily/index.html)   
  
# 关于Hmily 
    
   Hmily是柔性分布式事务解决方案，提供了`TCC` 与 `TAC` 模式。
   
   它以零侵入以及快速集成方式能够方便的被业务进行整合。
   
   在性能上，日志存储异步（可选）以及使用异步执行的方式，不损耗业务方法方法。
   
   之前是由我个人开发，目前在已经重新启动未来将会是金融场景的分布式事务解决方案。

  
# 关注趋势

[![Stargazers over time](https://starchart.cc/yu199195/hmily.svg)](https://starchart.cc/yu199195/hmily) 
 
# 用户墙
 
# 支持

  ![](https://yu199195.github.io/images/qq.png)    ![](https://yu199195.github.io/images/public.jpg)
 



