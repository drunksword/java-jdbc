# java-jdbc

完全不依赖IDE的java脚本程序,实现了jdbc连接sqlserver数据库，图片裁剪，文件读写等。<br>
更新了连接postgresql的方法，依赖postgresql-42.1.4.jar。代码修改为相应的注释部分

### 使用方法：
``` bash
javac -cp ./mssql-jdbc-6.2.1.jre7.jar; ./im4java-1.4.0.jar DealCardImg200.java
java -Djava.ext.dirs=./ DealCardImg200
```

linux下多个jar包用冒号分隔


### 小坑：
目录下包含了jar包sunjce_provider.jar，该jar拷贝自{JAVA_HOME}\jre\lib\ext，而在jre中的这个包就会抛出异常。
那么就干脆用jdk中的这个包替换掉jre中的这个包

### 最后
有了这个经验，再也不用写jsp运行占用tomcat资源啦，也不用把臃肿的项目全部依赖掉啦
