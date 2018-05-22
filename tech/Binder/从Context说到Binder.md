### 前情提要

当我们在Android项目中，希望使用系统服务时，可以通过`Context`的`getSystemService ()`方法来获得。例如，当我们想要判断当前网络状态是否可用，可以通过下面的代码实现：

```
public static boolean isNetworkAvailable(@NonNull Context context) {
	// 获取系统服务（核心代码）
   	ConnectivityManager cm = (ConnectivityManager)context
   							.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null &&
                info.isConnected() &&
                info.getState() == NetworkInfo.State.CONNECTED) {
                // 当前网络可用
                return true;
            }
        }
        // 当前网络不可用
        return false;
    }
```

上面的代码相对来说，还是比较简单的；核心代码就是通过`getSystemService ()`获取一个系统服务，后续只需调用`ConnectivityManager`提供的相关方法。下面我们将从`getSystemService ()`开始追溯，看看这个方法里面到底做了什么，竟有这样的魔力？

### 一. `Context.getSystemService()`

`getSystemService()`定义在`\frameworks\base\core\java\android\content\Context.java`文件中，方法声明如下：

```
public abstract Object getSystemService(@ServiceName @NonNull String name);
```

那么，该方法具体是在哪里实现的呢？

**除了Activity中的`getSystemService ()`有所区别之外，其余所有的`Context`调用`getSystemService ()`时，最终都会调用`ContextWrapper`中的`getSystemService ()`。**

