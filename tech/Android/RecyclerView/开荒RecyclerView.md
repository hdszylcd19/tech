# 开荒RecyclerView

基于`androidx.recyclerview:recyclerview:1.0.0`版本，进行开荒。

RecyclerView作为一个自定义View，我们先来看一下它的继承结构：

```java
public class RecyclerView extends ViewGroup implements ScrollingView, NestedScrollingChild2 {
	...
}
```

我们发现，

作为一个自定View，自然也离不开测量、布局、绘制三大步骤。那么，就先从这三个方法开始入手，先摸清RecyclerView大体的整个脉络，看看RecyclerView到底有什么魔力，可以实现这么强大的功能。

在开始分析之前，有几个概念我们需要先梳理一下，以助于我们后续对源码的理解。

#### State

以下是关于State静态内部类的官方注释信息：

```java
	/**
      <p>Contains useful information about the current RecyclerView state like target 			scroll position or view focus. State object can also keep arbitrary data, 				identified by resource ids.</p>
      <p>Often times, RecyclerView components will need to pass information between  			each other. To provide a well defined data bus between components, RecyclerView 		passes the same State object to component callbacks and these components can use 		it to exchange data.</p>
      <p>If you implement custom components, you can use State's put/get/remove methods 		to pass data between your components without needing to manage their lifecycles.	  </p>
     */
    public static class State {
        ......
    }
```

通过翻译官方注释，大概意思如下：

```java
	/ **
      	包含有关当前RecyclerView状态的有用信息，例如目标滚动位置或视图焦点。State对象还可以保留由资源ID	   标识的任意数据。
        
      	通常，RecyclerView组件将需要在彼此之间传递信息。 为了在组件之间提供定义良好的数据总线，			  RecyclerView将相同的State对象传递给组件回调，这些组件可以使用它来交换数据。
        
      	如果实现自定义组件，你可以使用State的 put/get/remove 方法即可在组件之间传递数据，而无需去管理	   它们的生命周期。
      * /
```

我们通过注释信息发现，这个类主要用来保存RecyclerView的各种状态信息。我们又发现mLayoutStep的取值总共有三种：

| mLayoutStep取值       | 含义                                                         |
| :-------------------- | :----------------------------------------------------------- |
| State.STEP_START      | mState.mLayoutStep的默认值，这种情况下，表示RecyclerView还未经历`dispatchLayoutStep1()`，因为`dispatchLayoutStep1()`调用之后mState.mLayoutStep会变为State.STEP_LAYOUT。 |
| State.STEP_LAYOUT     | 当mState.mLayoutStep为State.STEP_LAYOUT时，表示此时处于layout阶段，这个阶段会调用`dispatchLayoutStep2()`方法，内部又会调用LayoutManager的 `onLayoutChildren()`方法。调用`dispatchLayoutStep2()`方法之后，此时mState.mLayoutStep变为了State.STEP_ANIMATIONS。 |
| State.STEP_ANIMATIONS | 当mState.mLayoutStep为State.STEP_ANIMATIONS时，表示RecyclerView处于第三个阶段，也就是执行动画的阶段，也就是调用`dispatchLayoutStep3()`方法。当`dispatchLayoutStep3()`方法执行完毕之后，mState.mLayoutStep又变为了State.STEP_START。 |

从上表中，我们不难发现，其实，mLayoutStep的三种取值和RecyclerView布局的三大步骤有一个对应关系，通过查看各步骤的源码，发现有如下结论：

**`dispatchLayoutStep1()`方法执行时，mLayoutStep的取值必须为State.STEP_START；**

**`dispatchLayoutStep2()`方法执行时，mLayoutStep的取值可以为State.STEP_LAYOUT或State.STEP_ANIMATIONS；**

**`dispatchLayoutStep3()`方法执行时，mLayoutStep的取值必须为State.STEP_ANIMATIONS；**

我们先来看一下传说中的三大步骤大概都干了一些什么事情，先把整个脉络理清，最后再来开荒具体细节。

#### dispatchLayoutStep1()

`dispatchLayoutStep1()`官方注释如下：

```java
	/**
     * The first step of a layout where we;
     * - process adapter updates
     * - decide which animation should run
     * - save information about current views
     * - If necessary, run predictive layout and save its information
     */
    private void dispatchLayoutStep1(){
        ......
    }
```

