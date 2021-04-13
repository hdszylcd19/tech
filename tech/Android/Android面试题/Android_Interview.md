## Binder

### 谈谈你对Binder的理解

Binder是一种进程间通信机制，是Google基于开源的OpenBinder实现，binder是Android中主要的跨进程通信方式。其中，binder驱动和`Service Manager`分别相当于网络协议中的路由器和DNS（域名解析服务器），并基于mmap实现了IPC传输数据时只需一次拷贝。

binder包括BinderProxy、BpBinder等各种Binder实体，以及对binder驱动操作的`ProcessState`、`IPCThreadState`封装，再加上binder驱动内部的结构体、命令处理，整体贯穿Java、Native层，涉及用户态、内核态，往上可以说到Service、AIDL等，往下可以说到mmap、binder驱动设备，是相当庞大、繁琐的一个机制。

我自己来谈的话，一天时间都是不够的，还是问我具体问题吧。

**【面试官】：既然你说到AIDL，那么说一说oneway吧。**

oneway主要有两个特性：异步调用和串行化处理。

异步调用是指应用向binder驱动发送数据后不需要挂起线程等待binder驱动的回复，而是直接结束。像一些系统服务调用应用进程的时候就会使用oneway，比如AMS调用应用进程启动Activity，这样就算应用进程中做了耗时的任务，也不会阻塞系统服务的运行。

串行化处理是指对于一个服务端的AIDL接口而言，所有的oneway方法不会同时执行，binder驱动会将它们串行化处理，排队一个一个调用。

**【面试官】：有了解过相关的binder协议吗？**

了解过，看图会更直观一些，我来画一下图吧，首先是非oneway的情况：

![binder_default协议](./imgs/binder_default协议.webp.jpg)

如果是oneway的话，客户端就不需要挂起线程等待：

![binder_default协议](./imgs/binder_oneway协议.webp.jpg)

涉及到的binder命令也有规律，由外部发送给binder驱动的都是`BC_`开头，由binder驱动发往外部的都是`BR_`开头。

**【面试官】：怎么理解客户端线程挂起等待呢？有没有实际占用CPU的调度？**

这里的挂起相当于Thread的sleep，是真正的“休眠”，底层调用的是`waiteventinterruptible()`Linux系统函数。

**【面试官】：你是从哪里了解到`waiteventinterruptible()`函数的呢？**

在学习Handler机制的时候，Handler中最关键的地方就是Looper的阻塞与唤醒，阻塞是调用了`nativePollOnce()`方法，当时对它的底层实现感兴趣，就去了解了一下，也学习到Linux用来实现阻塞/唤醒的select、poll和epoll机制。

**【面试官】：基于mmap又是如何实现一次拷贝的？**

其实原理很简单，我来画一个示意图：

![binder_mmap一次拷贝](./imgs/binder_mmap一次拷贝.jpg)

Client与Server处于不同进程，有着不同的虚拟地址规则，所以无法直接通信。而一个页框可以映射给多个页，那么就可以将一块物理内存分别与Client和Server的虚拟内存块进行映射。

如图，Client就只需`copy_from_user`进行一次数据拷贝，Server进程就能读取到数据了。另外，映射的虚拟内存块大小将近1M（1M - 8K），所以，IPC通信传输的数据量也被限制为此值。

**【面试官】：怎么理解页框和页？**

页框是指一块实际的物理内存，页是指程序的一块内存数据单元。内存数据一定是存储在实际的物理内存上，即：页必然对应于一个页框，页数据实际是存储在页框上的。

页框和页一样大，都是内核对内存的分块单位。一个页框可以映射给多个页，也就是说一块实际的物理存储空间可以映射给多个进程的多个虚拟内存空间，这也就是mmap机制依赖的基础规则。

【面试官】：简单说一下binder的整体架构吧

再来画一个简单的示意图吧，这是一个比较典型的、两个应用之间的IPC通信流程图：

![binder整体架构](./imgs/binder整体架构.png)

Client通过`ServiceManager`或AMS获取到远程binder实体，一般会用**Proxy**做一层封装，比如`ServiceManagerProxy`、AIDL生成的Proxy类。而被封装的远程binder实体是一个**BinderProxy**。

**BpBinder**和**BinderProxy**其实是一个东西：远程binder实体。只不过一个Native层、一个Java层，**BpBinder**内部持有一个binder句柄值handle。

**ProcessState**是进程单例，负责打开binder驱动设备及mmap；**IPCThreadState**为线程单例，负责与binder驱动进行具体的命令通信。

