# AC自动机

AC = TrieTree + KMP

（1）ac.com.aldebran.AC 不支持包含词的AC自动机

（2）ac.com.aldebran.ACPlus 支持包含词的AC自动机

# 安装
powershell
```shell
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

# 下载源码
git clone https://github.com/aldebran97/AC.git

cd AC

# 编译
javac -d .\out2\  .\src\com\aldebran\text\*.java  .\src\com\aldebran\text\ac\*.java .\src\com\aldebran\text\replacePolicy\*.java .\src\com\aldebran\text\similarity\*.java .\src\com\aldebran\text\util\*.java

cd out2

# 打包
jar cfe AC.jar com.aldebran.text.TempTest .\com\aldebran\text\*.class .\com\aldebran\text\ac\*.class  .\com\aldebran\text\replacePolicy\*.class  .\com\aldebran\text\similarity\*.class .\com\aldebran\text\util\*.class

# 打的是可运行包，可以尝试运行一下，但其实只作为其他项目依赖就行
java -jar .\AC.jar
```

linux / macos

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

#### 插入查询用法

```java
// 构造
TextSimilaritySearch textSimilaritySearch = new TextSimilaritySearch(
        3, // criticalContentHitCount，临界情况，期望的内容命中Gram个数
        3, // criticalTitleHitCount，临界情况，期望的标题命中Gram个数
        0.5, // criticalScore，临界情况score值
        1, // contentK，内容权重
        2, // titleK，标题权重
        2, // hitGramsCountLogA，此值越小，命中累计计数对结果的影响越大
        200, // gramsCountLogA，低长度文本有略微的领先优势，此值越小，低长度文本优势越明显
        10, // idfGrowthK, idf区分度
        2, // n-gram中的n，n越大越严格
        "test");

// 加载替换库，里面定义了停止词、近义词和类别增强，可以替换为自己的库
textSimilaritySearch.textPreprocess.loadReplaceMapFromFile("./replace.txt");

String text1 = "《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。";

String title1 = "《梦游天姥吟留别》";

String text2 = "《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。";

String title2 = "《水调歌头·文字觑天巧》";

String text3 = "伊凡一世富于谋略，为达到自己的目的不择手段，狡猾而残忍。他利用莫斯科优越的地理优势，利用以往积累的财力贿赂金帐汗国统治阶层，又站在对清算封建分裂势力有利的教会一方，抑制以特维尔王公为首的莫斯科邻近各公国。";

String title3 = "伊凡一世";

// 插入若干文本
textSimilaritySearch.addText(text1, title1, "1", 1);

textSimilaritySearch.addText(text2, title2, "2", 1);

textSimilaritySearch.addText(text3, title3, "3", 1);

// 相似查询
System.out.println(textSimilaritySearch.similaritySearch(
"《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
"辛弃疾的《水调歌头》在此之后。", 10));
```

#### 库的保存和加载

保存
```java
File outFile = new File("./test-lib"); // 替换为自己的路径
TextSimilaritySearch.save(textSimilaritySearch, outFile);
```

加载
```java
// load
File inFile = new File("./test-lib"); // 替换为自己的路径
TextSimilaritySearch textSimilaritySearch=TextSimilaritySearch.load(inFile);
```


### （3）时间效率分析

#### AC词库匹配时间
```text
词库词数 2493896
输入文本长度 31chars
词库匹配所需时间 0.05ms
匹配时间主要与输入长度有关，受库内文本数量影响小。
```

#### 相似检索相关时间

入库文章数量: 88480(平均398.2071993670886 chars)
```text
相似搜索时间: 15.334ms
插入文章所用时间：75.226s
刷新索引所用时间：8.807s
持久化所用时间：47.818s
加载所有时间：
```

入库文章数量: 800003(平均233.0957046411076 chars)
```text
相似搜索时间: 127.778ms
插入文章所用时间：1446.497s
刷新索引所用时间：90.8s
持久化所用时间：477.163s
加载所有时间：
```

### （4）空间效率统计

入库文章数量: 88480(平均398.2071993670886 chars)，磁盘空间占用约1.2GB，内存占用约
入库文章数量: 800003(平均233.0957046411076 chars)，磁盘空间占用约5.1GB，内存占用约

（5）注意事项

当文章非常多的时候，要指定很大的Xss和Xms，例如：
-Xss1024m -Xms30g

（6）TODO

数字目前不支持精确匹配

# 兄弟项目

https://github.com/aldebran97/springboot_text_toolkit/tree/master

提供了API访问方式

