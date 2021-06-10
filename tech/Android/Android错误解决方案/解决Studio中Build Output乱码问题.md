“双击Shift”打开搜索框，在搜索框中输入`Edit Custom VM Options`,选中第一个选项，打开一个文件，添加如下代码：

```java
# custom Android Studio VM options, see https://developer.android.com/studio/intro/studio-config.html
-Dfile.encoding=UTF-8
```

然后重启Android Studio即可。

**注意：新增的代码，前面不能有空格！否则可能导致AS无法正常打开。**

