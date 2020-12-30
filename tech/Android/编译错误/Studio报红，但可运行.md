# Studio报红，但可运行

## 问题描述

```java
Cannot access 'androidx.lifecycle.HasDefaultViewModelProviderFactory' which is a supertype of 'com.xh.demo_animation.AnimActivity'. Check your module classpath for missing or conflicting dependencies
```

​	虽然该Activity报红，但是可以正常运行。特别影响工作效率！

​	使用过各种 “clean Project”、“Invalidate and Restart”还是不行...

## 解决方案

> Studio版本：4.0.2
>
> # [Cannot access 'androidx.lifecycle.HasDefaultViewModelProviderFactory'](https://stackoverflow.com/questions/62285843/cannot-access-androidx-lifecycle-hasdefaultviewmodelproviderfactory-which-is-a)

应该是ViewModel的版本和其中一个依赖库内部的版本不一致导致的。我的解决方案为：统一使用同一个ViewModel版本即可。

```groovy
dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.3.2'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'

    // ViewModel
    implementation "androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version"
}
```