通过翻译官方注释，我们大概就知道了，在布局三大步骤的第一步主要做了这些事：

- **处理adapter的更新逻辑；**
- **决定执行哪个动画；**
- **保存当前视图的相关信息；**
- **如果有必要的话，会执行预布局并保存其信息。**

`dispatchLayoutStep1()`暂时先分析到这里就足够了，后面我们再详细分析该方法内部的实现细节。

#### dispatchLayoutStep2()

`dispatchLayoutStep2()`官方注释如下：

```java
	/**
     * The second layout step where we do the actual layout of the views for the final 		   state.
     * This step might be run multiple times if necessary (e.g. measure).
     */
    private void dispatchLayoutStep2(){
        ......
    }
```

通过翻译官方注释，我们大概就知道了，在布局三大步骤的第二步主要做了这些事：

- **我们为最终状态进行views的实际布局。**
- **如果有必要（例如测量），此步骤可能被多次调用。**

我们发现，原来在这个方法中才会执行实际的布局逻辑，并且该方法在`onMeasure()`中有可能被多次调用，这个我们在源码中也不难发现。

#### dispatchLayoutStep3()

`dispatchLayoutStep3()`官方注释如下：

```java
	/**
     * The final step of the layout where we save the information about views for 			   animations,
     * trigger animations and do any necessary cleanup.
     */
    private void dispatchLayoutStep3(){
        ......
    }
```

通过官方注释，我们大概知道了，在布局三大步骤的最后一步主要做了这些事：

- **我们将保存有关views动画的信息，触发动画以及进行任何有必要的清理工作。**

我们发现，该方法主要是处理跟动画相关的部分，以及一些必要的清理工作。至此，我们对布局三大步骤已经有一个大致的概念了，等我们把整个RecyclerView的脉络摸清之后，再继续深入细节来体会其设计精妙之处。

## 一、onMeasure()

我们接下来看一下`onMeasure()`方法源码：

```java
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (mLayout == null) {
            // LayoutManager为null
            defaultOnMeasure(widthSpec, heightSpec);
            return;
        }
        if (mLayout.isAutoMeasureEnabled()) {
            // LayoutManager开启自动测量
        } else {
            // LayoutManager未开启自动测量
        }
    }
```

onMeasure()方法内部代码还是比较多的，我们把方法内部梳理为三种情况，以助于我们理解：

- 我们知道，当RecyclerView中的LayoutManager为null时，是不能显示任何数据的。
- LayoutManager开启了自动测量，这是一种情况。为了RecyclerView能够支持wrap_content属性，系统提供的三种LayoutManager（LinearLayoutManager、GridLayoutManager和StaggeredGridLayoutManager）默认都是开启自动测量的。其中，GridLayoutManager又继承自LinearLayoutManager。
- LayoutManager未开启自动测量，这种情况是比较少的，一般自定义LayoutManager的时候可能会用到，不过我们还是要分析一下的。

### 1.1、LayoutManager为null

这种情况比较简单，我们看到源码中调用了 `defaultOnMeasure()`之后，直接返回了。那么我们来看一下`defaultOnMeasure()`方法内部做了什么事情：

```java
	void defaultOnMeasure(int widthSpec, int heightSpec) {
        // calling LayoutManager here is not pretty but that API is already public and 			it is better
        // than creating another method since this is internal.
        final int width = LayoutManager.chooseSize(widthSpec,
                getPaddingLeft() + getPaddingRight(),
                ViewCompat.getMinimumWidth(this));
        final int height = LayoutManager.chooseSize(heightSpec,
                getPaddingTop() + getPaddingBottom(),
                ViewCompat.getMinimumHeight(this));

        setMeasuredDimension(width, height);
    }
```

我们看到，在 `defaultOnMeasure()`内部分别调用了 `LayoutManager.chooseSize()`来分别获取宽和高，最后通过 `setMeasuredDimension()`方法来设置RecyclerView的宽、高尺寸信息。我们来看一下`LayoutManager.chooseSize()`内部是怎么来计算尺寸信息的：