对此有疑问的小伙伴，可以参考我的另一篇文章：[彻底理解Android中的Context](https://juejin.im/post/5aeff87e6fb9a07aad177173)

#### 1.1 `Activity.getSystemService()`

方法定义在`\frameworks\base\core\java\android\app\Activity.java`中，声明如下：

```
@Override
public Object getSystemService(@ServiceName @NonNull String name) {
    // getBaseContext()获取的其实就是ContextWrapper中的成员变量mBase
    if (getBaseContext() == null) {
        throw new IllegalStateException(
        "System services not available to Activities before onCreate()");
    }

    if (WINDOW_SERVICE.equals(name)) { // 如果获取的是WindowManager，直接返回
    	return mWindowManager;
    } else if (SEARCH_SERVICE.equals(name)) { // 如果获取的是SearchManager，直接返回
    	ensureSearchManager();
    	return mSearchManager;
    }

    // 调用父类的方法
    return super.getSystemService(name);
}
```

从上面方法可以看出：

- 当在`Activity`中的`onCreate()`生命周期之前调用`getSystemService ()`时，会抛出异常！因为，`ContextWrapper`中的成员变量`mBase`是在`attach()`方法中初始化的；
- 当获取的是`WindowManager`或者`SearchManager`时，直接返回；
- 最终调用父类`ContextThemeWrapper`中的`getSystemService ()`方法；

##### 1.1.1 `ContextThemeWrapper.getSystemService()`

方法定义在`\frameworks\base\core\java\android\view\ContextThemeWrapper.java`中，声明如下：

```
@Override 
public Object getSystemService(String name) {
    if (LAYOUT_INFLATER_SERVICE.equals(name)) { //如果获取的是LayoutInflater，直接返回
        if (mInflater == null) {
        	mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
        }
        return mInflater;
    }
    return getBaseContext().getSystemService(name);
}
```

从上面的方法可以看出：

- 作进一步校验，如果获取的是`LayoutInflater`，直接返回；
- 调用`getBaseContext()`的`getSystemService ()`方法，其实最终就是调用`ContextWrapper`中的成员变量`mBase`的`getSystemService ()`方法；

**小结：**

**`Activity`中的`getSystemService ()`方法，最终会调用到`ContextWrapper`中的成员变量`mBase`的`getSystemService ()`方法；**

#### 1.2 `ContextWrapper.getSystemService()`

方法定义在`\frameworks\base\core\java\android\content\ContextWrapper.java`中，声明如下：

```
@Override
public Object getSystemService(String name) {
	return mBase.getSystemService(name);
}
```

由此我们发现：

- **通过Context调用`getSystemService ()`时，最终都会调用到`ContextWrapper`中的成员变量`mBase`的`getSystemService()`方法；**
- `mBase`其实就是`ContextImpl`的实例；详见[[彻底理解Android中的Context](https://juejin.im/post/5aeff87e6fb9a07aad177173)]。

#### 1.3 `ContextImpl.getSystemService()`

方法定义在`\frameworks\base\core\java\android\app\ContextImpl.java`中，声明如下：

```
@Override
public Object getSystemService(String name) {
	return SystemServiceRegistry.getSystemService(this, name);
}
```

##### 1.3.1 `SystemServiceRegistry.getSystemService()`

方法定义在`\frameworks\base\core\java\android\app\SystemServiceRegistry.java`中，声明如下：

```
public static Object getSystemService(ContextImpl ctx, String name) {
    ServiceFetcher<?> fetcher = SYSTEM_SERVICE_FETCHERS.get(name);
    // 最终调用ServiceFetcher的getService()方法
    return fetcher != null ? fetcher.getService(ctx) : null;
}
```

在该方法中，通过你传递过来的“name”，获取一个`ServiceFetcher`对象，最终调用的是`ServiceFetcher`中的`getService()`方法；

##### 1.3.2 `SystemServiceRegistry.SYSTEM_SERVICE_FETCHERS`

`SYSTEM_SERVICE_FETCHERS`的定义如下：

```
private static final HashMap<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS =
            new HashMap<String, ServiceFetcher<?>>();
```

##### 1.3.3 `SystemServiceRegistry.registerService()`

通过搜索发现，该集合只在一个地方有存储元素的操作；代码如下：

```
private static <T> void registerService(String serviceName, Class<T> serviceClass,
            ServiceFetcher<T> serviceFetcher) {
        SYSTEM_SERVICE_NAMES.put(serviceClass, serviceName);
        SYSTEM_SERVICE_FETCHERS.put(serviceName, serviceFetcher);
}
```

该方法把`serviceName`作为键，`serviceFetcher`作为值存储到集合当中。

##### 1.3.4 `SystemServiceRegistry.ServiceFetcher`

`ServiceFetcher`其实是一个泛型接口，定义在`SystemServiceRegistry`类的内部，声明如下：

```
 static abstract interface ServiceFetcher<T> {
 	T getService(ContextImpl ctx);
 }
```

接着搜索该方法的调用，发现该方法在静态代码块中被调用了很多次，用来注册不同的系统服务。我们以前面的`Context.CONNECTIVITY_SERVICE`为例，代码如下：

```
static {
	...
    registerService(Context.CONNECTIVITY_SERVICE, ConnectivityManager.class,
                    new StaticOuterContextServiceFetcher<ConnectivityManager>() {
                @Override
                public ConnectivityManager createService(Context context) {
                	// 终于发现跟Binder的身影了...泪崩...不容易呀
                    IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
                    IConnectivityManager service = IConnectivityManager.Stub.asInterface(b);
                    return new ConnectivityManager(context, service);
                }});
     ...
}
```

在该方法的调用中：

- `serviceName`就是`Context.CONNECTIVITY_SERVICE`；
- `serviceClass`就是`ConnectivityManager.class`；
- `serviceFetcher`就是`StaticOuterContextServiceFetcher`的一个匿名内部类。

##### 1.3.5 `SystemServiceRegistry.StaticOuterContextServiceFetcher`

该类定义在`SystemServiceRegistry`类的内部，声明如下：

```
static abstract class StaticOuterContextServiceFetcher<T> implements ServiceFetcher<T> {
    private T mCachedInstance;

    @Override
    public final T getService(ContextImpl ctx) {
        synchronized (StaticOuterContextServiceFetcher.this) {
        	// 首次调用时，mCachedInstance = null；会调用createService().
            if (mCachedInstance == null) {
            	mCachedInstance = createService(ctx.getOuterContext());
            }
            return mCachedInstance;
        }
    }

    public abstract T createService(Context applicationContext);
}
```

由[1.3.1]可知，`Context.getSystemService()`最终返回的是`ServiceFetcher`中`getService()`方法的返回值。而`getService()`返回的又是`createService()`方法的返回值；在本例中`createService()`的实现如下：

```
@Override
public ConnectivityManager createService(Context context) {
	// IPC通信。通过getService()最终获取了指向目标Binder服务端的代理对象BinderProxy。
    IBinder b = ServiceManager.getService(Context.CONNECTIVITY_SERVICE);
    // IConnectivityManager是一个aidl文件，定义在\frameworks\base\core\java\android\net目录下
    IConnectivityManager service = IConnectivityManager.Stub.asInterface(b);
    return new ConnectivityManager(context, service);
}
```

追溯到这里，我们已然来到了一个重要拐点：再继续下去，就下探到Binder的领域啦；是否到此为止了呢？

**你想拥有什么你就去追求什么！**

在这里，我准备先抛出结论。不想继续下去的小伙伴可以就近下车啦！

#### `Context.getSystemService()`小结

- 通过`Context.getSystemService()`方法的层层调用，最终会走到`ContextImpl.getSystemService ()`方法之中；
- 在`ContextImpl.getSystemService ()`方法中，经过层层调用，最终返回的是`ServiceFetcher`接口中`getService()`方法的返回值；
- 虽然，根据系统服务的不同，`ServiceFetcher.getService()`的返回值形式上有所差异；但是，底层都会调用到`ServiceManager.getService()`方法，最终会触发IPC(Inter-Process Communication  )机制，返回一个指向目标Binder服务端的代理对象`BinderProxy`；

- 后续所有的调用都是通过这个`BinderProxy`来实现的；

### 二. `ServiceManager.getService()`

to be continued...