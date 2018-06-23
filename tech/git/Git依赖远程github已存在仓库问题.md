在添加本地文件到远程github已存在的仓库中，遇到如下错误信息：

```
! [rejected]        master -> dev (fetch first)
error: failed to push some refs to 'git@github.com:hdszylcd19/tech.git'
hint: Updates were rejected because the remote contains work that you do
hint: not have locally. This is usually caused by another repository pushing
hint: to the same ref. You may want to first integrate the remote changes
hint: (e.g., 'git pull ...') before pushing again.
hint: See the 'Note about fast-forwards' in 'git push --help' for details.
```

经查阅相关资料发现：该问题产生的原因是因为”远程分支上存在本地分支中不存在的提交 “造成的。可以使用如下的命令先把远程分支的代码合并到本地。解决完冲突，再次提交即可。

```
git pull --rebase
```

