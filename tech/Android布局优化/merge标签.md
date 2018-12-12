### 通过merge标签减少View层级

> 参考资料：https://blog.csdn.net/a740169405/article/details/50473909

merge翻译成中文是合并的意思，在Android中通过使用merge能够减少视图的节点数，  从而减少视图在绘制过程消耗的时间，达到提高UI性能的效果。使用merge时通常需要注意以下几点： 

1. **merge必须放在布局文件的根节点上。**
2. **merge并不是一个ViewGroup，也不是一个View，它相当于声明了一些视图，等待被添加。**
3. **merge标签被添加到A容器下，那么merge下的所有视图将被添加到A容器下。**
4. **因为merge标签并不是View，所以在通过`LayoutInflate.inflate`方法渲染的时候， 第二个参数必须指定一个父容器，且第三个参数必须为true，也就是必须为merge下的视图指定一个父亲节点。**
5. **如果Activity的布局文件根节点是`FrameLayout`，可以替换为merge标签，这样，执行`setContentView`之后，会减少一层`FrameLayout`节点。**
6. **自定义View如果继承`LinearLayout`，建议让自定义View的布局文件根节点设置成merge，这样能少一层结点。**
7. **因为merge不是View，所以对merge标签设置的所有属性都是无效的。**



merge标签被添加到A容器下，那么merge下的所有视图将被添加到A容器下。

merge标签被添加到A容器下，那么merge下的所有视图将被添加到A容器下。

merge标签被添加到A容器下，那么merge下的所有视图将被添加到A容器下。