```java
		public static int chooseSize(int spec, int desired, int min) {
            final int mode = View.MeasureSpec.getMode(spec);
            final int size = View.MeasureSpec.getSize(spec);
            switch (mode) {
                case View.MeasureSpec.EXACTLY:
                    return size;
                case View.MeasureSpec.AT_MOST:
                    return Math.min(size, Math.max(desired, min));
                case View.MeasureSpec.UNSPECIFIED:
                default:
                    return Math.max(desired, min);
            }
        }
```

我们发现，`chooseSize()`方法还是比较简单的。其实就是通过RecyclerView的宽、高测量mode来获取不同的尺寸信息，这里就不详细解释了。

#### 小结

- **在RecyclerView测量阶段，当LayoutManager为null时，会直接调用`defaultOnMeasure()`，然后直接返回，结束测量流程。**
- **在`defaultOnMeasure()`内部，根据RecyclerView的宽、高测量mode，又通过调用`LayoutManager.chooseSize()`方法来分别获取宽、高值，最后通过 `setMeasuredDimension()`方法来设置RecyclerView的宽和高。**

### 1.2、LayoutManager开启自动测量

接下来，我们就来分析LayoutManager开启自动测量时的源码：

```java
final int widthMode = MeasureSpec.getMode(widthSpec);
final int heightMode = MeasureSpec.getMode(heightSpec);

/**
  * This specific call should be considered deprecated and replaced with
  * {@link #defaultOnMeasure(int, int)}. It can't actually be replaced as it could
  * break existing third party code but all documentation directs developers to not
  * override {@link LayoutManager#onMeasure(int, int)} when
  * {@link LayoutManager#isAutoMeasureEnabled()} returns true.
  */
mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);

final boolean measureSpecModeIsExactly =
    widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY;
if (measureSpecModeIsExactly || mAdapter == null) {
    return;
}

if (mState.mLayoutStep == State.STEP_START) {
    dispatchLayoutStep1();
}
// set dimensions in 2nd step. Pre-layout should happen with old dimensions for
// consistency
/*在第二步中设置尺寸。为了保持一致性，应当使用旧尺寸信息进行预布局*/
mLayout.setMeasureSpecs(widthSpec, heightSpec);
mState.mIsMeasuring = true;
dispatchLayoutStep2();

// now we can get the width and height from the children.
// 现在我们就可以从子view那里获取宽度和高度了。
mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);

// if RecyclerView has non-exact width and height and if there is at least one child
// which also has non-exact width & height, we have to re-measure.
// 如果RecyclerView的宽、高是不精确的，并且如果至少有一个子View的宽、高度也是不精确的，我们必须重新测量。
if (mLayout.shouldMeasureTwice()) {
    mLayout.setMeasureSpecs(
        MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
    mState.mIsMeasuring = true;
    dispatchLayoutStep2();
    // now we can get the width and height from the children.
    // 现在我们就可以从子view那里获取宽度和高度了。
    mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);
}
```

我们看到，在一开始首先调用了`mLayout.onMeasure()`方法，在该方法上方有一大段注释，大概意思如下：

```java
/**
  此特定方法调用应该视为已弃用，并替换为{@link #defaultOnMeasure（int，int）}。 
  但它实际上还不能被替换，因为它可能破坏现有的第三方代码，但是当{@link LayoutManager＃  isAutoMeasureEnabled（）}返回true时，所有文档都指示开发人员不要重写{@link LayoutManager＃onMeasure（int，int）}。
  */
```

我想这个地方应该是为了兼容性考虑，因为当自定义 LayoutManager时，有可能已经重写了 `onMeasure()` 方法。但是当 `LayoutManager#isAutoMeasureEnabled()`返回true时，所有自定义LayoutManager 都不要重写`onMeasure()`方法。

接下来我们看一下LayoutManager的`onMeasure()`方法内部做了什么：

```java
		public void onMeasure(@NonNull Recycler recycler, @NonNull State state, int 				widthSpec,int heightSpec) {
            mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
        }
```

我们发现，在`onMeasure()`内部，其实又调用了RecyclerView的`defaultOnMeasure()`方法去设置RecyclerView的默认宽、高尺寸信息。再然后，判断宽、高的测量模式是否都为“MeasureSpec.EXACTLY”精确测量模式，如果宽、高都为精确测量模式，或者`Adapter == null`时，直接返回，结束测量。

