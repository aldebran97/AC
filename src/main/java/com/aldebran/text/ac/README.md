### ac包实现了AC自动机

### 【1】Java类

（1）[AC.java](AC.java) 实现了不支持包含词的AC自动机

（2）[ACPlus.java](ACPlus.java)实现了支持包含词的AC自动机

### 【2】功能：输入文本对海量词库匹配的一种高效的方法。

设输入长度为n，库中词数为m，则匹配的时间复杂度为O(n)，与库中词数无关。

### 【3】代码示例

基本使用
```java
// 如果您的词库中没有包含词，使用AC类实例化，而不是ACPlus
AC acPlus = new ACPlus();
acPlus.addWords(Arrays.asList("ABCEAFBABCD", "EA", "FB", "F", "B"));
acPlus.update();
System.out.println(acPlus.indexOf("ABCEAFBABCQEA"));
```


输出
```text
[MatchResult{word='B', index=1}, MatchResult{word='EA', index=3}, MatchResult{word='F', index=5}, MatchResult{word='FB', index=5}, MatchResult{word='B', index=6}, MatchResult{word='B', index=8}, MatchResult{word='EA', index=11}]
```

保存
```java
// 如果您使用AC类实例化，则使用AC.save
ACPlus.save(
        acPlus,  // 保存的AC自动机
        new File("./test-ac"),  // 保存文件
        false // 是否启用多线程
        );
```


加载
```java
// 如果您使用AC类实例化，则使用AC.load
AC ac2 = ACPlus.load(
        new File("./test-ac"),  // 保存文件
        false // 是否启用多线程
        );
```

### 【4】效率统计  TODO

#### （1）输入长度字符数50，统计检索时间和库内词数的关系



#### （2）统计持久化时间时间和库内词数的关系




#### （3）统计加载时间时间和库内词数的关系