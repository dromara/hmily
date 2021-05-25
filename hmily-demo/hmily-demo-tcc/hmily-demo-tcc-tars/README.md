# 环境准备
- JDK 1.8+
- Maven 3.2.x
- Git
- Tars

# 代码拉取
```
  > git clone https://github.com/dromara/hmily.git

  > cd hmily

  > mvn -DskipTests clean install -U
```

# 执行demo 模块的sql语句。
[sql语句] (https://github.com/dromara/hmily/blob/master/hmily-demo/sql/hmily-demo.sql)

# 建立tars节点
根据[此文](https://tarscloud.github.io/TarsDocs/dev/tarsjava/tars-quick-start.html)在当前tars平台建立       
- 应用名:TestInventory,服务名称:InventoryApp,Obj名:InventoryObj,端口29740的节点。        
- 应用名:HmilyAccount,服务名称:AccountApp,Obj名:AccountObj,端口10386的节点。         
在完成节点的建立后，分别到hmily-demo-tars-springboot-account和hmily-demo-tars-springboot-inventory目录下执行mvn clean package命令打包并按照[此文](https://tarscloud.github.io/TarsDocs/dev/tarsjava/tars-quick-start.html)在两个前面建立的节点上使用打包的成果物进行节点发布。     

# 使用你的工具 idea 打开项目，找到hmily-demo-tars-springboot项目。
## 修改项目配置（hmily-demo-tars-springboot-account为列子）
- application.yml 下修改业务数据库     
```
spring:
    datasource:
        driver-class-name:  com.mysql.jdbc.Driver
        url: jdbc:mysql://改成你的ip+端口/hmily_account?useUnicode=true&characterEncoding=utf8
        username:  #改成你的用户名
        password:  #改成你的密码
```
- 修改 hmily.yml,这里使用mysql来存储     
```
repository:
  database:
    driverClassName: com.mysql.jdbc.Driver
    url : jdbc:mysql://改成你的ip+端口/hmily?useUnicode=true&characterEncoding=utf8
    username: root #改成你的用户名
    password: #改成你的密码
```
- 将rescouces目录下的config.conf后缀文件里的192.168.41.102全局替换成tars平台ip,并在启动参数中添加-Dconfig=该文件的路径

- run TarsHmilyAccountApplication.java

## 启动hmily-demo-tars-springboot-inventory 参考上述。

## 启动hmily-demo-tars-springboot-order 参考上述。

访问：http://127.0.0.1:18087/swagger-ui.html。
