## 本地ide的启动方法
- 首先要确保当前环境存在tars平台并且tarsregistry(tars的注册中心)正常启动
- 按照[该文](https://tarscloud.github.io/TarsDocs/resources-sharing/pdf/TarsJava服务端的本地调试--正风.pdf)修改项目resources目录下的conf文件
- 在启动参数中增加-Dconfig=上一步修改的文件地址 正常启动