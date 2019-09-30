
#   测试java 蓝牙服务案例
##  安装java
##  进入测试代码的目录
##  用 javac -cp .\lib\bluecove-2.1.1-SNAPSHOT.jar;.\lib\commons-io-2.5.jar -encoding utf-8 .\BluetoothJavaServer.java编译代码会生成.class文件,记得这条命令以及下一条命令一定要在windows cmd中执行,其它的例如powershell会无效
##  用java -cp .\lib\bluecove-2.1.1-SNAPSHOT.jar;.\lib\commons-io-2.5.jar  .\BluetoothJavaServer.java 启动蓝牙服务,记得打开电脑的蓝牙并且设置蓝牙可被发现(不然手机是搜不到的,亲身经历)


#   手机端apk使用
##  手机安装apk
##  在指定地方填写蓝牙目标名称和uuid
##  点击蓝牙按钮即可开始搜索配对连接指定蓝牙目标设备
##  显示ready即可使用

##  人脸识别
##  在人脸认证按钮上方输入设备ip与port点击按钮即可使用


#   android porject
##  install android studio
##  打开mentor项目
