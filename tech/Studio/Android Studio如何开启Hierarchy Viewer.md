# Android Studio如何开启Hierarchy Viewer

不知道大伙有没有这样一种经历：以前在使用Eclipse作为主力开发工具时，会经常使用Hierarchy Viewer来查看布局层级关系，调试一些疑难杂症；也可以查看世面上优秀APP的布局信息，堪称神器！但是，自从转到Studio之后，这个神器经常开启失败，导致雪藏至今......

**真机需要root权限！** **真机需要root权限！** **真机需要root权限！**

### 一、下载mprop工具

下载地址：链接：https://pan.baidu.com/s/1XTZQa6-0gMWBPIpwnlli8w 密码：9brx

### 二、将对应cpu架构下的mprop文件复制到data目录

### 三、打开cmd命令行执行如下命令

```
#  adb shell 
#  cd data
#  chmod 777 mprop //更改mprop文件权限
# ./mprop ro.debuggable 1 //修改值为1；修改后可使用getprop ro.debuggable查看是否修改成功
#  adb kill-server //关闭adb服务
#  adb start-server //重启adb服务
```

经过一波操作过后，即可开启神器。