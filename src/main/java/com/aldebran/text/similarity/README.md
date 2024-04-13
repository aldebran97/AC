### similarity用于实现相似检索

一、功能：海量文本的相似检索

[TextSimilaritySearch.java](TextSimilaritySearch.java)

（1）实现原理：

基于AC自动机做文章初筛，利用BM25算法做排序，利用多线程和其他算法提升检索速度。

相似语义解决方案：

【1】祖先类别追加

【2】英文词根追加

【3】基于相似词分组 + 中心词选举 + 相似词追加


（2）试用场合： 

【1】适合查询频繁，不频繁更新的场合

```text
由于数据变更会导致失配指针和统计参数变化，需要较长的更新时间。原本设计的使用场合，是大量频繁查询，很少更新或者利用闲时更新。

如果你也想支持频繁更新，就需要采用读写分离：

主库用于查询，小库用于更新和查询。

（1）新增文章时，插入到小库。（小库的数量可以大于1）

（2）查询时候，主库和小库同时查询，整合结果返回。

（3）在合适的时机（比如小库达到了一定数量或者闲时状态），合并为一个主库，持久化、加载并替换正在使用的主库对象。

这属于工程化的内容，我并没有编写相关的功能。
```

【2】查询速度要求很高，并且内存空间大小足够

查询速度的提升是一种空间换时间的方法，因此有较高的内存占用。

【3】对相似语义的可控性要求高

词语预处理类（见[README.md](..%2Fpreprocess%2FREADME.md)）对相似语义的可控性强。

TODO 后续会支持自动生成相似词的算法。

二、代码示例

【1】基本使用
```java
String text1 = "《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。";

String title1 = "《梦游天姥吟留别》";

String text2 = "《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。";

String title2 = "《水调歌头·文字觑天巧》";

String text3 = "伊凡一世富于谋略，为达到自己的目的不择手段，狡猾而残忍。他利用莫斯科优越的地理优势，利用以往积累的财力贿赂金帐汗国统治阶层，又站在对清算封建分裂势力有利的教会一方，抑制以特维尔王公为首的莫斯科邻近各公国。";

String title3 = "伊凡一世";


TextSimilaritySearch textSimilaritySearch = new TextSimilaritySearch(
        3, // 内容词临界命中个数
        3, // 标题词临界命中个数
        0.5, // 临界得分
        1,  // 内容权重
        3, // 标题权重
        2, // bm25算法中的k
        0.1, // bm25算法中的b
        10, // 得分区分度
        new NGramTokenizer(2, null), // 分词器
        "test"); // 库名

textSimilaritySearch.addText(text1, title1, "1", 1); // 内容 标题 自定义ID 附加权重

textSimilaritySearch.addText(text2, title2, "2", 1);

textSimilaritySearch.addText(text3, title3, "3", 1);

textSimilaritySearch.allowMultiThreadsSearch = true; // 是否启用多线程检索

textSimilaritySearch.multipleThreadSearchMinTextsCount = 300000; // 每多少文本启用一个线程检索

System.out.println(textSimilaritySearch.queryById("1"));

textSimilaritySearch.update(); // 检索之前必须更新

for (SimilaritySearchResult result : textSimilaritySearch.similaritySearch(
        "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
        "辛弃疾的《水调歌头》在此之后。", 10)) {
    System.out.printf("title: %s, score: %s, text: %s, id: %s%n", result.title, result.score, result.text, result.id);
}

```

输出
```text
title: 《梦游天姥吟留别》, score: 0.71284161610917, text: 《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。, id: 1
title: 《水调歌头·文字觑天巧》, score: 0.5962599146688901, text: 《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。, id: 2
```

tokenizer详细使用见[README.md](..%2Ftokenizer%2FREADME.md)

【2】保存
```java
File outFile = TextSimilaritySearch.save(textSimilaritySearch, new File("./test-lib"), true);
```

【3】加载
```java
TextSimilaritySearch textSimilaritySearch2 = TextSimilaritySearch.load(inFile, true);

textSimilaritySearch2.tokenizer = new NGramTokenizer(2, null); // 相似检索库和分词器是独立的
```

三、效率统计 TODO

（1）相似检索时间 多线程

（2）入库时间 = （插入时间+更新索引时间+导出时间）多线程

（3）加载时间 多线程

（4）内存空间消耗

（5）持久化 磁盘空间消耗

四、评分指标 TODO

五、效率测试平台 TODO