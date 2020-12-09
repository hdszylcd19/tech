# Am命令

> 参考资料： [Am命令用法](http://gityuan.com/2016/02/27/am-command/)

比如，启动包名为com.xh.cwprecorder，主Activity为`.MainActivity`，且extra数据以”coursePath”为key, “/storage/emulated/0/卫青霍去病上22.cwp”为value。通过java代码要完成该功能虽然不复杂，但至少需要一个android环境，而通过adb的方式，只需要在adb窗口，输入如下命令便可完成:

```java
adb shell am start -n  com.xh.cwprecorder/.MainActivity --es coursePath "/storage/emulated/0/卫青霍去病上22.cwp" --es callbackUrl "http://127.0.0.1/complete" --es deviceId "5200c5144653c5eb"
```

