### Service是什么？

简单来说，Service就是提供服务的代码，这些代码最终体现为一个个的接口函数，所以，Service就是实现一组函数的对象，通常也称为组件。Android 的Service 有以下一些特点：

1. 请求Service服务的代码(Client)  和 Service本身(Server) 不在一个线程，很多情况下不在一个进程内。跨进程的服务称为远端(Remote)服务，跨进程的调用称为IPC。通常应用程序通过代理(Proxy)对象来访问远端的Service。
2. Service 可以运行在native 端(C/C++)，也可以运行在Java 端。同样，Proxy 可以从native 端访问Java Service, 也可以从Java端访问native service， 也就是说，service的访问与语言无关。
3. Android里大部分的跨进程的IPC都是基于Binder实现。
4. Proxy 通过 Interface 类定义的接口访问Server端代码。
5. Service可以分为匿名和具名Service. 前者没有注册到ServiceManager, 应用无法通过名字获取到访问该服务的Proxy对象。
6. Service通常在后台线程执行（相对于前台的Activity), 但Service不等同于Thread，Service可以运行在多个Thread上，一般这些Thread称为 Binder Thread.