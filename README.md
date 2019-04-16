Hmily
================
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2f0a0191b02448e6919aca6ce12a1584)](https://app.codacy.com/app/yu199195/hmily?utm_source=github.com&utm_medium=referral&utm_content=yu199195/hmily&utm_campaign=Badge_Grade_Settings)
[![Total lines](https://tokei.rs/b1/github/yu199195/hmily?category=lines)](https://github.com/yu199195/hmily)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license)](https://github.com/yu199195/hmily/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.dromara/hmily.svg?label=maven%20central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.dromara%22%20AND%20hmily)
[![Build Status](https://travis-ci.org/yu199195/hmily.svg?branch=master)](https://travis-ci.org/yu199195/hmily)
[![QQ群](https://img.shields.io/badge/chat-on%20QQ-ff69b4.svg?style=flat-square)](https://shang.qq.com/wpa/qunwpa?idkey=2e9e353fa10924812bc58c10ab46de0ca6bef80e34168bccde275f7ca0cafd85)
### Open source framework for high-performance distributed transaction TCC solution.Based on Java language development (JDK1.8), support dubbo, springcloud,motan and other RPC framework for distributed transactions.


# Modules

  * hmily-admin: Transaction log management background
  
  * hmily-annotation : Framework common annotations
  
  * hmily-apache-dubbo : Support for the dubbo rpc framework 2.7.X

  * hmily-common :  Framework common class
  
  * hmily-core : Framework core package (annotation processing, log storage...)              
  
  * hmily-dashboard : Management background front-end
  
  * hmily-dubbo : Support for the dubbo framework Less than 2.7 version
  
  * hmily-motan : Support for the motan rpc framework
  
  * hmily-springcloud : Support for the spring cloud rpc framework
  
  * hmily-spring-boot-starter : Support for the spring boot starter
  
  * hmily-demo : Examples using the hmily framework
 
#  Features
   
   *  All spring versions are supported and Seamless integration
   
   *  Provides support for the springcloud dubbo motan RPC framework
   
   *  Provides integration of the spring boot starter approach
   
   *  Support Nested transaction 
   
   *  Local transaction storage support :  redis mongodb zookeeper file mysql
   
   *  Transaction log serialization support : java hessian kryo protostuff
   
   *  Spi extension : Users can customize the storage of serialization and transaction logs

  
# About 

  #####  Hmily is a TCC solution for distributed transactions, Its rapid integration, zero penetration high performance has been run by a number of companies including my own company in the production environment
    
  #####  Its performance is nearly lossless compared to your RPC framework, its confrim cancel, and its log store is conducted asynchronously using a disruptor.
  
  #####  If you want to use it or get a quick look at it [!Quick Start](http://dromara.org/website/zh-cn/docs/hmily/index.html)
  
 
# Stargazers over time

[![Stargazers over time](https://starchart.cc/yu199195/hmily.svg)](https://starchart.cc/yu199195/hmily) 
 

# Support

 * ![](https://yu199195.github.io/images/qq.png) 
 
 * ![](https://yu199195.github.io/images/public.jpg)


