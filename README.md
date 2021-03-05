<p align="center" >
    <a href="https://dromara.org"><img src="https://yu199195.github.io/images/hmily/hmily-logo.png" width="45%"></a>
</p>
<p align="center">
  <strong>Financial-level flexible distributed transaction solution</strong>
</p>
<p align="center">
  <a href="https://dromara.org">https://dromara.org/</a>
</p>

<p align="center">
  English | <a href="https://github.com/dromara/hmily/blob/master/README_CN.md">简体中文</a>
</p>

<p align="center">
    <a target="_blank" href="https://search.maven.org/search?q=g:org.dromara%20AND%20hmily">
        <img src="https://img.shields.io/maven-central/v/org.dromara/hmily.svg?label=maven%20central" />
    </a>
    <a target="_blank" href="https://github.com/Dromara/hmily/blob/master/LICENSE">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license" />
    </a>
    <a target="_blank" href="https://app.codacy.com/app/Dromara/hmily?utm_source=github.com&utm_medium=referral&utm_content=Dromara/hmily&utm_campaign=Badge_Grade_Settings">
         <img src="https://api.codacy.com/project/badge/Grade/2f0a0191b02448e6919aca6ce12a1584" />
    </a>
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
        <img src="https://img.shields.io/badge/JDK-8+-green.svg" />
    </a>
    <a target="_blank" href="https://github.com/dromara/hmily">
        <img src="https://github.com/dromara/hmily/workflows/build/badge.svg" />
    </a>
    <a href="https://codecov.io/gh/dromara/hmily">
        <img src="https://codecov.io/gh/dromara/hmily/branch/master/graph/badge.svg"/>
    </a>
    <a target="_blank" href='https://gitee.com/dromara/hmily/stargazers'>
        <img src='https://gitee.com/dromara/hmily/badge/star.svg?theme=gvp' alt='gitee stars'/>
    </a>
    <a target="_blank" href='https://github.com/dromara/hmily'>
        <img src="https://img.shields.io/github/forks/dromara/hmily.svg" alt="github forks"/>
    </a>
    <a target="_blank" href='https://github.com/dromara/hmily'>
        <img src="https://img.shields.io/github/stars/dromara/hmily.svg" alt="github stars"/>
    </a>
    <a target="_blank" href='https://github.com/dromara/hmily'>
        <img src="https://img.shields.io/github/contributors/dromara/hmily.svg" alt="github contributors"/>
    </a>   
   <a href="https://github.com/Dromara/hmily">
        <img src="https://tokei.rs/b1/github/Dromara/hmily?category=lines"/>
   </a>
</p>
<br/>

-------------------------------------------------------------------------------

# Panorama of distributed transaction solutions
 ![](https://yu199195.github.io/images/hmily/hmily.png) 

-------------------------------------------------------------------------------

#  Features

   *  high reliability ：supports abnormal transaction rollback in distributed scenarios, and abnormal recovery over time to prevent transaction suspension
   
   *  usability ：provide zero-invasive `Spring-Boot`, `Spring-Namespace` to quickly integrate with business systems
   
   *  high performance ：decentralized design, fully integrated with business systems, naturally supporting cluster deployment
   
   *  observability ：metrics performance monitoring of multiple indicators, and admin management background UI display
   
   *  various RPC ： support `Dubbo`, `SpringCloud`, `Motan`, `Sofa-rpc`, `brpc`, `tars` and other well-known RPC frameworks
   
   *  log storage ： support `mysql`, `oracle`, `mongodb`, `redis`, `zookeeper` etc.
   
   *  complex scene ： support RPC nested call transaction

-------------------------------------------------------------------------------

# Necessary premise 

  * must use `JDK8+` 
  
  * TCC mode must use a `RPC` framework, such as: `Dubbo`, `SpringCloud`, `Montan`

-------------------------------------------------------------------------------

# TCC mode

 ![](https://yu199195.github.io/images/hmily/hmily-tcc.png) 

   when using the `TCC` mode, users provide three methods: `try`, `confirm`, and `cancel` according to their business needs.
    And the `confirm` and `cancel` methods are implemented by themselves, and the framework is only responsible for calling them to achieve transaction consistency。

-------------------------------------------------------------------------------

# TAC mode  
   ![](https://yu199195.github.io/images/hmily/hmily-tac.png) 

   When the user uses the `TAC` mode, the user must use a relational database for business operations, and the framework will automatically generate a `rollback SQL`,
    When the business is abnormal, the `rollback SQL` will be executed to achieve transaction consistency。

-------------------------------------------------------------------------------

# Documentation
[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://dromara.org/en-us/docs/hmily/index.html)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://dromara.org/zh-cn/docs/hmily/index.html)

If you want to use it, you can refer to [Quick Start](https://dromara.org/en-us/docs/hmily/index.html)

# About Hmily 

   Hmily is a flexible distributed transaction solution that provides `TCC` and `TAC` modes。

   It can be easily integrated by business with zero intrusion and rapid integration。

   In terms of performance, log storage is asynchronous (optional) and asynchronous execution is used, without loss of business methods。

   It was previously developed by me personally. At present, I have restarted at JD Digital. The future will be a distributed transaction solution for financial scenarios.。

-------------------------------------------------------------------------------
# Follow the trend

[![Stargazers over time](https://starchart.cc/yu199195/hmily.svg)](https://starchart.cc/yu199195/hmily) 

-------------------------------------------------------------------------------
# User wall


# Support

  ![](https://yu199195.github.io/images/qq.png)    ![](https://yu199195.github.io/images/public.jpg)




