# ngram + AC自动机为基础实现的词库检索、文本相似检索

# java代码说明

### 1. 导入依赖 + 准备工作

首先在项目里引入jar包，并且import class

```java
import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.similarity.TextSimilaritySearch;
```

### 2. 海量词库检索

```java
AC trieTree=new AC(); // 如果您的词库中有包含关系，请用ACPlus
// 向词库中加入若干词
trieTree.addWords(Arrays.asList("word1","word2","word3"));
// 更新失配指针，首次必须调用。之后词库更新不宜频繁调用，应在恰当的时机调用（比如累计追加数量到达阈值，或者间隔时间到达阈值）。
trieTree.update();
// 词库匹配
System.out.println(trieTree.indexOf("001word1002word0003word2"));
File acLibFile = new File("./test-ac");
// 保存词库
AC.save(ac, acLibFile);
// 导入词库
AC ac2 = AC.load(acLibFile);  // 如果您用了ACPlus，请用ACPlus.load
```

输出

```text
[MatchResult{word='word1', index=3}, MatchResult{word='word2', index=19}]
```

### 2. 文本检索，相似检索

##### 插入查询用法

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
// 参数分别是：文本、标题、文本ID、文本附加权重（至少为1）
textSimilaritySearch.addText(text1,title1,"1",1);

textSimilaritySearch.addText(text2,title2,"2",1);

textSimilaritySearch.addText(text3,title3,"3",1);

// 首次插入完毕后，一定要update，至关重要！
// 后续相似库发生更新后，累计插入一些数据后再update！此方法不适合频繁调用！如果不update，仅仅查不到新数据，旧数据仍旧能查到。
// 功能为：更新适配指针、统计指标和score计算器
textSimilaritySearch.update();

// 如果你想支持多线程检索，设置lib.allowMultiThreadsSearch为true。
// 只有命中数量大于lib.searchDocsUnit才会启用多线程。此值的取值范围最好在[10000-30000]。
// lib.allowMultiThreadsSearch = true;

// 相似查询
System.out.println(textSimilaritySearch.similaritySearch(
        "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。"+
        "辛弃疾的《水调歌头》在此之后。",10));
```

输出

```text
title: 《梦游天姥吟留别》, score: 0.6464106850592721, text: 《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。, id: 1
title: 《水调歌头·文字觑天巧》, score: 0.5641475960385378, text: 《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。, id: 2
```


### 3. 重定义参数

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

### 4. 库的保存和加载

#### 保存

```java
File saveFolder=new File("./test-lib"); // 替换为自己的路径，是一个目录。
TextSimilaritySearch.save(textSimilaritySearch,saveFolder,true); // 最后一个参数表示是否允许多线程导出。
```


#### 加载

```java
// load
File saveFolder=new File("./test-lib"); // 替换为自己的路径，是一个目录。
TextSimilaritySearch textSimilaritySearch=TextSimilaritySearch.load(saveFolder, true); // 最后一个参数表示是否允许多线程导入。
```
