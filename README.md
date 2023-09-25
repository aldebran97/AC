# AC自动机

AC = TrieTree + KMP

（1）ac.com.aldebran.AC 不支持包含词的AC自动机

（2）ac.com.aldebran.ACPlus 支持包含词的AC自动机

# 安装

windows powershell

```shell
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

# 下载源码
git clone https://github.com/aldebran97/AC.git

cd AC

mkdir out2

# 编译
javac -d .\out2\  .\src\com\aldebran\text\*.java  .\src\com\aldebran\text\ac\*.java .\src\com\aldebran\text\replacePolicy\*.java .\src\com\aldebran\text\similarity\*.java .\src\com\aldebran\text\util\*.java

cd out2

# 打包
jar cfe AC.jar com.aldebran.text.TempTest .\com\aldebran\text\*.class .\com\aldebran\text\ac\*.class  .\com\aldebran\text\replacePolicy\*.class  .\com\aldebran\text\similarity\*.class .\com\aldebran\text\util\*.class

cp ../replace.txt .

# 打的是可运行包，可以尝试运行一下，但其实只作为其他项目依赖就行
java -jar .\AC.jar
```

linux / macos

```shell
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# 下载源码
git clone https://github.com/aldebran97/AC.git

cd AC

mkdir out2

# 编译
javac -d ./out2/  ./src/com/aldebran/text/*.java  ./src/com/aldebran/text/ac/*.java ./src/com/aldebran/text/replacePolicy/*.java ./src/com/aldebran/text/similarity/*.java ./src/com/aldebran/text/util/*.java

cd out2

# 打包
jar cfe AC.jar com.aldebran.text.TempTest ./com/aldebran/text/*.class ./com/aldebran/text/ac/*.class  ./com/aldebran/text/replacePolicy/*.class  ./com/aldebran/text/similarity/*.class ./com/aldebran/text/util/*.class

cp ../replace.txt .

# 打的是可运行包，可以尝试运行一下，但其实只作为其他项目依赖就行
java -jar ./AC.jar
```

# 用途

首先在项目里引入jar包，并且import class
```java
import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.similarity.TextSimilaritySearch;
```

### （1）海量词库检索

用法

代码
```java
AC trieTree=new AC();
trieTree.addWords(Arrays.asList("word1","word2","word3"));
trieTree.update();
System.out.println(trieTree.indexOf("001word1002word0003word2"));
```

输出
```text
[MatchResult{word='word1', index=3}, MatchResult{word='word2', index=19}]
```

### （2）文本检索，相似检索

#### 插入查询用法

代码
```java
// 构造
TextSimilaritySearch textSimilaritySearch=new TextSimilaritySearch(
        3, // criticalContentHitCount，临界情况，期望的内容命中Gram个数
        3, // criticalTitleHitCount，临界情况，期望的标题命中Gram个数
        0.5, // criticalScore，临界情况score值
        1, // contentK，内容权重
        2, // titleK，标题权重
        2, // hitGramsCountLogA，此值越小，命中累计计数对结果的影响越大
        200, // gramsCountLogA，低长度文本有略微的领先优势，此值越小，低长度文本优势越明显
        10, // idfGrowthK, gram得分区分度，此值越大，得分梯度越大
        2, // n-gram中的n，n越大越严格
        "test" // 相似库名称
        );

// 加载替换库，里面定义了停止词、近义词和类别增强，可以替换为自己的库
textSimilaritySearch.textPreprocess.loadReplaceMapFromFile("./replace.txt");

String text1="《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。";

String title1="《梦游天姥吟留别》";

String text2="《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。";

String title2="《水调歌头·文字觑天巧》";

String text3="伊凡一世富于谋略，为达到自己的目的不择手段，狡猾而残忍。他利用莫斯科优越的地理优势，利用以往积累的财力贿赂金帐汗国统治阶层，又站在对清算封建分裂势力有利的教会一方，抑制以特维尔王公为首的莫斯科邻近各公国。";

String title3="伊凡一世";

// 插入若干文本
textSimilaritySearch.addText(text1,title1,"1",1);

textSimilaritySearch.addText(text2,title2,"2",1);

textSimilaritySearch.addText(text3,title3,"3",1);

// 首次插入完毕后，一定要updae，至关重要！
// 后续相似库发生更新后，累计插入一些数据后再update！此方法不适合频繁调用！如果不update，仅仅查不到新数据，旧数据仍旧能查到。
// 功能为：更新适配指针、统计指标和score计算器
textSimilaritySearch.update();

// 相似查询
System.out.println(textSimilaritySearch.similaritySearch(
"《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。"+
"辛弃疾的《水调歌头》在此之后。",10));
```

