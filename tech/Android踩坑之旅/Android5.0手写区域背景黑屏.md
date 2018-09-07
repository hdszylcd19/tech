### 问题描述

在开发题型组件的过程中，一直存在一个非常**"难以捉摸"**的bug......尝试了各种方式，一直没有很好地解决掉......

在Android5.0（API版本21）中，在手写区域的地方有一定几率出现背景变黑的情况；在6.0、7.0和8.0版本中，该问题则没有复现......通过观察发现：貌似出现黑底的情况，都是出现在列表中滑动时；当题目单独展示时，则没有发现该问题；

### 问题追踪

在刚发现该问题时，一直怀疑该问题是覆盖在手写区域上的手写控件造成的（其实并不是......手写控件一直背着这口锅负重前行）；一度通过关闭手写控件的硬件加速选项，确实黑屏问题的几率大大降低啦！基本上很少会再出现啦；虽然偶尔在题目比较多时，还有一定几率会再出现，但已经不影响正常使用啦......

自此，一直认为：该问题是因为应用本身的内存不足，手写控件丢失了透明度信息，导致背景变黑啦......（手写控件默默地擦干了屈辱的泪水，继续负重前行......）

最近一段时间，该问题又出现了好几次，确实比较棘手；所以，痛定思痛；决心花大力气来尝试彻底根治黑底的问题；

网上的关于这方面的信息少之又少，尝试了各种方式：好像解决啦？好像又复现啦？一直在痛苦的深渊中挣扎......

后来有同事提醒说：好像同样有手写区域的题干部分就从来没有变黑过！！！哎，你还别说，还真是这样呢......一语惊醒梦中人的感觉！又有新的尝试方向啦！

题干区域布局信息如下：

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingLeft="@dimen/qt_dp_10"
    android:paddingRight="@dimen/qt_dp_10">

    <ViewStub
        android:id="@id/qt_web_view_vs"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout="@layout/qt_stem_webview"/>

    <ViewStub
        android:id="@id/qt_text_view_vs"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout="@layout/qt_stem_textview"/>

    <ImageView
        android:id="@+id/qt_stem_img"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

    <com.zhitongyunle.xh_handwriting.HandWritingView
        android:id="@+id/qt_stem_HandWritingView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
</FrameLayout>
```

学生回答手写区域布局信息如下：

```
<FrameLayout
            android:id="@id/qt_write_root_fl"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/qt_handwriting_repeat_bg">

            <com.zhitongyunle.xh_handwriting.HandWritingView
                android:id="@+id/qt_hand_writing_hwv"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:minHeight="@dimen/qt_completion_sub_min_height"/>

            <!--省略部分代码-->
          
 </FrameLayout>
```

有啥区别么？好像都是一模一样的吧！你要非要说区别的话：无非就是题干区域的FrameLayout没有在布局文件中设置背景而已！这有影响么？（你还别说，还真有影响......）

学生回答手写区域的background信息如下：

```
<?xml version="1.0" encoding="utf-8"?>
<bitmap
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@mipmap/qt_ic_handwriting_bg"
    android:tileMode="repeat"/>
```

**当我把这个背景去掉之后，神奇的一幕出现啦！一直变黑的问题居然修复啦！！！！！！**

这种用法存在有什么问题么？这种用法之前也一直在用呀，怎么问题会出在这里呢？难道是Android5.0留下的坑么？这还不去读一波源码么？

### 解决方案

既然问题是因为这个原因产生的，那么，为了实现类似于“作业本”的显示效果，就只能换一种实现方式啦！

自定义FrameLayout来画出类似于“作业本”的显示效果；代码如下：

```
public class QTHandWritingBgFrameLayout extends FrameLayout {
    private static final String TAG = "QTHandWritingFrameLayout";
    private int mWidth;
    private int mHeight;
    private int mLineSpacing; //分割线间距
    private int mPadding;
    private Paint mPaint;

    public QTHandWritingBgFrameLayout(Context context) {
        this(context, null);
    }

    public QTHandWritingBgFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QTHandWritingBgFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);

        int strokeWidth = getResources().getDimensionPixelSize(R.dimen.qt_handwriting_bg_stroke_width);
        mLineSpacing = getResources().getDimensionPixelSize(R.dimen.qt_handwriting_bg_line_spacing);
        mPadding = getResources().getDimensionPixelSize(R.dimen.qt_handwriting_bg_padding);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(Color.parseColor("#f1f1f1"));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0) {
            return;
        }

        if (IS_DEBUG) {
            XHLog.i(TAG, "mWidth = [" + mWidth + "], mHeight = [" + mHeight + "]");
        }

        int num = mHeight / mLineSpacing;
        for (int i = 1; i <= num; i++) {
            int y = i * mLineSpacing;
            canvas.drawLine(mPadding, y, mWidth - mPadding, y, mPaint);
        }
    }
}
```

### 手写控件背过的那些锅

**手写区域黑底**、**OOM**、**内存泄漏**......