由**Proxy**发起`transact()`调用，会将数据打包到Parcel中，层层向下调用到**BpBinder**，在**BpBinder**中调用**IPCThreadState**的`transact()`方法并传入handle句柄值，**IPCThreadState**再去执行具体的binder命令。

由binder驱动到Server的大概流程就是：Server通过**IPCThreadState**接受到Client的请求后，层层向上，最后回调到**Stub**的`onTransact()`方法。

当然，这不代表所有的IPC流程，比如`ServiceManager`作为一个Server时，便没有上层的封装，也没有借助**IPCThreadState**，而是初始化后通过`binder_loop()`方法直接与binder驱动通信的。

**【面试官】：可以可以，我们再来聊聊别的。**

### 谈谈你对binder驱动的了解

先简单画张图：

![binder驱动](./imgs/binder驱动.webp.jpg)

对Binder机制来说，它相当于IPC通信中的路由器，负责实现不同进程间的数据交互，是Binder机制的核心；对Linux系统来说，它是一个字符驱动设备，运行在内核空间，向上层提供`/dev/binder`设备节点以及open、mmap、ioctl等系统调用。

**【面试官】：既然你提到了驱动设备，那先说说Linux的驱动设备吧**

Linux把所有的硬件访问都抽象为对文件的读写、设置，这一“抽象”的具体实现就是驱动程序。驱动程序充当硬件和软件之间的枢纽，提供了一套标准化的调用，并将这些调用映射为实际硬件设备相关的操作，对应用程序来说隐藏了设备工作的细节。

**Linux驱动设备分为三类：字符设备、块设备和网络设备：**

**字符设备：**字符设备就是能够像字节流文件一样被访问的设备。对字符设备进行读/写操作时，实际硬件的I/O一般也紧接着发生。字符设备驱动通常会实现open、close、read和write系统调用，比如显示屏、键盘、串口、LCD、LED等。

**块设备：**块设备是指通过传输数据块来访问的设备，比如硬盘、SD卡、U盘、光盘等。

**网络设备：**网络设备是指能够和其它主机交换数据的设备，比如网卡、蓝牙等设备。

字符设备中有一个比较特殊的杂项设备，它可以自动生成设备节点，设备号为10。Android的Ashmem（匿名共享内存）、Binder都属于杂项设备。

**【面试官】：看过binder驱动的open、mmap、ioctl方法的具体实现吗？**

它们分别对应于驱动层源码`binder.c`中的`binder_open()`、`binder_mmap()`、`binder_ioctl()`方法；

`binder_open()`中主要是创建及初始化`binder_proc`，`binder_proc`是用来存放binder相关数据的结构体，每个进程独有一份。

`binder_mmap()`的主要工作是建立应用程序虚拟内存在内核中的一块映射，这样应用程序和内核就拥有了共享的内存空间，为后面的一次拷贝做准备。

`binder_ioctl()`是binder驱动中工作量最大的一个，它承担了binder驱动的大部分业务。因为，binder驱动并不提供常规的`read()`、`write()`等文件操作，全部通过`binder_ioctl()`实现。

**【面试官】：仅`binder_ioctl()`一个方法是怎么实现大部分业务的？**

binder机制将业务细分为不同的命令，调用`binder_ioctl()`时，传入具体的命令来区分业务，比如有读写数据的`BINDER_WRITE_READ`命令、`Service Manager`的注册命令`BINDER_SET_CONTEXT_MGR`等等。

`BINDER_WRITE_READ`命令最为关键，其细分了一些子命令，比如：`BC_TRANSACTION`、`BC_REPLY`等。`BC_TRANSACTION`就是上层最常用的IPC调用命令了，AIDL接口的`transact`方法就是这个命令。

**【面试官】：binder驱动中要实现这些业务功能，必然要用一些数据结构来存放相关数据，比如你上面说`binder_open()`方法时提到的`binder_proc`，说说你对这些结构体的了解吧。**

| 结构体             | 说明                                                         |
| ------------------ | ------------------------------------------------------------ |
| binder_proc        | 描述使用binder的进程，当调用binder_open函数时会创建          |
| binder_thread      | 描述使用binder的线程，当调用binder_ioctl函数时会创建         |
| binder_node        | 描述binder实体节点，对应于一个server，即用户态的BpBinder对象 |
| binder_ref         | 描述对binder实体节点的引用，关联到一个binder_node            |
| binder_buffer      | 描述binder通信过程中存储数据的Buffer                         |
| binder_work        | 描述一个binder任务                                           |
| binder_transaction | 描述一次binder任务相关的数据信息                             |
| binder_ref_death   | 描述binder_node即binder server的死亡信息                     |

其中主要结构体的引用关系如下：

