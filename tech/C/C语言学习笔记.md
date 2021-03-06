---

---

# C语言学习笔记

> [《C语言学习笔记在线文档》](https://www.yuque.com/docs/share/627f3ddd-75ea-47ba-903b-5ec75a1784f1?# 《C语言学习笔记》)

## 关键字

[ANSI C标准](https://baike.baidu.com/item/ANSI%20C%E6%A0%87%E5%87%86/6044290)C语言共有32个关键字，9种控制语句；这些关键字如下：

### 基本数据类型关键字（5个）

| 基本数据类型 |                            释义                            |
| :----------: | :--------------------------------------------------------: |
|     void     | 声明函数无返回值或无参数，声明无类型指针，显式丢弃运算结果 |
|     char     |             字符型类型数据，属于整型数据的一种             |
|     int      |            整型数据，通常为编译器指定的机器字长            |
|    float     |            单精度浮点型数据，属于浮点数据的一种            |
|    double    |            双精度浮点型数据，属于浮点数据的一种            |

### 类型修饰关键字（4个）

|          |                     释义                     |
| :------: | :------------------------------------------: |
|  short   | 修饰int，短整型数据类型，可以省略被修饰的int |
|   long   | 修饰int，长整型数据类型，可以省略被修饰的int |
|  signed  |         修饰整型数据，有符号数据类型         |
| unsigned |         修饰整型数据，无符号数据类型         |

### 复杂类型关键字（5个）

|         |               释义               |
| :-----: | :------------------------------: |
| struct  |            结构体声明            |
|  union  |            共用体声明            |
|  enum   |             枚举声明             |
| typedef |           声明类型别名           |
| sizeof  | 得到特定类型或特定类型变量的大小 |

### 存储级别关键字（6个）

|          |                             释义                             |
| :------: | :----------------------------------------------------------: |
|   auto   |    指定为自动变量，由编译器自动分配及释放。通常在栈上分配    |
|  static  | 指定为静态变量，分配在静态变量区，修饰函数时，指定函数作用域为文件内部 |
| register | 指定为寄存器变量，建议编译器将变量存储到寄存器中使用，也可以修饰函数形参，建议编译器通过寄存器而不是堆栈传递参数 |
|  extern  | 指定对应变量为外部变量，即在另外的目标文件中定义，可以认为是约定由另外文件声明的对象的一个“引用” |
|  const   | 与volatile合称“cv特性”，指定变量不可被当前线程/进程改变（但有可能被系统或其它线程/进程改变） |
| volatile | 与const合称“cv特性”，指定变量的值有可能会被系统或其它进程/线程改变，强制编译器每次从内存中取得该变量的值 |

### 跳转关键字（4个）

|          |                          释义                          |
| :------: | :----------------------------------------------------: |
|  return  | 用于函数体中，返回特定值（或者是void类型，即不返回值） |
| continue |              结束当前循环，开始下一轮循环              |
|  break   |                跳出当前循环或switch结构                |
|   goto   |                     无条件跳转语句                     |

### 条件关键字（5个）

|         |               释义               |
| :-----: | :------------------------------: |
|   if    |             条件语句             |
|  else   | 条件语句否定分支（与if一起使用） |
| switch  |     开关语句（多重分支语句）     |
|  case   |       开关语句中的分支标记       |
| default |   开关语句中的“默认”分支，可选   |

### 循环关键字（3个）

以下循环语句，当循环条件表达式为真则继续循环；为假则跳出循环。

|       |                             释义                             |
| :---: | :----------------------------------------------------------: |
|  for  | for循环结构，for(1;2;3)4;的执行顺序为：1243 -> 243 -> 243...循环，其中2为循环条件 |
|  do   | do循环结构，do 1 while(2);的执行顺序为：121 -> 21 -> 21...循环，其中2为循环条件 |
| while | while循环结构，while(1)2;的执行顺序为：12 -> 12 ->12...循环，其中1为循环条件 |

### C99新增关键字（5个）

1999年12月16日，ISO推出了C99标准，该标准新增了5个C语言关键字：

```c 
inline restrict _Bool _Complex _Imaginary
```

### C11新增关键字（7个）

2011年12月8日，ISO发布C语言的新标准C11，该标准新增了7个C语言关键字：

```c
_Alignas _Alignof _Atomic _Static_assert _Noretrun _Thread_local _Generic
```

## 基本数据类型

C语言并没有严格规定short、int、long的长度，只是做了宽泛的限制：

- short至少占用两个字节；
- int建议为一个机器字长。32位环境下机器字长为4字节，64位环境下机器字长为8字节；
- short的长度不能大于int，long的长度不能小于int。

总结起来，它们的长度（所占字节数）关系为：

``` c
2 <= short <= int <= long
```

**这就意味着，short并不一定真的”短“，long也并不一定真的”长“，它们有可能和int占用相同的字节数。**

在16位环境下，short的长度为2个字节，int也为2个字节，long为4个字节。16位环境多用于单片机和低级嵌入式系统，在PC和服务器上已经见不到了。

对于32位的Windows、Linux和Mac OS，short的长度为2个字节，int为4个字节，long也为4个字节。PC和服务器上的32位系统占有率也在慢慢下降，嵌入式系统使用32位越来越多。

在64位环境下，不同的操作系统会有不同的结果，如下所示：

|                       操作系统                        | short | int  | long |
| :---------------------------------------------------: | :---: | :--: | :--: |
|                 Win64（64位 Windows）                 |   2   |  4   |  4   |
| 类Unix系统（包括Unix、Linux、Mac OS、BSD、Solaris等） |   2   |  4   |  8   |

目前，我们使用较多的PC系统为Win XP、Win 7、Win 8、Win 10、Mac OS、Linux，在这些系统中，short和int的长度都是固定的，分别为2和4，大家可以放心使用，只有long的长度在Win64和类Unix系统下会有所不同，使用时要注意移植性。

以本机（Win 10家庭中文版）为例，测得C语言基本数据类型所占长度如下：

| 基本数据类型 |       说明       | 占用空间（byte） |
| :----------: | :--------------: | :--------------: |
|     char     |   字符数据类型   |        1         |
|    short     |    短整数类型    |        2         |
|     int      |     整数类型     |        4         |
|     long     |    长整数类型    |        4         |
|  long long   |  更长的整数类型  |        8         |
|    float     | 单精度浮点数类型 |        4         |
|    double    | 双精度浮点数类型 |        8         |

##  printf()

在C语言中，有三个函数可以用来在显示器上输出数据，它们分别是：

- puts()：只能输出字符串，并且输出结束后会自动换行。
- putchar()：只能输出单个字符。
- printf()：可以输出各种类型的数据。

printf()是最灵活、最 复杂、最常用的输出函数，完全可以替代puts()和putchar()。

首先汇总一下printf()的格式控制符：

|             格式控制符              | 说明                                                         |
| :---------------------------------: | :----------------------------------------------------------- |
|                 %c                  | 输出一个单一的字符                                           |
|            %hd、%d、%ld             | 以十进制、有符号的形式输出short、int、long类型的整数         |
|            %hu、%u、%lu             | 以十进制、无符号的形式输出short、int、long类型的整数         |
|            %ho、%o、%lo             | 以八进制、不带前缀、无符号的形式输出short、int、long类型的整数 |
|           %#ho、%#o、%#lo           | 以八进制、带前缀、无符号的形式输出short、int、long类型的整数 |
|    %hx、%x、%lx<br/>%hX、%X、%lX    | 以十六进制、不带前缀、无符号的形式输出short、int、long类型的整数。如果x小写，那么输出的十六进制数字也小写；如果X大写，那么输出的十六进制数字和前缀都大写。 |
| %#hx、%#x、%#lx<br/>%#hX、%#X、%#lX | 以十六进制、带前缀、无符号的形式输出short、int、long类型的整数。如果x小写，那么输出的十六进制数字和前缀都小写；如果X大写，那么输出的十六进制数字和前缀都大写。 |
|               %f、%lf               | 以十进制的形式输出float、double类型的小数                    |
|         %e、%le<br/>%E、%lE         | 以指数形式输出float、double类型的小数。如果e小写，那么输出结果中的e也小写；如果E大写，那么输出结果中的E也大写。 |
|         %g、%lg<br/>%G、%lG         | 以十进制和指数中较短的形式输出float、double类型的小数，并且小数部分的最后不会添加多余的0。如果g小写，那么当以指数形式输出时，e也小写；如果G大写，那么当以指数形式输出时，E也大写。 |
|                 %s                  | 输出一个字符串                                               |

### printf()的高级用法

假如，现在我们要输出一个4*4的整数矩阵，为了增强阅读性，数字要对齐，怎么办呢？我们显然可以这样做：

```c
#include <stdio.h>
int main()
{
    int a1=20, a2=345, a3=700, a4=22;
    int b1=56720, b2=9999, b3=20098, b4=2;
    int c1=233, c2=205, c3=1, c4=6666;
    int d1=34, d2=0, d3=23, d4=23006783;
    printf("%d        %d       %d       %d\n", a1, a2, a3, a4);
    printf("%d     %d      %d     %d\n", b1, b2, b3, b4);
    printf("%d       %d       %d         %d\n", c1, c2, c3, c4);
    printf("%d        %d         %d        %d\n", d1, d2, d3, d4);
    return 0;
}
```

运行结果：

```c
20        345       700       22
56720     9999      20098     2
233       205       1         6666
34        0         23        23006783
```

注：矩阵一般在大学的《高等数学》中会讲到，`m*n`的数字矩阵，可以理解为把`m*n`个数字，摆放成m行n列的样子。

看，这是多么地自虐，要敲那么多空格，还要严格控制空格数，否则输出就会错位。更加恶心的是，如果数字的位数变了，空格的数目也要跟着变。例如，当 a1 的值是 20 时，它后面要敲八个空格；当 a1 的值是 1000 时，它后面就要敲六个空格。每次修改整数的值，都要考虑修改空格的数目，逼死强迫症。

类似的需求随处可见，整齐的格式会更加美观，让人觉得生动有趣。其实，我们大可不必像上面一样，printf() 可以更好的控制输出格式。更改上面的代码：

```c
#include <stdio.h>
int main()
{
    int a1=20, a2=345, a3=700, a4=22;
    int b1=56720, b2=9999, b3=20098, b4=2;
    int c1=233, c2=205, c3=1, c4=6666;
    int d1=34, d2=0, d3=23, d4=23006783;
    printf("%-9d %-9d %-9d %-9d\n", a1, a2, a3, a4);
    printf("%-9d %-9d %-9d %-9d\n", b1, b2, b3, b4);
    printf("%-9d %-9d %-9d %-9d\n", c1, c2, c3, c4);
    printf("%-9d %-9d %-9d %-9d\n", d1, d2, d3, d4);
    return 0;
}
```

输出结果：

```c
20        345       700       22
56720     9999      20098     2
233       205       1         6666
34        0         23        23006783
```

这样写起来更加方便，即使改变某个数字，也无需修改printf()语句，增加或者减少空格数目。

`%-9d`中，`d`表示以十进制输出；`9`表示最少占9个字符的宽度，宽度不足时，以空格补齐；`-`表示左对齐。`%-9d`表示以十进制输出，左对齐，宽度最小为9个字符。

printf()格式控制符的完整形式如下：

```c
%[flag][width][.precision]type
```

注：[]表示此处的内容可有可无，是可以省略的。

#### type

type表示输出类型，比如%d、%f、%c、%lf，type就分别对应d、f、c、lf；例如，`%-9d`中type对应d。type这一项必须有，这意味着输出时，必须要知道是什么类型。

#### [width]

width表示最小输出宽度，也就是至少占用几个字符的位置；例如，`%-9d`中width对应9，表示输出结果最少占用9个字符的宽度。当输出结果的宽度不足width时，以空格补齐（如果没有指定对齐方式，默认会在左边补齐空格）；当输出结果的宽度超过width时，width不再起作用，按照数据本身的宽度来输出。

下面的代码演示了width的用法：

```c
#include <stdio.h>
int main(){
    int n = 234;
    float f = 9.8;
    char c = '@';
    char *str = "http://c.biancheng.net";
    printf("%10d%12f%4c%8s", n, f, c, str);
    return 0;
}
```

运行结果：

```c
       234    9.800000   @http://c.biancheng.net
```

对输出结果的说明：

- n的指定输出宽度为10,234的宽度为3，所以前边要补上7个空格。
- f的指定输出宽度为12，9.800000的宽度为8，所以前边要补上4个空格。
- str的指定输出宽度为8，"http://c.biancheng.net" 的宽度为 22，超过了 8，所以指定输出宽度不再起作用，而是按照 str 的实际宽度输出。

#### [.precision]

`.precision`表示输出精度，也就是小数的位数。

- 当小数部分的位数大于precision时，会按照四舍五入的原则丢掉多余的数字；
- 当小数部分的位数小于precision时，会在后面补0。

另外，`.precision`也可以用于整数和字符串，但是功能却是相反的；

- 用于整数时，`.precision`表示最小输出宽度。与width不同的是，整数的宽度不足时会在左边补0，而不是补空格。
- 用于字符串时，`.precision`表示最大输出宽度，或者说截取字符串。当字符串的长度大于precision时，会截掉多余的字符；当字符串的长度小于precision时，`.precision`就不再起作用。

请看下面的例子：

```c
#include <stdio.h>
int main(){
    int n = 123456;
    double f = 882.923672;
    char *str = "abcdefghi";
    printf("n: %.9d  %.4d\n", n, n);
    printf("f: %.2lf  %.4lf  %.10lf\n", f, f, f);
    printf("str: %.5s  %.15s\n", str, str);
    return 0;
}
```

运行结果：

```c
n: 000123456  123456
f: 882.92  882.9237  882.9236720000
str: abcde  abcdefghi
```

对输出结果的说明：

- 对于n，`.precision`表示最小输出宽度。n本身的宽度为6，当precision为9时，大于6，要在n的前面补3个0；当precision为4时，小于6，不再起作用。
- 对于f，`.precision`表示输出精度。f的小数部分有6位数字，当precision为2或者4时，都小于6，要按照四舍五入的原则截断小数；当precision为10时，大于6，要在小数的后面补四个0。
- 对于str，`.precision`表示最大输出宽度。str本身的宽度为9，当precision为5时，小于9，要截取str的前5个字符；当precision为15时，大于9，不再起作用。

#### [flag]

flag是标志字符。例如，`%#x`中flag对应`#`；`%-9d`中flag对应`-`。下表列出了printf()可以用的flag：

| 标志字符 | 含义                                                         |
| :------: | :----------------------------------------------------------- |
|    -     | `-`表示左对齐。如果没有，就按照默认的对齐方式，默认一般为右对齐。 |
|    +     | 用于整数或者小数，表示输出符号（正负号）。如果没有，那么只有负数才会输出符号。 |
|   空格   | 用于整数或者小数，输出值为正时冠以空格，为负时冠以负号。     |
|    #     | 对于八进制（%o）和十六进制（%x/%X）整数，#表示在输出时添加前缀；八进制的前缀是0，十六进制的前缀是0x/0X。<br/>对于小数（%f/%e/%g），#表示强迫输出小数点。如果没有小数部分，默认是不输出小数点的，加上`#`以后，即使没有小数部分，也会带上小数点。 |

请看下面的例子：

```c
#include <stdio.h>
int main(){
    int m = 192, n = -943;
    float f = 84.342;
    printf("m=%10d, m=%-10d\n", m, m);  //演示 - 的用法
    printf("m=%+d, n=%+d\n", m, n);  //演示 + 的用法
    printf("m=% d, n=% d\n", m, n);  //演示空格的用法
    printf("f=%.0f, f=%#.0f\n", f, f);  //演示#的用法
    return 0;
}
```

运行结果：

```c
m=       192, m=192      
m=+192, n=-943
m= 192, n=-943
f=84, f=84.
```

对输出结果的说明：

- 当以`%10d`输出m时，是右对齐，所以在192前面补7个空格；当以`%-10d`输出m时，是左对齐，所以在192后面补7个空格。
- m是正数，以`%+d`输出时要带上正号；n是负数，以`%+d`输出时要带上负号。
- m是正数，以`% d`输出时要在前面加空格；n是负数，以`% d`输出时要在前面加负号。
- `%.0f`表示保留0位小数，也就是只输出整数部分，不输出小数部分。默认情况下，这种输出形式是不带小数点的，但是如果有了`#`标志，那么就要在整数的后面“硬加上”一个小数点，以和纯整数区分开。

## 随机数

在C语言中，我们一般使用<stdlib.h>头文件中的rand()函数来生成随机数，它的用法为：

```c
int rand(void);
```

void表示不需要传递参数。C语言中还有一个random()函数可以获取随机数，但是random()不是标准函数，不能在VC/VS等编译器通过，所以比较少用。

rand()会随机生成一个位于0~RAND_MAX之间的整数。

RAND_MAX是<stdlib.h>头文件中的一个宏，它用来指明rand()所能返回的随机数的最大值。C语言标准并没有规定RAND_MAX的具体数值，只是规定它的值至少为32767。在实际编程中，我们也不需要知道RAND_MAX的具体值，把它当作一个很大的数来对待即可。

下面是一个随机数生成的实例：

```c
#include <stdio.h>
#include <stdlib.h>
int main(){
    int a = rand();
    printf("%d\n",a);
    return 0;
}
```

### 随机数的本质

多次运行上面的代码，你会发现每次产生的随机数都一样，这是怎么回事呢？为什么随机数并不随机呢？

实际上，rand()函数产生的随机数是伪随机数，是根据一个数值按照某个公式推算出来的，这个数值我们称之为“种子”。种子和随机数之间的关系是一种正态分布，如下图所示：

![正态分布](./imgs/正态分布.gif)

种子在每次启动计算机时是随机的，但是一旦计算机启动以后它就不再变化了。也就是说，每次启动计算机以后，种子就是定值了，所以根据公式推算出来的结果（也就是生成的随机数）就是固定的。

### 重新播种

我们可以通过srand()函数来重新“播种”，这样种子就会发生改变。srand()的用法为：

```c
void srand (unsigned int seed);
```

它需要一个unsigned int类型的参数。在实际开发中，我们可以用时间作为参数，只要每次播种的时间不同，那么生成的种子就不同，最终的随机数也就不同。

使用<time.h>头文件中的time()函数即可得到当前的时间（精确到秒），就像下面这样：

```c
srand((unsigned)time(NULL));
```

对上面的代码进行修改，生成随机数之前先进行播种：

```c
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
int main() {
    int a;
    srand((unsigned)time(NULL));
    a = rand();
    printf("%d\n", a);
    return 0;
}
```

多次运行程序，会发现每次生成的随机数都不一样了。但是，这些随机数会有逐渐增大或者减小的趋势，这是因为我们以时间为种子，时间是逐渐增大的，结合上面的正态分布图，很容易推断出随机数也会逐渐增大或者减小。

### 生成一定范围内的随机数

在实际开发中，我们往往需要一定范围内的随机数，过大或者过小都不符合要求，那么，如何产生一定范围内的随机数呢？我们可以利用取模的方法：

```c
int a = rand() % 10;    //产生0~9的随机数，注意10会被整除
```

如果要规定上下限：

```c
int a = rand() % 51 + 13;    //产生13~63的随机数
```

分析：取模即取余，rand() % 51 + 13我们可以看成两部分：rand() % 51是产生0~50的随机数；后面`+13`保证a的最小值只能为13，最大值就是50+13=63；

最后给出产生13~63范围内随机数的完整代码：

```c
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
int main(){
    int a;
    srand((unsigned)time(NULL));
    a = rand() % 51 + 13;
    printf("%d\n",a);
    return 0;
}
```

## const

有时候我们希望定义这样一种变量，它的值不能被改变，在整个作用域中都保持固定。例如，用一个变量来表示班级的最大人数，或者表示缓冲区的大小。为了满足这一要求，可以使用`const`关键字对变量加以限定：

```c
const int MaxNum = 100;  //班级的最大人数
```

这样 MaxNum 的值就不能被修改了，任何对 MaxNum 赋值的行为都将引发错误：

```c
MaxNum = 90;  //错误，试图向 const 变量写入数据
```

我们经常将 const 变量称为常量（Cons[tan](http://c.biancheng.net/ref/tan.html)t）。创建常量的格式通常为：

```c
const type name = value;
```

const 和 type 都是用来修饰变量的，它们的位置可以互换，也就是将 type 放在 const 前面：

```c
type const name = value;
```

但我们通常采用第一种方式，不采用第二种方式。另外建议将常量名的首字母大写，以提醒程序员这是个常量。

由于常量一旦被创建后其值就不能再改变，所以常量必须在定义的同时赋值（初始化），后面的任何赋值行为都将引发错误。一如既往，初始化常量可以使用任意形式的表达式，如下所示：

```c
#include <stdio.h>
int getNum(){
    return 100;
}
int main(){
    int n = 90;
    const int MaxNum1 = getNum();  //运行时初始化
    const int MaxNum2 = n;  //运行时初始化
    const int MaxNum3 = 80;  //编译时初始化
    printf("%d, %d, %d\n", MaxNum1, MaxNum2, MaxNum3);
    return 0;
}
```

运行结果：
100, 90, 80

### const和指针

const 也可以和指针变量一起使用，这样可以限制指针变量本身，也可以限制指针指向的数据。const 和指针一起使用会有几种不同的顺序，如下所示：

```c 
const int *p1;
int const *p2;
int * const p3;
```

**在最后一种情况下，指针是只读的，也就是 p3 本身的值不能被修改；**

**在前面两种情况下，指针所指向的数据是只读的，也就是 p1、p2 本身的值可以修改（指向不同的数据），但它们指向的数据不能被修改。**

当然，指针本身和它指向的数据都有可能是只读的，下面的两种写法能够做到这一点：

```c
const int * const p4;
int const * const p5;
```

const 和指针结合的写法多少有点让初学者摸不着头脑，大家可以这样来记忆：

**const 离变量名近就是用来修饰指针变量的，离变量名远就是用来修饰指针指向的数据，如果近的和远的都有，那么就同时修饰指针变量以及它指向的数据。**

### const和函数形参

在C语言中，单独定义 const 变量没有明显的优势，完全可以使用`#define`命令代替。

**const 通常用在函数形参中，如果形参是一个指针，为了防止在函数内部修改指针指向的数据，就可以用 const 来限制。**

在C语言标准库中，有很多函数的形参都被 const 限制了，下面是部分函数的原型：

```c
size_t strlen ( const char * str );
int strcmp ( const char * str1, const char * str2 );
char * strcat ( char * destination, const char * source );
char * strcpy ( char * destination, const char * source );
int system (const char* command);
int puts ( const char * str );
int printf ( const char * format, ... );
```

我们自己在定义函数时也可以使用 const 对形参加以限制，例如查找字符串中某个字符出现的次数：

```c
#include <stdio.h>
size_t strnchr(const char *str, char ch){
    int i, n = 0, len = strlen(str);
    for(i=0; i<len; i++){
        if(str[i] == ch){
            n++;
        }
    }
   
    return n;
}
int main(){
    char *str = "http://c.biancheng.net";
    char ch = 't';
    int n = strnchr(str, ch);
    printf("%d\n", n);
    return 0;
}
```

运行结果：
3

根据 strnchr() 的功能可以推断，函数内部要对字符串 str 进行遍历，不应该有修改的动作，用 const 加以限制，不但可以防止由于程序员误操作引起的字符串修改，还可以给用户一个提示，函数不会修改你提供的字符串，请你放心。

### const和非const类型转换

当一个指针变量 str1 被 const 限制时，并且类似`const char *str1`这种形式，说明指针指向的数据不能被修改；如果将 str1 赋值给另外一个未被 const 修饰的指针变量 str2，就有可能发生危险。因为通过 str1 不能修改数据，而赋值后通过 str2 能够修改数据了，意义发生了转变，所以编译器不提倡这种行为，会给出错误或警告。

也就是说，`const char *`和`char *`是不同的类型，不能将`const char *`类型的数据赋值给`char *`类型的变量。但反过来是可以的，编译器允许将`char *`类型的数据赋值给`const char *`类型的变量。

这种限制很容易理解，`char *`指向的数据有读取和写入权限，而`const char *`指向的数据只有读取权限，降低数据的权限不会带来任何问题，但提升数据的权限就有可能发生危险。

C语言标准库中很多函数的参数都被 const 限制了，但我们在以前的编码过程中并没有注意这个问题，经常将非 const 类型的数据传递给 const 类型的形参，这样做从未引发任何副作用，原因就是上面讲到的，将非 const 类型转换为 const 类型是允许的。

下面是一个将 const 类型赋值给非 const 类型的例子：

```c
#include <stdio.h>
void func(char *str){ }
int main(){
    const char *str1 = "c.biancheng.net";
    char *str2 = str1;
    func(str1);
    return 0;
}
```

第7、8行代码分别通过赋值、传参（传参的本质也是赋值）将 const 类型的数据交给了非 const 类型的变量，编译器不会容忍这种行为，会给出警告，甚至直接报错。

## struct

C语言结构体（Struct）从本质上讲是一种自定义的数据类型，只不过这种数据类型比较复杂，是由 int、char、float 等基本类型组成的。你可以认为结构体是一种聚合类型。

在实际开发中，我们可以将一组类型不同的、但是用来描述同一件事物的变量放到结构体中。例如，在校学生有姓名、年龄、身高、成绩等属性，学了结构体后，我们就不需要再定义多个变量了，将它们都放到结构体中即可。

### 字节对齐

问大家一个问题：

```c
struct STUDENT
{
    char a;
    int b;
}data;
```

如上结构体变量 data 占多少字节？char 占 1 字节，int 占 4 字节，所以总共占 5 字节吗？我们写一个程序验证一下：

```c
# include <stdio.h>
struct STUDENT
{
    char a;
    int b;
}data;
int main(void)
{
    printf("%p, %p\n", &data.a, &data.b);  //%p是取地址输出控制符
    printf("%d\n", sizeof(data));
    return 0;
}
```

输出结果是：
00427E68, 00427E6C
8

我们看到 data 不是占 5 字节，而是占 8 字节。变量 a 的地址是从 00427E68 到 00427E6B，占 4字 节；变量 b 的地址是从 00427E6C 到 00427E6F，也占 4 字节。b 占 4 字节我们能理解，但 a 是 char 型，char 型不是占 1 字节吗，这里为什么占 4 字节？其实不是它占了 4 字节，它占的还是 1 字节，只不过结构体中有一个字节对齐的概念。

什么叫字节对齐？我们知道结构体是一种构造数据类型，里面可以有不同数据类型的成员。在这些成员中，不同的数据类型所占的内存空间是不同的。那么系统是怎么给结构体变量的成员分配内存的呢？或者说这些成员在内存中是如何存储的呢？通过上面这个例子我们知道肯定不是顺序存储的。

那么到底是怎么存储的呢？就是按字节对齐的方式存储的！即以结构体成员中占内存最多的数据类型所占的字节数为标准，所有的成员在分配内存时都要与这个长度对齐。我们举一个例子：我们以上面这个程序为例，结构体变量 data 的成员中占内存最多的数据类型是 int 型，其占 4 字节的内存空间，那么所有成员在分配内存时都要与 4 字节的长度对齐。也就是说，虽然 char 只占 1 字节，但是为了与 4 字节的长度对齐，它后面的 3 字节都会空着，即：

![两个变量](./imgs/struct_student_1.png)

所谓空着其实也不是里面真的什么都没有，它就同定义了一个变量但没有初始化一样，里面是一个很小的、负的填充字。为了便于表达，我们就暂且称之为空好了。

如果结构体成员为：

```c
struct STUDENT
{
    char a;
    char b;
    int c;
}data;
```

那么这三个成员是怎么对齐的？a 和 b 后面都是空 3 字节吗？不是！如果没有 b，那么 a 后面就空 3 字节，有了 b 则 b 就接着 a 后面填充。即：

![三个成员](./imgs/struct_student_2.png)

所以这时候结构体变量 data 仍占 8 字节。我们写一个程序验证一下：

```c
# include <stdio.h>
struct STUDENT
{
    char a;
    char b;
    int c;
}data;
int main(void)
{
    printf("%p, %p, %p\n", &data.a, &data.b, &data.c);  //%p是取地址输出控制符
    printf("%d\n", sizeof(data));
    return 0;
}
```

输出结果是：
00427E68, 00427E69, 00427E6C
8

这时我们发现一个问题：所有成员在分配内存的时候都与 4 字节的长度对齐，多个 char 类型时是依次往后填充，但是 char 型后面的 int 型为什么不紧接着后面填充？为什么要另起一行？也就是说，到底什么时候是接在后面填充，什么时候是另起一行填充？

我们说，所有的成员在分配内存时都要与所有成员中占内存最多的数据类型所占内存空间的字节数对齐。假如这个字节数为 N，那么对齐的原则是：理论上所有成员在分配内存时都是紧接在前一个变量后面依次填充的，但是如果是“以 N 对齐”为原则，那么，如果一行中剩下的空间不足以填充某成员变量，即剩下的空间小于某成员变量的数据类型所占的字节数，则该成员变量在分配内存时另起一行分配。

下面再来举一个例子，大家觉得下面这个结构体变量data占多少字节？

```c
struct STUDENT
{
    char a;
    char b;
    char c;
    char d;
    char e;
    int f;
}data;
```

首先最长的数据类型占 4 字节，所以是以 4 对齐。然后 a 占 1 字节，b 接在 a 后面占 1 字节，c 接在 b 后面占 1 字节，d 接在 c 后面占 1 字节，此时满 4 字节了，e 再来就要另起一行。f 想紧接着 e 后面分配，但 e 后面还剩 3 字节，小于 int 类型的 4 字节，所以 f 另起一行。即该结构体变量分配内存时如下：

![6个成员](./imgs/struct_student_3.png)

即总共占 12 字节。我们写一个程序验证一下：

```c
# include <stdio.h>
struct STUDENT
{
    char a;
    char b;
    char c;
    char d;
    char e;
    int f;
}data;
int main(void)
{
    printf("%p, %p, %p, %p, %p, %p\n", &data.a, &data.b, &data.c, &data.d, &data.e, &data.f);  //%p是取地址输出控制符
    printf("%d\n", sizeof(data));
    return 0;
}
```

输出结果是：
00427E68, 00427E69, 00427E6A, 00427E6B, 00427E6C, 00427E70
12

现在大家应该能掌握字节对齐的精髓了吧！下面给大家出一个题目试试掌握情况。我们将前面的结构体改一下：

```c
struct STUDENT
{
    char a;
    int b;
    char c;
}data;
```

即将原来第二个和第三个声明交换了位置，大家看看现在 data 变量占多少字节？没错，是 12 字节。首先最长类型所占字节数为 4，所以是以 4 对齐。分配内存的时候 a 占 1 字节，然后 b 想紧接着 a 后面存储，但 a 后面还剩 3 字节，小于 b 的 4 字节，所以 b 另起一行分配。然后 c 想紧接着 b 后面分配，但是 b 后面没空了，所以 c 另起一行分配。所以总共 12 字节。内存分配图如下所示：

![3个成员](./imgs/struct_student_4.png)

下面写一个程序验证一下：

```c
# include <stdio.h>
struct STUDENT
{
    char a;
    int b;
    char c;
}data;
int main(void)
{
    printf("%p, %p, %p\n", &data.a, &data.b, &data.c);  //%p是取地址输出控制符
    printf("%d\n", sizeof(data));
    return 0;
}
```

输出结果是：
00427E68, 00427E6C, 00427E70
12

我们看到，同样三个数据类型，只不过交换了一下位置，结构体变量data所占的内存空间就由8字节变成12字节，多了4字节。这就告诉我们，在声明结构体类型时，各类型成员的前后位置会对该结构体类型定义的结构体变量所占的字节数产生影响。没有规律的定义会增加系统给结构体变量分配的字节数，降低内存分配的效率。但这种影响对操作系统来说几乎是可以忽略不计的！所以我们在写程序的时候，如果有心的话，声明结构体类型时就按成员类型所占字节数从小到大写，或从大到小写。但是如果没有按规律书写的话也不要紧，声明结构体类型时并非一定要从小到大声明，只是为了说明“字节对齐”这个概念！而且有时候为了增强程序的可读性我们就需要没有规律地写，比如存储一个人的信息：

```c
struct STUDENT
{
    char name[10];
    int age;
    char sex;
    float score;
}data;
```

正常的思维是将“性别”放在“年龄”后面，但如果为了内存对齐而交换它们的位置，总让人觉得有点别扭。所以我说“尽量”有规律地写！

这时又有人会提出一个问题：“上面这个结构体变量 data 中有成员 char name[10]，长度最长，是 10，那是不是要以 10 对齐？”不是，char a[10] 的本质是 10 个 char 变量，所以就把它当成 10 个 char 变量看就行了。所以结构体变量 data 中成员最长类型占 4 字节，还是以 4 对齐。该结构体变量分配内存时情况如下：

![4个成员](./imgs/struct_student_5.png)

总共 24 字节，我们写一个程序验证一下：

```c
# include <stdio.h>
struct STUDENT
{
    char name[10];
    int age;
    char sex;
    float score;
}data;
int main(void)
{
    printf("%p, %p, %p, %p, %p, %p, %p, %p, %p, %p, %p, %p, %p\n", &data.name[0], &data.name[1], &data.name[2], &data.name[3], &data.name[4], &data.name[5], &data.name[6], &data.name[7], &data.name[8], &data.name[9], &data.age, &data.sex, &data.score);
    printf("%d\n", sizeof(data));
    return 0;
}
```

输出结果是：
00427E68, 00427E69, 00427E6A, 00427E6B, 00427E6C, 00427E6D, 00427E6E,
00427E6F, 00427E70, 00427E71, 00427E74, 00427E78, 00427E7C
24

### 总结

一个结构体占用多大的内存空间？

1. **首先，要看这个结构体中，所占空间最大的那个成员变量，假设所占空间大小为MAX(单位字节)；其它的变量都要按照MAX大小，进行内存对齐；**
2. **然后，从第一个变量开始依次分配内存空间；当其它变量开始分配之前，要先确认，上一个变量是否已经把MAX整数倍的空间占满，如果没有，则要检查剩余的空间够不够存储当前变量，如果足够，则紧挨着上一个变量地址后面分配；如果不够，则另起一行进行分配；另起一行的意思就是，变量的起始地址值为MAX的整数倍；**
3. **结构体所占内存空间为MAX的整数倍。**