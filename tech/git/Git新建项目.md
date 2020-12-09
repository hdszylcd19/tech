# Git新建项目

##### Git global setup

```
git config --global user.name "姬林"
git config --global user.email "jl@100fen.cn"
```

##### Create a new repository

```
git clone git@192.168.5.250:root/document-browsing-tool.git
cd document-browsing-tool
touch README.md
git add README.md
git commit -m "add README"
git push -u origin master
```

##### Existing folder

```
cd existing_folder
git init
git remote add origin git@192.168.5.250:root/document-browsing-tool.git
git add .
git commit
git push -u origin master
```

##### Existing Git repository

```
cd existing_repo
git remote add origin git@192.168.5.250:root/document-browsing-tool.git
git push -u origin --all
git push -u origin --tags
```

