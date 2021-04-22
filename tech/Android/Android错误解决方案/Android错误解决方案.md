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

## ViewModel初始化错误

### 问题描述

> Studio版本：v4.0.2
>
> Android版本：API28

在`Activity`中初始化`ViewModel`时，遇到了如下错误：

```kotlin
//编译期错误：None of the following functions can be called with the arguments supplied.
// 大意为：使用提供的参数不能调用以下任何函数，也就是说参数不匹配咯
private val bitmapViewModel by lazy {
    ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(BitmapViewModel::class.java)
}
```

`ViewModelProvider`的构造方法如下：

```kotlin
public ViewModelProvider(@NonNull ViewModelStoreOwner owner, @NonNull Factory factory)

public ViewModelProvider(@NonNull ViewModelStore store, @NonNull Factory factory)
```

经过一番查找发现，在该项目中使用的`support`库版本为：

```java
implementation 'com.android.support:appcompat-v7:26.1.0'
```

在`v7:26.1.0`版本中的`FragmentActivity`声明如下：

```java
public class FragmentActivity extends BaseFragmentActivityApi16 implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        ActivityCompat.RequestPermissionsRequestCodeValidator {...}
```

我们发现`FragmentActivity`并没有实现`ViewModelStoreOwner`接口，所以才会报错！

### 解决方案

提升`support`库版本为：`28.0.0`！或直接使用`Androidx`库。

在`v7:28.0.0`版本中的`FragmentActivity`声明如下：

```java
public class FragmentActivity extends SupportActivity implements ViewModelStoreOwner, OnRequestPermissionsResultCallback, RequestPermissionsRequestCodeValidator {...}
```

