# 谈谈对Zygote的理解

## What：Zygote的作用是什么？

Zygote的作用可以分为两点；**启动SystemServer**和**孵化应用进程**！

Android系统进程启动的常用套路，大致可以分为三步

- 进程启动
- 准备工作
- loop()，不停地接收消息、处理消息。有可能是Socket发过来的、也有可能是MessageQueue里面的消息、也有可能是binder驱动发过来的消息。

## How：Zygote的启动流程是什么？

### Zygote进程是怎么启动的？

Init进程是Linux系统启动后，用户空间的第一个进程。Init进程启动之后，首先会去加载`init.rc`启动配置文件。Zygote就是其中要启动的系统服务之一。

启动进程有两种方式，fork() + handle和fork() + execve系统调用；

**信号处理 - SIGCHLD**

### Zygote进程启动之后做了什么？

#### Zygote的Native世界

启动Android虚拟机

注册Android系统关键类的JNI函数

通过JNI调用进入Java世界

#### Zygote的Java世界

预加载资源（Preload Resources）

启动System Server

进入loop()循环

## Why：Zygote的工作原理是什么？



## 要注意的细节

Zygote fork要单线程

Zygote的IPC（跨进程通信机制）没有采用binder，而是采用的Socket。

## 两个问题

孵化应用进程这种事为什么不交给SystemServer来做，而专门设计一个Zygote？

Zygote的IPC通信机制为什么不采用binder？如果采用binder的话会有什么问题吗？