输出
```text
[SimilaritySearchResult{id='1', title='《梦游天姥吟留别》', text='《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。', score=0.6464106850592721}, SimilaritySearchResult{id='2', title='《水调歌头·文字觑天巧》', text='《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。', score=0.5641475960385378}]
```

#### 重定义参数
在库建立好后，参数仍旧可调整
```java
textSimilaritySearch.changeArgs(
        3, // criticalContentHitCount，临界情况，期望的内容命中Gram个数
        3, // criticalTitleHitCount，临界情况，期望的标题命中Gram个数
        0.5, // criticalScore，临界情况score值
        1, // contentK，内容权重
        2, // titleK，标题权重
        2, // hitGramsCountLogA，此值越小，命中累计计数对结果的影响越大
        200, // gramsCountLogA，低长度文本有略微的领先优势，此值越小，低长度文本优势越明显
        10 // idfGrowthK, gram得分区分度，此值越大，得分梯度越大
        );
```

#### 库的保存和加载

保存

```java
File outFile = new File("./test-lib"); // 替换为自己的路径
TextSimilaritySearch.save(textSimilaritySearch,outFile);
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

入库文章数量: 88480(每个文章平均398.2071993670886 chars)

```text
搜索操作：
相似搜索时间: 14.352ms

入库操作：
插入文章时间：75.226s
刷新索引时间：8.807s

保存库操作：
持久化时间：47.818s

加载操作：
加载时间：20.257s （程序启动只需一次）
```

入库文章数量: 800003(每个文章平均233.0957046411076 chars)

```text
搜索操作：
相似搜索: 116.738ms

入库操作：
插入文章时间：1446.497s
刷新索引时间：90.8s

保存库操作：
持久化时间：477.163s

加载操作：
加载时间：181.485s（程序启动只需一次）
```

### （4）空间效率统计

```text
入库文章数量: 88480(平均398.2071993670886 chars)，磁盘空间占用约1.2GB，内存占用约9G。
入库文章数量: 800003(平均233.0957046411076 chars)，磁盘空间占用约5.1GB，内存占用约24G。
```

### （5）替换库说明（见replace.txt，可自定义追加）

格式说明
```text
# 停止词，标点符号
·
`
。
.

# 停止词，中文字词
的
了

# 相似词语
高兴 快乐

# 类别追加
SUV SUV运动型多用途汽车
```

原则
```text
（1）停止词是必备的，减少干扰。一行只需一个词（字）。

（2）相似词语替换可进一步增加召回率（查全率），不过对于知识检索领域，没有相似词库也能取得较好的效果。

（3）类别追加，增加搜索的通用性，比如追加全称，或者追加一个类型。例如“老虎 猫科动物”，这会显著增加召回率（查全率），但是精确率可能会有所降低（误杀率提高）。
（score可能会变得比较高，但排名不变）
```

### （6）注意事项

```text
当文章非常多的时候，要指定很大的Xss和Xms，例如：
-Xss1024m -Xms30g
```

### （7）TODO

```text
1. 数字目前不支持精确匹配，有需求应追加参数控制。

2. AvgIdfGrowthCalculator用于计算gram得分，并且一定程度上扩大gram得分差距，依靠idfGrowthK参数控制。
目前是线性均匀增加，可继承AvgIdfGrowthCalculator实现S型曲线（AvgIdfGrowthCalculatorSigmoid），扩大期望值附近的得分差距，减少高分之间（低分之间）的差距。
或者实现x^3类型的曲线（AvgIdfGrowthCalculatorCube），减少期望值附近的得分差距，增加高分之间（低分之间）的差距。
```

### （8）效率测试平台说明
```text
处理器：intel i9-12900HX 16核24线程
内存：32G物理内存 4800MHz
操作系统：Windows11
Java版本：OpenJDK20（你可以用JDK8，但内存占用会有所增加）
```

# 兄弟项目

https://github.com/aldebran97/springboot_text_toolkit/tree/master

提供了API访问方式