![binder驱动结构体](./imgs/binder驱动结构体.webp.jpg)

**【面试官】：可以可以，我们再来聊点儿别的。**

> 参考链接：
>
> [谈谈AIDL中的oneway修饰符](https://mp.weixin.qq.com/s/wD3Io-ikS1VCljNkxEb6tQ)
> [谈谈你对binder的理解](https://mp.weixin.qq.com/s/Ef2Qy_xFeI6WU3Q0wf5czA)
> [谈谈你对binder驱动的了解](https://mp.weixin.qq.com/s/LH_JR5Rwh1JL4B6qQkEv9Q)


## Handler

### 谈谈你对Android消息机制的理解



**【面试官】：主线程的Looper.loop()在死循环，为什么没有ANR呢？**

首先我们要明白产生ANR的原因，产生ANR的根本原因是：应用程序未在规定的时间内处理AMS指定的任务才会触发ANR。AMS调用到应用端的Binder线程，应用再将任务封装成Message发送到主线程Handler，`Looper.loop()`通过`MessageQueue.next()`拿到这个消息进行处理。如果不能及时处理这个消息消息呢，肯定是因为它前面有耗时的消息处理，或者因为这个任务本身就很耗时。所以，ANR不是因为loop循环，而是因为主线程中有耗时任务。

**【面试官】：主线程的Looper.loop()在死循环，会很消耗资源吗？**

`Looper.loop()`通过`MessageQueue.next()`取当前需要处理的消息，如果当前没有需要处理的消息呢？会调用`nativePollOnce()`方法让线程进入休眠状态，当消息队列没有消息时，无限休眠；当队列的第一个消息还没到需要处理的时间时，则休眠时间为`Message.when - 当前时间`，这样在空闲的时候，主线程也不会消耗额外的资源了。而当有新消息入队时，`enqueueMessage()`方法内部会判断是否需要通过`nativeWake()`方法唤醒主线程来处理新消息。唤醒最终时通过往eventfd发起一个写操作，这样主线程就会收到一个可读事件，进而从休眠状态被唤醒。

**【面试官】：你知道IdleHandler吗？**

IdleHandler是通过`MessageQueue.addIdleHandler()`来添加到MessageQueue中的，前面提到，当`MessageQueue.next()`获取消息时，如果当前没有需要处理的消息，就会进入休眠，而在进入休眠之前呢，就会调用IdleHandler接口的`queueIdle()`方法。这个方法的返回值为true时，则调用后保留，下次队列空闲时还会继续调用；而如果返回值为false，调用之后就被remote了。可以用来做延时加载，而且是在空闲时加载，不像`Handler.postDelayed()`需要指定延时的时间。

**【面试官】：可以可以，我们再来聊点儿别的。**

> 相关知识点：
>
> [Android同步屏障机制](https://juejin.cn/post/6915339994234126349/)

## 四大组件

> API：28
>

### Activity

#### 当调用startActivity之后，都发生了什么？

startActivity主要就是应用进程与system_server进程的AMS通信，AMS是实际来管理Activity组件的，负责处理启动模式，维护Activity栈等工作。startActivity的大概流程就是由应用进程通过IPC调用到AMS，AMS处理完这些工作后再通过IPC回到应用进程，创建Activity的实例，回调Activity的生命周期。

**【面试官】：通过什么实现跨进程的呢？**

都是通过AIDL接口，App进程到system_server进程是通过`IActivityManager.aidl`,systemserver到App进程通过`IApplicationThread.aidl`。

**【面试官】：startActivity时，前后Activity的生命周期是怎样的？**

旧Activity的onPause会先执行，然后新Activity依次执行onCreate、onStart、onResume，随后再执行旧Activity的onStop...

**【面试官】：旧Activity的onPause一定会先执行吗？为什么？**

这主要是由AMS来控制的，它会先后将前一个Activity的onPause事物和新Activity的启动事物发送给App进程，而在App端，由`IApplicationThread.aidl`接收到之后，会入队到ActivityThread.H中的消息队列中，这个也是主线程的消息队列，在队列上自然就实现了先后顺序的控制。

**【面试官】：了解插件化吗？知道怎么启动一个插件中的Activity吗？**

主要需要解决的问题是Activity未在manifest中注册的问题，因为在AMS中会检查Activity是否注册，而这个检查逻辑处于system_server进程，我们是无法hook的。可以在manifest中提前注册一个占位Activity，然后startActivity时进入到system_server进程之前，hook把未注册的Activity改为占位Activity，AMS检测就可以通过，然后再回到App进程后，把这个占位Activity再换成插件Activity。

**【面试官】：可以可以，我们再来聊点儿别的。**