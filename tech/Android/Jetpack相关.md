# Jetpack相关



## LiveData

### 优势

#### 确保界面符合数据状态

LiveData 遵循观察者模式。当生命周期状态发生变化时，LiveData 会通知 [`Observer`](https://developer.android.com/reference/androidx/lifecycle/Observer) 对象。您可以整合代码以在这些 `Observer` 对象中更新界面。观察者可以在每次发生更改时更新界面，而不是在每次应用数据发生更改时更新界面。

#### 不会发生内存泄漏

观察者会绑定到 [`Lifecycle`](https://developer.android.com/reference/androidx/lifecycle/Lifecycle) 对象，并在其关联的生命周期遭到销毁后进行自我清理。

#### 不会因Activity停止而导致崩溃

如果观察者的生命周期处于非活跃状态（如返回栈中的 Activity），则它不会接收任何 LiveData 事件。

#### 不再需要手动处理生命周期

界面组件只是观察相关数据，不会停止或恢复观察。LiveData 将自动管理所有这些操作，因为它在观察时可以感知相关的生命周期状态变化。

#### 数据始终保持最新状态

如果生命周期变为非活跃状态，它会在再次变为活跃状态时接收最新的数据。例如，曾经在后台的 Activity 会在返回前台后立即接收最新的数据。

#### 适当的配置更改

如果由于配置更改（如设备旋转）而重新创建了 Activity 或 Fragment，它会立即接收最新的可用数据。

#### 共享资源

您可以使用单一实例模式扩展 [`LiveData`](https://developer.android.com/reference/androidx/lifecycle/LiveData) 对象以封装系统服务，以便在应用中共享它们。`LiveData` 对象连接到系统服务一次，然后需要相应资源的任何观察者只需观察 `LiveData` 对象。如需了解详情，请参阅[扩展 LiveData](https://developer.android.com/topic/libraries/architecture/livedata#extend_livedata)。