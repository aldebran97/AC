# AC自动机

AC = TrieTree + KMP

（1）ac.com.aldebran.AC 不支持包含词的AC自动机

（2）ac.com.aldebran.ACPlus 支持包含词的AC自动机

# 用途

### （1）海量词库检索

用法

```java
AC trieTree=new AC();
trieTree.addWords(Arrays.asList("word1","word2","word3"));
trieTree.update();
System.out.println(trieTree.indexOf("001word1002word0003word2"));
```

### （2）文本检索，相似检索

插入查询用法

```java
// 构造
TextSimilaritySearch textSimilaritySearch=new TextSimilaritySearch(
        1.5, // criticalContentHitCount，临界情况，期望的内容命中Gram个数
        1.5,  // criticalTitleHitCount，临界情况，期望的标题命中Gram个数
        0.5, // criticalScore，临界情况score值
        1000, // contentGrowRate，内容命中Gram单项评分增长率，可用于抵抗小idf
        200, // titleGrowthRate，标题命中Gram单项评分增长率，可用于抵抗小idf
        0.5, // decayRate，小idf衰减率
        2, // n-gram中的n，n越大越严格
        "test" // 库名称
);
// 插入文本
textSimilaritySearch.addText("伊凡一世  莫斯科大公（约1325年－1340年3月31日在位）","伊凡一世","1",0.5);
textSimilaritySearch.addText("水调歌头 水调歌头，词牌名。亦称《花犯念奴》、《元会曲》。","水调歌头","2",0.5);

// 相似查询
System.out.println(textSimilaritySearch.similaritySearch("伊凡二世 水调歌头",10));
```

库的保存和加载

```java
// load
TextSimilaritySearch textSimilaritySearch=TextSimilaritySearch.load(inFile);
// save
TextSimilaritySearch.save(outFile);
```

### （3）空间效率统计

90386个短文本(平均404.56231053481736 chars)，磁盘空间占用1.2GB

### （4）时间效率统计

#### AC词库匹配时间

```text
词库词数 2493896
输入文本长度 31chars
词库匹配所需时间 0.05ms
匹配时间主要与输入长度有关，受库内文本数量影响小。
```

#### 相似搜索时间

```text
90386个短文本(平均404.56231053481736 chars)
输入文本长度 47chars
相似搜索所需时间 2.265ms
匹配时间主要与输入长度有关，受库内文本数量影响小。

```
```text
269319个短文本(平均363.58687652932025 chars)
输入文本长度 47chars
相似搜索所需时间 8.399999999999999ms
匹配时间主要与输入长度有关，受库内文本数量影响小。

```

#### 入库时间

```text
90386个文本(平均404.56231053481736 chars)
插入文章所用时间：62.243s
刷新索引所用时间：12.116s
总时间：74.359s
```
```text
269319个文本(平均363.58687652932025 chars)
插入文章所用时间：185.617s
刷新索引所用时间：26.562s
总时间：212.179s
```

#### 加载时间（程序启动时候，只需要一次）

```text
90386个文本
加载所需时间44.493s
```
```text
269319个文本
加载所需时间126.647s
```
（5）注意事项

当文章非常多的时候，要指定很大的Xss和Xms，例如：
-Xss1024m -Xms28g

（6）TODO

数字目前不支持精确匹配

兄弟项目：https://github.com/aldebran97/springboot_text_toolkit/tree/master