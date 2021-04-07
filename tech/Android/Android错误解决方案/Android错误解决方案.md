## Attempted to finish an input event but the input event receiver has already been disposed.

### 问题描述

> Studio版本：v4.0.2
>
> Android版本：API28

当在封装XHPPTViewer时，在logcat中看到了如下警告信息：

```java
Attempted to finish an input event but the input event receiver has already been disposed.
// 试图完成一个输入事件，但该输入事件接收器已被处置。
```

具体现象为，第一次或前几次点击PPT时，并没有响应触摸事件，并且会打印上述警告信息。

经过查阅资料和调试后发现，引起该警告信息的原因，是Demo中创建的`ProgressDialog`调用了`hide()`方法，并没有调用`dismiss()`方法。

```kotlin
		val progress = ProgressDialog(this)
        progress.setTitle("文件下载中")
        val config = PPTConfig.ConfigBuilder()
            .setDownloadUrl(downUrl)
            .setSaveZipPath("${SDCARD}/zip/${fileName}.7z")
            .setSavePPTPath("${SDCARD}/zip/ppt/${fileName}/")
            .setOnListener(object : PPTConfig.OnListener {
                override fun onProcess(step: String) {
                    runOnUiThread {
                        when (step) {
                            "Start" -> {
                                progress.show()
                            }
                            "Download" -> {

                            }
                            "UnZip" -> {

                            }
                            "Show" -> {
                                // 错误原因
                                progress.hide()
                            }
                        }
                    }
                }

                override fun onFail(e: Exception) {
                    runOnUiThread {
                        // 错误原因
                        progress.hide()
                    }
                }

            }).build()
```

### 解决方案

当`ProgressDialog`不使用时，调用`dismiss()`方法，而不是`hide()`方法。