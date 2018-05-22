### 1. 使用git命令修改

 

```
git remote set-url origin [url]

例如：git remote set-url origin git@github.com:hdszylcd19/tech.git
```

### 2. 先删除再添加

```
git remote rm origin //先删除

git remote add origin [url] //后添加
```

 

### 3. 直接修改.git目录下的config文件

```
[core]
	repositoryformatversion = 0
	filemode = false
	bare = false
	logallrefupdates = true
	symlinks = false
	ignorecase = true
[remote "origin"]
	url = git@github.com:hdszylcd19/tech.git //在此修改即可
	fetch = +refs/heads/*:refs/remotes/origin/*
[branch "master"]
	remote = origin
	merge = refs/heads/master
```

