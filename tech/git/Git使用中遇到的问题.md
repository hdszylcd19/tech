## Git使用中遇到的问题

#### 1.撤销本地分支与远程分支的映射关系

​	在实际使用中，遇到如下异常提示：

```
Your branch is based on 'origin/dev', but the upstream is gone.
(use "git branch --unset-upstream" to fixup)
```

​	出现该提示的原因是：远程分支和本地分支映射关系出了问题；远程分支被删除了，本地分支跟它关联不上造成的；通过下面的git命令重置一下就好了。

```
git branch --unset-upstream
```

#### 2.查看/修改当前配置的用户名和邮箱地址

​	查看用户名和邮箱地址：

```
git config <--global/--local> user.name

git config <--global/--local> user.email
```

​	其中--global用来查看全局的用户名和邮箱地址，--local是用来查看当前仓库的用户名和邮箱地址，默认查看的是当前仓库的用户名和邮箱地址。

​	修改用户名和邮箱地址：

```
git config <--global/--local> user.name "username"

git config <--global/--local> user.email "email"
```

​	其中--global用来修改全局的用户名和邮箱地址，--local是用来修改当前仓库的用户名和邮箱地址，默认修改的是当前仓库的用户名和邮箱地址。