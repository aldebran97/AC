# AC自动机

AC = TrieTree + KMP

（1）com.aldebran.AC 不支持包含词的AC自动机

（2）com.aldebran.ACPlus 支持包含词的AC自动机

# 用途

（1）海量词库检索

用法

```java
AC trieTree=new AC();
trieTree.addWords(Arrays.asList("word1","word2","word3"));
trieTree.update();
System.out.println(trieTree.indexOf("001word1002word0003word2"));
```

（2）文本检索，相似检索

用法

