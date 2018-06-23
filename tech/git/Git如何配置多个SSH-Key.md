### 0. 为何有此需求

在实际项目开发过程中，我们除了有公司gitlab的SSH-key需要配置外，可能还有一些自己的项目放在github之上。在这样的使用情形下就需要配置不同的SSH-Key来对应不同的项目环境。

### 1. 生成SSH-Key

以windows环境为例。打开Git Bash，输入如下命令，生成SSH-Key：

```
ssh-keygen -t rsa -C "your_email@example.com" -f ~/.ssh/id_rsa
```

默认生成SSH-Key在`C:\Users\你的电脑name\.ssh`路径下；当然，你也可以在“-f”后面指定你自己想要生成的路径，不过一般没有必要。以本人电脑为例，即为`C:\Users\OneDay\.ssh`。

其中“id_rsa”可以更改成你自己喜欢的名称，用以区分不同的SSH-Key；默认名称为“id_rsa”。

输入此命令，点击三下“回车键”后，即会在`~/.ssh/`目录下，生成id-rsa和id-rsa.pub；其中id-rsa为私钥，id-rsa.pub为公钥。我们将id-rsa.pub中的内容粘贴到git服务器的SSH-Key的配置中。

**注意：即使是同一个email地址，在不同的电脑上，都需要重新生成SSH-Key；然后再添加到git服务器的SSH-Key配置中。**

我曾想当然地以为，同一个email地址生成的公钥和私钥，可以拿到任意电脑上随意使用......[手动捂脸]其实，并不允许这种操作。我还是太天真啦.....

### 2. 添加私钥

```
ssh-add ~/.ssh/id_rsa $ ssh-add ~/.ssh/github_rsa
```

使用该命令，可以同时添加多个私钥，命令之间，需用“$”隔开；如果执行ssh-add时提示"Could not open a connection to your authentication agent"，可以执行如下命令： 

```
ssh-agent bash
```

然后再运行上面的命令即可。你还可以执行如下操作：

```
# 可以通过 ssh-add -l 来确私钥列表
ssh-add -l
# 可以通过 ssh-add -D 来清空私钥列表
ssh-add -D
```

###  3. 创建并修改配置文件

在 ~/.ssh 目录下通过如下命令，新建一个config文件：

```
touch config
```

打开并编辑“config”文件，添加如下内容：

```
# gitlab
Host gitlab.com
    HostName gitlab.com
    PreferredAuthentications publickey
    IdentityFile ~/.ssh/id_rsa
# github
Host github.com
    HostName github.com
    PreferredAuthentications publickey
    IdentityFile ~/.ssh/github_rsa
```

### 4. 测试

执行如下命令，查看我们配置是否成功：

```
ssh -T git@github.com
```

如果成功输出欢迎语句，则表示配置成功。例如：

```
Hi hdszylcd19! You've successfully authenticated, but GitHub does not provide shell access.
```

这里是以github为例。当然，你也需要试一试链接公司的gitlab，这里就不再展开啦。

如果遇到错误的话，则可以使用如下命令，查看详细信息，定位具体原因：

```
ssh -vT git@github.com
```

