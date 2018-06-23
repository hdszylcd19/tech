> 参考资料：
>
> [关于 git reset 命令几个常用参数的理解](https://blog.csdn.net/hbwindy/article/details/51519999)

在实际使用过程中，有可能有误操作，本地git仓库有错误提交。这时，就需要用到撤销commit的操作啦。

### 1. 找到commit id

执行`git log`命令，找到想要撤销的commit id；例如：

```
commit 01d1c0aa9e43f70d5e3a39ed1d53e1f0c23b17a6 // 错误提交记录
Author: hdszylcd19 <hdszylcd19@126.com>
Date:   Fri Jun 8 11:05:07 2018 +0800
    新增 ‘

commit 32d46e86a08f6cded1093430df81e31c2fd0eaf0 // 需要回退到的commit_id
Author: hdszylcd19 <hdszylcd19@126.com>
Date:   Fri Jun 8 09:34:21 2018 +0800
 	新增 ‘语音识别’目录；修改‘volatile关键字’文档

```

### 2. 执行撤销命令

`git reset --hard commit_id`

这个命令 **非常危险** ，是 git 中少有的几个会丢失信息的操作。它会把回退点之前的所有信息都删掉，一个不留，干干净净。  

`git reset --soft commit_id  `

回退到某个版本，只回退了commit的信息，不会恢复到index file一级。如果还要提交，直接commit即可； 

``git reset --mixed commit_id ``

这也是 `git reset` 的默认参数 ，完成Commit命令的撤销，但是不对代码修改进行撤销，可以直接通过git commit 重新提交对本地代码的修改 。