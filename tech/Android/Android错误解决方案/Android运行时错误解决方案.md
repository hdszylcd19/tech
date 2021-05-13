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

## Bitmap保存到本地背景变黑

### 问题描述

> Studio版本：v4.0.2
>
> Android版本：API28

当通过手写控件获取笔迹生成的Bitmap后，把Bitmap保存到本地后，背景变成了黑色，丢失了透明度。

获取Bitmap方法如下：

```java

/**
 * 获取手写笔迹Bitmap
 *
 * @param config            Bitmap配置
 * @param isCutStrokeHeight 当该值为true时，截取有效笔迹高度；为false时，生成的bitmap为原始高度
 */
public synchronized Bitmap getBitmap(@NonNull Bitmap.Config config, boolean isCutStrokeHeight) {
    Bitmap strokeBitmap;
    
    //...省略部分无关代码

    Canvas canvas = new Canvas(strokeBitmap);
    strokeBitmap.eraseColor(Color.TRANSPARENT); //把bitmap所有像素点填充为透明
    drawCachedHWC(canvas);
    return strokeBitmap;
}
```

Bitmap保存到本地方法如下：

```kotlin
//图片保存路径savePath = /storage/emulated/0/strokes/temp//20210422_200233_645.png
suspend fun doSaveBitmap(src: Bitmap, savePath: String, quality: Int = 100,
                         format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
                         recycle: Boolean = true, liveData: MutableLiveData<BitmapSaveResult>) {
    withContext(Dispatchers.IO) {
        if (!XHFileUtil.createOrExistsFile(savePath)) {
            val errorMsg = "创建文件失败"
            LogUtils.e(errorMsg)
            /*失败回调*/
            liveData.postValue(BitmapSaveResult(savePath = savePath, errorMsg = errorMsg))
            return@withContext
        }

        var success = false
        var errorMsg = ""
        var os: OutputStream? = null
        try {
            os = BufferedOutputStream(FileOutputStream(File(savePath)))
            LogUtils.i("开始保存图片...")
            success = src.compress(format, quality, os)
            if (recycle && !src.isRecycled) src.recycle()
        } catch (e: Exception) {
            errorMsg = "写入文件失败"
            LogUtils.e(errorMsg)
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        liveData.postValue(BitmapSaveResult(success, savePath, errorMsg))
    }
}
```

经过对比发现，保存图片到本地的文件为`.png`格式，而`doSaveBitmap()`默认使用的图片压缩格式为`JPEG`。

### 解决方案

修改`doSaveBitmap()`默认使用的图片压缩格式为`PNG`。

## assets目录文件打开失败

### 问题描述

> Studio版本：v4.0.2
>
> Android版本：API28

在做手写识别项目时，需要从资产目录中加载`tensorflow`识别模型`mnist.tflite`文件。遇到了如下错误：

```java
2021-05-13 11:01:05.056 21748-21748/com.xh.recognition_score_demo E/HandWritingModel: Error to setting up digit classifier.
    java.io.FileNotFoundException: This file can not be opened as a file descriptor; it is probably compressed
        at android.content.res.AssetManager.nativeOpenAssetFd(Native Method)
        at android.content.res.AssetManager.openFd(AssetManager.java:820)
        at com.xh.recognition_score_demo.digitclassifier.DigitClassifier.loadModelFile(DigitClassifier.java:89)
        at com.xh.recognition_score_demo.digitclassifier.DigitClassifier.initializeInterpreter(DigitClassifier.java:68)
        at com.xh.recognition_score_demo.digitclassifier.DigitClassifier.access$000(DigitClassifier.java:29)
        at com.xh.recognition_score_demo.digitclassifier.DigitClassifier$1.run(DigitClassifier.java:55)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
        at java.lang.Thread.run(Thread.java:764)
```

该错误的描述原因为：**该文件不能作为文件描述符打开，它可能被压缩了!**，猜测可能是gradle的配置有问题。

### 解决方案

在对应模块下的`build.gradle`文件中，添加如下配置：

```java
android {

    //...省略无关配置

    //添加如下配置，不压缩tflite文件
    aaptOptions {
        noCompress "tflite"
    }
}
```

> 参考链接：
>
> [This file can not be opened as a file descriptor](https://stackoverflow.com/questions/6186866/java-io-filenotfoundexception-this-file-can-not-be-opened-as-a-file-descriptor) 