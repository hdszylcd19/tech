# Android Studio常见问题

## 错误代码没有提示

Android Studio版本：v4.0.2

进过验证发现，是该版本和其中一个插件Java Object Layout (JOL)冲突导致；禁用掉该插件就可以解决这个问题；每个人可能冲突的插件不太一样，如碰到这个问题，需要一个一个禁用插件来尝试。该问题属于Studio的BUG!!!