接下来，关键点来了，这段逻辑涉及到和布局相关的三大步骤中的其中前两个。我们发现，源码中首先通过判断`mState.mLayoutStep == State.STEP_START`是否为true，来执行`dispatchLayoutStep1()`方法，这也印证了我们前面分析得到的结论：

**`dispatchLayoutStep1()`方法执行时，mLayoutStep的取值必须为State.STEP_START！**

接下来，在执行`dispatchLayoutStep2()`之前，首先调用了LayoutManager的`setMeasureSpecs()`方法，源码如下：

```java
		void setMeasureSpecs(int wSpec, int hSpec) {
            mWidth = MeasureSpec.getSize(wSpec);
            mWidthMode = MeasureSpec.getMode(wSpec);
            if (mWidthMode == MeasureSpec.UNSPECIFIED && 												!ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mWidth = 0;
            }

            mHeight = MeasureSpec.getSize(hSpec);
            mHeightMode = MeasureSpec.getMode(hSpec);
            if (mHeightMode == MeasureSpec.UNSPECIFIED && 												!ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mHeight = 0;
            }
        }
```

我们发现，该方法中的主要逻辑是为LayoutManager中的mWidth（宽度）、mWidthMode（宽度测量模式）、mHeight（高度）和mHeightMode（高度测量模式）成员变量赋值的操作，逻辑还是比较容易理解的。

再然后，标识当前`mState.mIsMeasuring = true`，来表示当前处于测量阶段。通过，查阅源码当中该字段的赋值发现，只有当处于测量阶段时，该值才会赋值为true，其它阶段都为false。

接下来，就开始执行布局三大步骤中的第二步`dispatchLayoutStep2()`执行预布局逻辑啦。在预布局逻辑执行完之后，又调用LayoutManager的`setMeasuredDimensionFromChildren()`来最终设置RecyclerView的宽、高信息。

通过该方法上方的注释信息，我们发现：在`dispatchLayoutStep2()`方法执行完毕之后，就可以获取子View的宽高信息啦。由此，我们可以大胆猜测`dispatchLayoutStep2()`方法内部肯定测量了所有子View的尺寸信息。

我们接着来分析LayoutManager的`setMeasuredDimensionFromChildren()`方法，看看其内部的逻辑是怎样的。源码如下：

```java
		void setMeasuredDimensionFromChildren(int widthSpec, int heightSpec) {
            final int count = getChildCount();
            if (count == 0) {
                mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
                return;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                final Rect bounds = mRecyclerView.mTempRect;
                getDecoratedBoundsWithMargins(child, bounds);
                if (bounds.left < minX) {
                    minX = bounds.left;
                }
                if (bounds.right > maxX) {
                    maxX = bounds.right;
                }
                if (bounds.top < minY) {
                    minY = bounds.top;
                }
                if (bounds.bottom > maxY) {
                    maxY = bounds.bottom;
                }
            }
            mRecyclerView.mTempRect.set(minX, minY, maxX, maxY);
            setMeasuredDimension(mRecyclerView.mTempRect, widthSpec, heightSpec);
        }
```

这个方法还是比较容易理解的，遍历RecyclerView所有已添加的子View，计算它们所占的范围信息，最后调用`setMeasuredDimension()`来设置RecyclerView的宽、高信息。其实，该方法内部最终还是通过LayoutManager的静态方法`chooseSize()`来得到一个选择后的尺寸信息设置给RecyclerView。

接下来，我们发现其实重新测量逻辑方法块内部的逻辑，和我们前面分析的测量逻辑是一样的，只不过是重新又调用了一遍而已。我们重点分析的应该是什么情况下才回去重新测量。通过其上方的注释，我们可以看到：**如果RecyclerView经历过一次测量逻辑之后，宽、高还不是精确的；并且如果，至少有一个子View的宽、高度也不是精确的，我们就必须要重新测量。**

我们先来看一下LayoutManager中的默认实现：

```java
/**
   Internal API to allow LayoutManagers to be measured twice.
   <p>
    This is not public because LayoutManagers should be able to handle their layouts in     one pass but it is very convenient to make existing LayoutManagers support wrapping     content when both orientations are undefined.
   <p>
   This API will be removed after default LayoutManagers properly implement wrap content in non-scroll orientation.
  */
/**
   内部API，可以允许LayoutManagers进行两次测量。
   
   这不是公共的方法，因为LayoutManagers应该一次测量就能处理好它们的布局。但是当两个方向都未定义时，可以使    系统已有的LayoutManagers支持内容包裹时非常方便（其实就是LinearLayoutManager，只有它重写了该方      法）。
   
   在系统默认的LayoutManagers可以以非滚动方向正确实现内容包裹之后，将删除此API。       
  */
boolean shouldMeasureTwice() {
    return false;
}
```

