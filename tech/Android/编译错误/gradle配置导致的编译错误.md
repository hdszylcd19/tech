该文件用以记录实际开发过程中，因为gradle配置导致的编译错误问题；

# transformClassesWithDexBuilderForDebug

> Studio版本：4.0.2
>
> Gradle版本：4.6
>
> 参考链接：
>
> # [transformClassesWithDexBuilderForDebug](https://stackoverflow.com/questions/52645163/lombok-1-18-2-throws-transformclasseswithdexbuilderfordebug)
>
> # [Android Studio 3.0+ 新Dex编译器D8 Desugar R8](https://www.jianshu.com/p/bb6fb79dab17)

### 问题描述

```groovy
> Task :app:transformClassesWithDexBuilderForDebug FAILED
java.lang.RuntimeException: java.lang.RuntimeException
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	at java.util.concurrent.ForkJoinTask.getThrowableException(ForkJoinTask.java:593)
	at java.util.concurrent.ForkJoinTask.reportException(ForkJoinTask.java:677)
	at java.util.concurrent.ForkJoinTask.join(ForkJoinTask.java:720)
	at com.android.ide.common.internal.WaitableExecutor.waitForTasksWithQuickFail(WaitableExecutor.java:146)
	at com.android.build.gradle.internal.transforms.DesugarIncrementalTransformHelper.getInitalGraphData(DesugarIncrementalTransformHelper.java:162)
	at com.android.build.gradle.internal.transforms.DesugarIncrementalTransformHelper.makeDesugaringGraph(DesugarIncrementalTransformHelper.java:130)
	at com.google.common.base.Suppliers$NonSerializableMemoizingSupplier.get(Suppliers.java:160)
	at com.android.build.gradle.internal.transforms.DesugarIncrementalTransformHelper.getDependenciesPaths(DesugarIncrementalTransformHelper.java:231)
	at com.android.build.gradle.internal.transforms.DexArchiveBuilderTransform.getD8DesugaringCacheInfo(DexArchiveBuilderTransform.java:461)
	at com.android.build.gradle.internal.transforms.DexArchiveBuilderTransform.transform(DexArchiveBuilderTransform.java:375)
	at com.android.build.gradle.internal.pipeline.TransformTask$2.call(TransformTask.java:239)
	at com.android.build.gradle.internal.pipeline.TransformTask$2.call(TransformTask.java:235)
	at com.android.builder.profile.ThreadRecorder.record(ThreadRecorder.java:102)
	at com.android.build.gradle.internal.pipeline.TransformTask.transform(TransformTask.java:230)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.gradle.internal.reflect.JavaMethod.invoke(JavaMethod.java:103)
	at org.gradle.api.internal.project.taskfactory.IncrementalTaskInputsTaskAction.doExecute(IncrementalTaskInputsTaskAction.java:47)
	at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:42)
	at org.gradle.api.internal.project.taskfactory.AbstractIncrementalTaskAction.execute(AbstractIncrementalTaskAction.java:25)
	at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:28)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$5.run(ExecuteActionsTaskExecuter.java:476)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:402)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:394)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:92)
	at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeAction(ExecuteActionsTaskExecuter.java:461)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:444)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.access$200(ExecuteActionsTaskExecuter.java:93)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.execute(ExecuteActionsTaskExecuter.java:237)
	at org.gradle.internal.execution.steps.ExecuteStep.lambda$execute$0(ExecuteStep.java:32)
	at java.util.Optional.map(Optional.java:215)
	at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:32)
	at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:26)
	at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:58)
	at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:35)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:48)
	at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:33)
	at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:39)
	at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:73)
	at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:54)
	at org.gradle.internal.execution.steps.CatchExceptionStep.execute(CatchExceptionStep.java:35)
	at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:51)
	at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:45)
	at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:31)
	at org.gradle.internal.execution.steps.CacheStep.executeWithoutCache(CacheStep.java:208)
	at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:70)
	at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:45)
	at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:49)
	at org.gradle.internal.execution.steps.StoreSnapshotsStep.execute(StoreSnapshotsStep.java:43)
	at org.gradle.internal.execution.steps.StoreSnapshotsStep.execute(StoreSnapshotsStep.java:32)
	at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:38)
	at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:24)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:96)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$0(SkipUpToDateStep.java:89)
	at java.util.Optional.map(Optional.java:215)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:54)
	at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:38)
	at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:76)
	at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:37)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:36)
	at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:26)
	at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:90)
	at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:48)
	at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:69)
	at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:47)
	at org.gradle.internal.execution.impl.DefaultWorkExecutor.execute(DefaultWorkExecutor.java:33)
	at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:140)
	at org.gradle.api.internal.tasks.execution.ValidatingTaskExecuter.execute(ValidatingTaskExecuter.java:62)
	at org.gradle.api.internal.tasks.execution.SkipEmptySourceFilesTaskExecuter.execute(SkipEmptySourceFilesTaskExecuter.java:108)
	at org.gradle.api.internal.tasks.execution.ResolveBeforeExecutionOutputsTaskExecuter.execute(ResolveBeforeExecutionOutputsTaskExecuter.java:67)
	at org.gradle.api.internal.tasks.execution.ResolveAfterPreviousExecutionStateTaskExecuter.execute(ResolveAfterPreviousExecutionStateTaskExecuter.java:46)
	at org.gradle.api.internal.tasks.execution.CleanupStaleOutputsExecuter.execute(CleanupStaleOutputsExecuter.java:94)
	at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
	at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:95)
	at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
	at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:56)
	at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:416)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:406)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
	at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:102)
	at org.gradle.internal.operations.DelegatingBuildOperationExecutor.call(DelegatingBuildOperationExecutor.java:36)
	at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
	at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:43)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:355)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:343)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:336)
	at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:322)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker$1.execute(DefaultPlanExecutor.java:134)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker$1.execute(DefaultPlanExecutor.java:129)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:202)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.executeNextNode(DefaultPlanExecutor.java:193)
	at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:129)
	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
	at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:48)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:56)
	at java.lang.Thread.run(Thread.java:745)
Caused by: java.lang.RuntimeException
	at org.objectweb.asm.ClassVisitor.visitModule(ClassVisitor.java:148)
	at org.objectweb.asm.ClassReader.readModule(ClassReader.java:731)
	at org.objectweb.asm.ClassReader.accept(ClassReader.java:632)
	at org.objectweb.asm.ClassReader.accept(ClassReader.java:500)
	at com.android.builder.desugaring.DesugaringClassAnalyzer.analyze(DesugaringClassAnalyzer.java:144)
	at com.android.builder.desugaring.DesugaringClassAnalyzer.analyzeJar(DesugaringClassAnalyzer.java:92)
	at com.android.builder.desugaring.DesugaringClassAnalyzer.analyze(DesugaringClassAnalyzer.java:63)
	at com.android.build.gradle.internal.transforms.DesugarIncrementalTransformHelper.lambda$getInitalGraphData$4(DesugarIncrementalTransformHelper.java:150)
	at java.util.concurrent.ForkJoinTask$AdaptedCallable.exec(ForkJoinTask.java:1424)
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)

Caused by: java.lang.RuntimeException

error processing C:\Users\One'Day\.gradle\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib\1.4.10\ea29e063d2bbe695be13e9d044dcfb0c7add398e\kotlin-stdlib-1.4.10.jar
java.lang.RuntimeException
	at org.objectweb.asm.ClassVisitor.visitModule(ClassVisitor.java:148)
	at org.objectweb.asm.ClassReader.readModule(ClassReader.java:731)
	at org.objectweb.asm.ClassReader.accept(ClassReader.java:632)
	at org.objectweb.asm.ClassReader.accept(ClassReader.java:500)
	at com.android.builder.desugaring.DesugaringClassAnalyzer.analyze(DesugaringClassAnalyzer.java:144)
	at com.android.builder.desugaring.DesugaringClassAnalyzer.analyzeJar(DesugaringClassAnalyzer.java:92)
	at com.android.builder.desugaring.DesugaringClassAnalyzer.analyze(DesugaringClassAnalyzer.java:63)
	at com.android.build.gradle.internal.transforms.DesugarIncrementalTransformHelper.lambda$getInitalGraphData$4(DesugarIncrementalTransformHelper.java:150)
	at java.util.concurrent.ForkJoinTask$AdaptedCallable.exec(ForkJoinTask.java:1424)
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)


```

