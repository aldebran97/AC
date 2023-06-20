# AC自动机

AC = TrieTree + KMP

（1）ac.com.aldebran.AC 不支持包含词的AC自动机

（2）ac.com.aldebran.ACPlus 支持包含词的AC自动机

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

插入查询用法

```java
TextSimilaritySearch textSimilaritySearch=new TextSimilaritySearch("test",3,0.5,2,0.3);
textSimilaritySearch.addText("Good morning. Hello");
textSimilaritySearch.addText("Good night. Hello");
textSimilaritySearch.update();
System.out.println(textSimilaritySearch.similaritySearch("Good afternoon. Bye",10));
```

库的保存和加载

```java
// load
TextSimilaritySearch textSimilaritySearch=TextSimilaritySearch.load(inFile);
// save
TextSimilaritySearch.save(outFile);
```

（3）空间效率统计

170909个短文本(200-600 chars)，磁盘空间占用1.04GB

（4）时间效率统计

查询时间
170909个短文本，查询所需时间0.194s

加载时间（程序启动时候，只需要一次）
170909个短文本，加载所需时间46.902s

（5）注意事项

当文章非常多的时候，要指定很大的Xss和Xms
-Xss1024m -Xms5g

（6）TODO

数字还需要支持精确匹配