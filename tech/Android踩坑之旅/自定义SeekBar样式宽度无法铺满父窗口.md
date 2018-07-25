在自定义`SeekBar`样式时，发现了一个怪异的现象：虽然设置了`SeekBar`的宽度为"match_parent"，但是，`SeekBar`的两端距离父窗口还有一定的距离......百思不得其解。

经过查询发现，可以在xml布局文件中设置`SeekBar`的"paddingLeft"和"paddingRight"来解决该问题，代码如下：

```
<SeekBar
        android:id="@+id/watch_video_sb"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/headline_rl"
        android:maxHeight="4dp"
        android:minHeight="4dp"
        android:paddingLeft="0dp"
        android:paddingRight="0dp"
        android:progressDrawable="@drawable/item_video_seek_bar_bg"
        android:thumb="@null"
        android:thumbOffset="0dp"/>
```

可是如此设置之后，还不生效；但是在代码中，通过`SeekBar`来设置是可以；代码如下：

```
 mWatchVideoSb.setPadding(0, 0, 0, 0);
```

至于产生该问题的具体原因，只有通过阅读源码才能彻底搞清楚它们之间的差异。