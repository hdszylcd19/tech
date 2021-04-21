

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

#### 使用方式

如果要使用静态广播，请按以下步骤执行：

##### 声明静态广播

在`AndroidManifest.xml`清单文件中，声明`<receiver>`元素。示例代码如下：

```java


```
