# java-jdbc

完全不依赖IDE的java脚本程序,实现了jdbc连接sqlserver数据库，图片裁剪，文件读写等

使用方法：
``` bash
javac -cp ./mssql-jdbc-6.2.1.jre7.jar; ./im4java-1.4.0.jar DealCardImg200.java
java -Djava.ext.dirs=./ DealCardImg200
```

linux下多个jar包用冒号分隔


小坑：
目录下包含了jar包sunjce_provider.jar，该jar拷贝自
