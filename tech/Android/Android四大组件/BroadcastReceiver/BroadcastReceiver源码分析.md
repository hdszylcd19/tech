

## BroadcastReceiver概览

Android应用与Android系统，和其它Android应用之间可以相互收发广播消息，这与“发布-订阅”设计模式相似。这些广播会在所关注的事件发生时发送。

举例来说，Android系统会在发生各种系统事件时发送广播，例如系统启动或设备开始充电时。再比如，应用可以发送自定义广播来通知其它应用它们可能感兴趣的事件（例如，一些新数据已下载）。

应用可以注册接收特定的广播。广播发出后，系统会自动将广播传送给同意接收这种广播的应用。

## 关于系统广播

Android系统会在发生各种系统事件时自动发送广播，例如，当系统进入和退出飞行模式时。系统广播会被发送给所有同意接收相关事件的应用。

广播消息本身会被封装在一个Intent对象中，该对象的操作字符串会标识所发生的事件（例如：`android.intent.action.AIRPLANE_MODE`）。该Intent可能还包含绑定到其extra字段中的附加信息。例如，飞行模式intent包含布尔值extra来指示是否已开启飞行模式。

有关系统广播操作的完整列表，请参阅Android SDK中的`broadcast_actions.txt`文件。每个广播都有一个与之关联的常量字段。例如，常量`ACTION_AIRPLANE_MODE_CHANGED`的值为`android.intent.action.AIRPLANE_MODE`。每个广播操作的文档都可以在关联的常量字段中找到。

### 系统广播变更

随着Android平台的发展，Google会不定期更改系统广播的行为方式。

如果您的应用以Android7.0（API：24）或更高版本为目标平台，或者安装在搭载Android7.0或更高版本的设备上，请注意以下变更：

#### Android 9.0

从Android 9.0（API：28）开始，`NETWORK_STATE_CHANGED_ACTION`广播不再接收有关用户位置或个人身份数据的信息。

