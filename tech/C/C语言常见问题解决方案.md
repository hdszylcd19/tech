# C语言常见问题解决方案

## printf()和scanf()乱序

### 问题描述

> 开发环境：CLion 2020.3.1
> Build #CL-203.6682.181, built on December 31, 2020
> Licensed to CLion Evaluator
> Expiration date: March 23, 2021
> Runtime version: 11.0.9.1+11-b1145.63 amd64
> VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
> Windows 10 10.0
> GC: ParNew, ConcurrentMarkSweep
> Memory: 1987M
> Cores: 16
> Registry: run.processes.with.pty=false
> Non-Bundled Plugins: io.zhile.research.ide-eval-resetter
>
> C编译器：gcc (x86_64-posix-seh-rev0, Built by MinGW-W64 project) 8.1.0

在clion中，printf()和scanf()一起使用时，控制台输出顺序会有问题；明明printf()在scanf()之前，却先运行了scanf()，后运行printf()。示例代码如下：

```c 
int main() {
    int n;
    printf("请输入正整数：");
    scanf("%d", &n);

    printf("%d的阶乘为：%u\n", n, calc_factorial(n));
    return 0;
}
```

### 解决方案

在printf()和scanf()之间加上`fflush(stdout);`即可。示例如下：

```c
printf("请输入正整数：");
fflush(stdout);
scanf("%d", &n);
```

### 原因分析

```c
头文件：#include<stdio.h>

定义函数：int fflush(FILE * stream);
函数说明：fflush()会强迫将缓冲区内的数据写回参数stream指定的文件中，如果参数stream为NULL，fflush()会将所有打开的文件数据更新。
返回值：成功返回0，失败返回EOF，错误代码存于errno中。
fflush()也可用于标准输入（stdin）和标准输出（stdout），用来清空标准输入输出缓冲区。
stdin是standard input的缩写，即标准输入，一般是指键盘；标准输入缓冲区即是用来暂存从键盘输入的内容的缓冲区。stdout是standard output 的缩写，即标准输出，一般是指显示器；
标准输出缓冲区即是用来暂存将要显示的内容的缓冲区。

清空标准输出缓冲区，
刷新输出缓冲区，即将缓冲区的东西输出到屏幕上
如果圆括号里是已写打开的文件的指针，则将输出缓冲区的内容写入该指针指向的文件，否则清除输出缓冲区。
这里的stdout是系统定义的标准输出文件指针，默认情况下指屏幕，那就是把缓冲区的内容写到屏幕上。
可是从代码中看不出缓冲区会有什么内容，所以它实际上没有起什么作用
```