通过对官方注释的分析，我们发现，一般情况下，LayoutManager应该一次测量就能处理好它们的布局。系统提供的默认LayoutManager中，只有LinearLayoutManager重写了该方法，用以处理当两个方向都未定义时的内容包裹逻辑，并且，该方法将在不久之后移除掉。

我们再来看一下LinearLayoutManager中该方法的实现：

```java
	@Override
    boolean shouldMeasureTwice() {
        return getHeightMode() != View.MeasureSpec.EXACTLY
                && getWidthMode() != View.MeasureSpec.EXACTLY
                && hasFlexibleChildInBothOrientations();
    }
```

我们发现，其实，系统内部需要测量两次的逻辑，还是比较严格的：

**只有当宽、高测量模式都不是精确模式，并且如果子View的宽、高都不是精确的话，才会去测量第二次！**

#### 小结

- **在LayoutManager开启自动测量时，首先会调用`mLayout.onMeasure()`方法来设置一次RecyclerView的宽、高尺寸；然后再判断宽、高的测量模式是否都为“MeasureSpec.EXACTLY”精确测量模式，如果宽、高都为精确测量模式，或者`Adapter == null`时，直接返回，结束测量。**
- **然后，根据`mState.mLayoutStep 是否为 State.STEP_START`，来执行`dispatchLayoutStep1()`逻辑；**
- **接下来，在执行`dispatchLayoutStep2()`方法之前，做了一些前置工作：设置LayoutManager的宽高测量规则；标识当前处于测量测量阶段；当预布局逻辑完成之后，最终通过LayoutManager的`setMeasuredDimensionFromChildren()`来最终设置RecyclerView的宽、高尺寸。**
- **如果RecyclerView经历过一次测量逻辑之后，宽、高还不是精确的；并且如果，至少有一个子View的宽、高度也不是精确的，我们就必须要重新测量。该逻辑只有LinearLayoutManager及其子类才有可能测量两次！**

### 1.3、LayoutManager未开启自动测量

我们首先来看一下LayoutManager未开启自动测量时的源码：

```java
if (mHasFixedSize) {
    mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);
    return;
}
// custom onMeasure
if (mAdapterUpdateDuringMeasure) {
    startInterceptRequestLayout();
    onEnterLayoutOrScroll();
    processAdapterUpdatesAndSetAnimationFlags();
    onExitLayoutOrScroll();

    if (mState.mRunPredictiveAnimations) {
        mState.mInPreLayout = true;
    } else {
        // consume remaining updates to provide a consistent state with the layout pass.
        mAdapterHelper.consumeUpdatesInOnePass();
        mState.mInPreLayout = false;
    }
    mAdapterUpdateDuringMeasure = false;
    stopInterceptRequestLayout(false);
} else if (mState.mRunPredictiveAnimations) {
    // If mAdapterUpdateDuringMeasure is false and mRunPredictiveAnimations is true:
    // this means there is already an onMeasure() call performed to handle the pending
    // adapter change, two onMeasure() calls can happen if RV is a child of LinearLayout
    // with layout_width=MATCH_PARENT. RV cannot call LM.onMeasure() second time
    // because getViewForPosition() will crash when LM uses a child to measure.
    setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    return;
}

if (mAdapter != null) {
    mState.mItemCount = mAdapter.getItemCount();
} else {
    mState.mItemCount = 0;
}
startInterceptRequestLayout();
mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);
stopInterceptRequestLayout(false);
mState.mInPreLayout = false; // clear
```

我们发现，当mHasFixedSize为true（调用了RecyclerView的`setHasFixedSize(true)`方法）时，通过调用LayoutManager的`onMeasure()`去设置RecyclerView的默认宽、高尺寸信息。

## 二、onLayout()



## 三、onDraw()



## RecyclerView.Recycler



### RecycledViewPool



### ViewCacheExtension



### 内部重要方法



#### getViewForPositon(int position)