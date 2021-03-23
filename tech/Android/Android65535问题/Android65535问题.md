# Android65535问题

出现这个问题的根本原因是在 DVM 源码中的 MemberIdsSection.java 类中，有如下一段代码：

```java
	@Override
    protected void orderItems() {
        int idx = 0;

        if (items().size() > DexFormat.MAX_MEMBER_IDX + 1) {
            throw new DexIndexOverflowException(getTooManyMembersMessage());
        }

        for (Object i : items()) {
            ((MemberIdItem) i).setIndex(idx);
            idx++;
        }
    }
```

如果items个数超过`DexFormat.MAX_MEMBER_IDX + 1`，则会报错；`DexFormat.MAX_MEMBER_IDX`的值为`0XFFFF(65535)`，items代表dex文件中的方法个数、属性个数、以及类的个数。

也就是说理论上不止方法数，我们在Java文件中声明的变量，或者创建的类的个数如果也超过了`65536（DexFormat.MAX_MEMBER_IDX + 1）`，同样会编译失败；

Android提供了MultiDex来解决这个问题。很多网上的文章说 65535 问题是因为解析 dex 文件到数据结构 DexFile 时，使用了 short 来存储方法的个数，其实这种说法是错误的！