### 解决方案

> 参考链接：
>[More than one file was found with OS independent path](https://stackoverflow.com/questions/52518378/more-than-one-file-was-found-with-os-independent-path-meta-inf-proguard-android)

在gradle.properties中添加如下两行配置即可：

```groovy
  android.enableD8.desugaring = true
  android.enableIncrementalDesugaring = false
```

## More than one file was found with OS independent

> Studio版本：4.0.2
>
> Gradle版本：4.6

### 问题描述

​	当在项目中添加协程相关的依赖时，碰到如下错误：

```groovy
More than one file was found with OS independent path 'META-INF/proguard/coroutines.pro'
```

​	这应该是同一类的错误，发现了多个相关的文件。

### 解决方案

​	这应该是同一类的错误，可以在module中的gradle文件中添加如下配置即可。

```groovy
android {
     packagingOptions {
         exclude 'META-INF/proguard/coroutines.pro'
     }
 }
```

## gradlew :app:dependencies报错

> Studio版本：4.0.2
>
> Gradle版本：4.6
>

参考链接：

https://stackoverflow.com/questions/49074620/starting-a-gradle-daemon-1-busy-and-6-stopped-daemons-could-not-be-reused-use

### 问题描述

当我想查看项目中的依赖库时，会使用到这个命令。但是今天出现了如下报错信息：

```groovy
D:\dev\corp\QuestionType3.0>gradlew :app:dependencies
Starting a Gradle Daemon, 1 incompatible Daemon could not be reused, use --status for details

FAILURE: Build failed with an exception.

* What went wrong:
Unable to start the daemon process.
This problem might be caused by incorrect configuration of the daemon.
For example, an unrecognized jvm option is used.
Please refer to the user guide chapter on the daemon at https://docs.gradle.org/4.6/userguide/gradle_daemon.html
Please read the following process output to find out more:
-----------------------
ERROR: transport error 202: bind failed: Address already in use
ERROR: JDWP Transport dt_socket failed to initialize, TRANSPORT_INIT(510)
JDWP exit error AGENT_ERROR_TRANSPORT_INIT(197): No transports initialized [debugInit.c:750]


* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org
```

​	看字面意思，应该是已经有一个守护进程在运行了。

### 解决方案

使用如下命令终止守护进程即可。

```groovy
gradlew --stop
```

## Connection timed out: connect

> Studio版本：4.0.2
>
> Gradle版本：4.6

### 问题描述

​	在导入“学海输入法”项目时，一直报如下错误：

```groovy
Connection timed out: connect. If you are behind an HTTP proxy, please configure the proxy settings either in IDE or Gradle.
```

​	看字面意思，应该是代理或者网络的问题导致的。后经过各种尝试还是无法解决该问题。

### 解决方案

​	在项目根目录下的build.gradle文件中添加google()仓库，重新编译即可。

```java
buildscript {
    repositories {
        // 添加google仓库
        google()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.1'
    }
}
allprojects {
    repositories {
        // 添加google仓库
        google()
    }
}
```