此外，如果您的应用安装在搭载Android 9.0或更高版本的设备上，则通过WLAN接收的系统广播不包含SSID、BSSID、连接信息或扫描结果。如果要获取这些信息，请调用WifiManager中的[getConnectionInfo()](https://developer.android.google.cn/reference/android/net/wifi/WifiManager#getConnectionInfo())方法。

#### Android 8.0

从Android 8.0（API：26）开始，系统对清单文件中声明的广播接收器施加了额外的限制。

如果您的应用以Android8.0或更高版本为目标平台，那么对于大多数隐式广播（静态广播），您不能使用清单文件来声明广播接收器。当用户正在活跃地使用您的应用时，您仍可使用动态注册广播的方式来注册`BroadcastReceiver`。

#### Android 7.0

Android 7.0(API：24)及更高版本不发送以下系统广播：

- [ACTION_NEW_PICTURE](https://developer.android.google.cn/reference/android/hardware/Camera#ACTION_NEW_PICTURE)
- [ACTION_NEW_VIDEO](https://developer.android.google.cn/reference/android/hardware/Camera#ACTION_NEW_VIDEO)

此外，以Android 7.0及更高版本为目标平台的应用必须使用[registerReceiver(BroadcastReceiver,IntentFilter)](https://developer.android.google.cn/reference/android/content/Context#registerReceiver(android.content.BroadcastReceiver,%20android.content.IntentFilter))来注册[CONNECTIVITY_ACTION](https://developer.android.google.cn/reference/android/net/ConnectivityManager#CONNECTIVITY_ACTION)广播。无法在清单文件中声明为静态广播。

## 接收广播

应用程序可以通过两种方式接收广播：静态广播和动态广播。

### 静态广播

如果您在清单文件中声明广播接收器，Android系统会在广播发出后启动您的应用（如果应用尚未运行）。

> 
> **特别注意：**
>
> 如果您的应用以Android 8.0（API：26）或更高版本的平台为目标，则不能使用清单文件为隐式广播（静态广播）声明广播接收器，但有一些[不受此限制](https://developer.android.google.cn/guide/components/broadcast-exceptions)的隐式广播除外。在大多数情况下，您可以使用[调度作业](https://developer.android.google.cn/guide/background)来代替。

如果要使用静态广播，请按以下步骤执行：

#### 步骤一

在`AndroidManifest.xml`清单文件中，声明`<receiver>`元素。示例代码如下：

```java
<receiver android:name=".StaticBroadcastReceiver">
    <intent-filter>
    	// 接收开机广播(P200平板上实测，接收不到开机广播，貌似是非系统应用接收不到...)
    	// 实测使用模拟器是可以接收开机广播的
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        // 接收自定义静态广播
        <action android:name="com.xh.broadcast_receiver_demo.RECEIVER" />
    </intent-filter>
</receiver>
```
其中，`intent-filter`指定您的接收器所订阅的广播操作。

#### 步骤二

创建`BroadcastReceiver`子类并实现`onReceive(Context,Intent)`方法。以下示例中的广播接收器会记录并显示广播的内容：

```java
public class StaticBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + action + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME) + "\n");
        String log = sb.toString();
        Log.i("StaticBroadcastReceiver", log);
        Toast.makeText(context, log, Toast.LENGTH_LONG).show();
    }
}
```

系统软件包管理器会在应用安装时注册接收器。然后，该接收器会成为应用的一个独立入口点，这意味着如果应用当前未运行，系统可以启动应用并发送广播。

系统会创建新的`BroadcastReceiver`组件对象，来处理它接收到的每个广播。此对象仅在调用`onReceive(Context,Intent)`期间有效。一旦此方法执行完毕，系统便会认为该组件不再活跃。

### 动态广播

要使用`Context`注册广播接收器，请按以下步骤执行：

#### 步骤一

自定义`BroadcastReceiver`，并创建其实例：

```java
BroadcastReceiver dynamicBroadcastReceiver = new DynamicBroadcastReceiver();
```

#### 步骤二

创建IntentFilter并调用`registerReceiver(BroadcastReceiver, IntentFilter)`来注册接收器：

```java
IntentFilter intentFilter = new IntentFilter();
intentFilter.addAction(Intent.ACTION_SCREEN_ON); //监听亮屏广播
intentFilter.addAction(Intent.ACTION_SCREEN_OFF); //监听锁屏广播
intentFilter.addAction(ACTION_ACT); //监听自定义广播
registerReceiver(dynamicBroadcastReceiver, intentFilter);
```

**注意：要注册本地广播，请调用`LocalBroadcastManager.registerReceiver(BroadcastReceiver, IntentFilter)`。**

只要注册的`Context`有效，`Context`注册的接收器就会接收广播。如果您在`Activity`上下文中注册，只要Activity没有被销毁，您就会收到广播。如果您在`Application`上下文中注册，只要应用在运行，您就会收到广播。

#### 步骤三

要停止接收广播，请调用`unregisterReceiver(android.content.BroadcastReceiver)`。当您不再需要接收器或上下文不再有效时，请务必注销接收器。

**请注意注册和注销接收器的位置：**

如果您使用`Activity`上下文在`onCreate(Bundle)`中注册接收器，则应在`onDestory()`中注销，以防止接收器从`Activity`上下文中泄漏出去。

如果您在`onResume()`中注册接收器，则应在`onPause()`中注销，以防止多次注册接收器（如果您不想在暂停时接收广播，这样可以减少不必要的系统开销）。

请勿在onSaveInstanceState(Bundle)中注销，因为如果用户在历史记录堆栈中后退，则不会调用此方法。

## 对进程状态的影响

`BroadcastReceiver`的状态（无论它是否在运行）会影响其所在进程的状态，而其所在进程的状态又会影响它被系统终结的可能性。

例如，当进行执行接收器（也就是当前正在运行`onReceive()`方法中的代码）时，它被认为是前台进程。除非遇到极大的内存压力，否则系统会保持该进程运行。

但是，一旦从`onReceive()`返回，`BroadcastReceiver`就不再活跃。接收器的宿主进程变得与其中运行的其它应用组件一样重要。如果该进程仅托管清`AndroidManifest.xml`清单文件中声明的接收器（这对于用户从未与之互动或最近没有与之互动的应用很常见），则从`onReceive()`返回时，系统会将其进程视为低优先级进程，并可能会将其终止，以便将资源提供给其它更重要的进程使用。

因此，您不应从广播接收器启动长时间运行的后台线程。`onReceive()`执行完成后，系统可以随时终止进程来回收内存，在此过程中，也会终止进程中运行的派生线程。要避免这种情况，您应该调用`goAsync()`（如果您希望在后台线程中多花一点时间来处理广播）或者使用`JobScheduler`从接收器调度`JobService`，这样系统就会知道该进程将继续活跃地工作。如需了解详情，请参阅[进程和应用生命周期](https://developer.android.google.cn/guide/components/activities/process-lifecycle)。

以下代码展示了一个`BroadcastReceiver`，它使用`goAsync()`来标记它在`onReceive()`完成后需要更多时间才能完成。如果您希望在`onReceive()`中完成的工作很长，足以导致界面线程丢帧（>16ms），则这种做法非常有用，这使它尤其适用于后台线程。

```java
public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        Task asyncTask = new Task(pendingResult, intent);
        asyncTask.execute();
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;

        private Task(PendingResult pendingResult, Intent intent) {
            this.pendingResult = pendingResult;
            this.intent = intent;
        }

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
            String log = sb.toString();
            Log.d(TAG, log);
            return log;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }
}
```

## 发送广播

Android为应用提供三种方式来发送广播：

`sendOrderedBroadcast(Intent,String)`：该方法一次向一个接收器发送广播。当接收器逐个顺序执行时，接收器可以向下传递结果，也可以完全中止广播，使其不再传递给其它接收器。接收器的运行顺序可以通过匹配的`intent-filter`的`android:priority`属性来控制；具有相同优先级的接收器将按随机顺序运行。

`sendBroadcast(Intent)`：该方法会按随机的顺序向所有接收器发送广播，这称为常规广播。这种方法效率更高，但也意味着接收器无法从其它接收器读取结果，无法传递从广播中收到的数据，也无法中止广播。

`LocalBroadcastManager.sendBroadcast(Intent)`：该方法会将广播发送给与发送器位于同一应用中的接收器。如果您不需要跨应用发送广播，请使用本地广播。这种实现方式的效率更高（无需进行进程间通信），而且您无需担心其它应用在收发您的广播时带来的任何安全问题。

以下代码展示了如何通过创建`Intent`并调用`sendBroadcast(Intent)`来发送广播。

```java
Intent intent = new Intent();
intent.setAction(ACTION_ACT); //自定义Action
intent.putExtra("data", "启动ACT");
sendBroadcast(intent);
```

广播消息封装在`Intent`对象中。`Intent`的`Action`操作字符串必须提供应用的Java软件包名称语法，并唯一标识广播事件。您可以使用`putExtra(String,Bundle)`向Intent中附加其它信息。您也可以使用`Intent`的`setPackage(String)`,将广播限定到同一组织中的一组应用。

**注意：虽然`Intent`既用于发送广播，也用于通过startActivity(Intent)启动Activity，但这两种操作是完全无关的。广播接收器无法查看或捕获用于启动Activity的Intent；同样，当您广播Intent时，也无法找到或启动Activity。**

## 通过权限限制广播

您可以通过权限将广播限定到拥有特定权限的一组应用。您可以对广播的发送器或接收器施加限制。

### 带权限的发送

当您调用`sendBroadcast(Intent,String)`或`sendOrderedBroadcast(Intent,String,BroadcastReceiver,Handler,int,String,Bundle)`时，可以指定权限参数。接收器若要接收此广播，则必须通过其`AndroidManifest.xml`清单文件中的标记请求该权限（如果存在危险，则会被授予该权限）。例如，以下代码会发送广播：

```java
sendBroadcast(new Intent("com.example.NOTIFY"),Manifest.permission.SEND_SMS);
```

要接收此广播，接收方应用必须请求如下权限：

```java
<uses-permission android:name="android.permission.SEND_SMS"/>
```

您可以指定现有的系统权限（如：SEND_SMS），也可以使用`<permission>`元素定义自定义权限。有关权限和安全性的一般信息，请参阅[系统权限](https://developer.android.google.cn/guide/topics/permissions/overview)。

**注意：自定义权限将在安装应用时注册。定义自定义权限的应用必须在使用自定义权限的应用之前安装。**	

### 带权限的接收

如果您在注册广播接收器时指定了权限参数（通过`registerReceiver(BroadcastReceiver,IntentFilter,String,Handler)`或`AndroidManifest.xml`清单文件中的`<receiver>`标记指定），则广播方必须通过其`AndroidManifest.xml`清单文件中的`<uses-permission>`标记请求该权限（如果存在危险，则会被授予该权限），才能向该接收器发送`Intent`。

假如，您的接收方应用具有如下所示的`AndroidManifest.xml`清单文件中声明的接收器：

```java
<receiver android:name=".MyBroadcastReceiver"
          android:permission="android.permission.SEND_SMS">
    <intent-filter>
        <action android:name="android.intent.action.AIRPLANE_MODE"/>
    </intent-filter>
</receiver>
```

或者您的接收方应用具有如下所示的上下文注册的接收器：

```java
IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
registerReceiver(receiver, filter, Manifest.permission.SEND_SMS, null );
```

那么，发送方应用必须请求如下权限，才能向这些接收器发送广播：

```java
<uses-permission android:name="android.permission.SEND_SMS"/>
```

## 安全注意事项和最佳做法

以下是有关收发广播的一些安全注意事项和最佳做法：




> 参考链接：
> 
> [广播概览](https://developer.android.google.cn/guide/components/broadcasts#sending-broadcasts)