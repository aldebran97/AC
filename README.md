# ngram + AC自动机为基础实现的词库检索、相似检索程序

AC = TrieTree + KMP

（1）com.aldebran.AC 不支持包含词的AC自动机

（2）com.aldebran.ACPlus 支持包含词的AC自动机

## 一、安装

### 1. 安装JDK，并配置环境变量

#### （1）下载符合自己操作系统的JDK版本，并解压
https://jdk.java.net/archive/ 

#### （2）配置环境变量

##### windows操作系统

```text
（1）windows需要到高级系统设置->高级->环境变量里配置
配置JAVA_HOME，例如：D:\user_dir\program_files\jdk-20.0.2
追加PATH，例如：%JAVA_HOME%\bin
配置CLASS_PATH，例如：%JAVA_HOME%\lib;.
配置JAVA_TOOL_OPTIONS，值为：-Dfile.encoding=UTF-8

（2）验证JDK是否安装成功
首先重启powershell，然后输入指令：
java -version
javac -version
如果不报错，说明安装成功
````

##### macos/debian/ubuntu
```text
（1）配置环境变量，在~/.bashrc结尾追加：
export JAVA_HOME='your_java_home'
export PATH=$PATH:$JAVA_HOME/bin
export CLASS_PATH=$JAVA_HOME/lib:.
export 配置JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8'
（2）运行指令
source ~/.bashrc
（3）验证JDK是否安装成功
java -version
javac -version
如果不报错，说明安装成功
````

### 2. 从源码安装此程序

```shell
# 以下指令均支持windows powershell / linux shell / macos shell
# 下载源码
git clone https://github.com/aldebran97/AC.git

cd AC

# 创建target目录
mkdir out2

# 编译
javac -d ./out2/  ./src/com/aldebran/text/*.java  ./src/com/aldebran/text/ac/*.java ./src/com/aldebran/text/replacePolicy/*.java ./src/com/aldebran/text/similarity/*.java ./src/com/aldebran/text/util/*.java

# 进入目录
cd out2

# 打包
jar cfe AC.jar com.aldebran.text.TempTest ./com/aldebran/text/*.class ./com/aldebran/text/ac/*.class  ./com/aldebran/text/replacePolicy/*.class  ./com/aldebran/text/similarity/*.class ./com/aldebran/text/util/*.class

cp ../replace.txt .

# 打的是可运行包，可以尝试运行一下，但其实只作为其他项目依赖就行。如果有输出，说明安装成功
java -jar ./AC.jar
```

### 3. 如果还需要python调用，可利用pyjnius库，不用python可跳过此步

##### 安装pyjnius库

```text
# 如果JDK版本发生变更，需要重新安装pyjnius
# pip uninstall pyjnius
pip install pyjnius==1.5.0
```

## 二、使用方法

以下都有JAVA和python代码

java代码示例见[TempTest.java](src%2Fcom%2Faldebran%2Ftext%2FTempTest.java)

python代码示例见[python_call_example.py](python_call_example.py)

### 1. 导入依赖 + 准备工作

Java：首先在项目里引入jar包，并且import class
```java
import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.similarity.TextSimilaritySearch;
```

python：CLASSPATH中指定jar路径，指定JAVA_HOME
```shell
import os

os.environ['CLASSPATH'] = r"C:\Users\aldebran\user_dir\code\AC\out2" # 替换为你自己的路径
os.environ['JAVA_HOME'] = r'D:\user_dir\program_files\jdk-20.0.2' # 替换为你自己的路径
from jnius import autoclass

AC_CLASS = autoclass('com.aldebran.text.ac.AC')
AC_PLUS_CLASS = autoclass('com.aldebran.text.ac.ACPlus')
TextSimilaritySearchClass = autoclass('com.aldebran.text.similarity.TextSimilaritySearch')
ARRAY_LIST_CLASS = autoclass('java.util.ArrayList')
FILE_CLASS = autoclass('java.io.File')


def j_list(l: list):
    j_list_obj = ARRAY_LIST_CLASS()
    for e in l: j_list_obj.add(e)
    return j_list_obj
```


### 2. 海量词库检索



java代码
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

python代码
```shell
ac = AC_CLASS()
ac.addWords(j_list(['word1', 'word2', 'word3']))
ac.update()
for mr in ac.indexOf("001word1002word0003word2"):
    print('index:', mr.index, 'word:', mr.word)
```

输出
```text
index: 3 word: word1
index: 19 word: word2
```

### 2. 文本检索，相似检索

##### 插入查询用法

java
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
// 参数分别是：文本、标题、文本ID、文本附加权重（至少为1）
textSimilaritySearch.addText(text1,title1,"1",1);

textSimilaritySearch.addText(text2,title2,"2",1);

textSimilaritySearch.addText(text3,title3,"3",1);

// 首次插入完毕后，一定要update，至关重要！
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

python代码
```shell
lib = TextSimilaritySearchClass(
    3,  # criticalContentHitCount，临界情况，期望的内容命中Gram个数
    3,  # criticalTitleHitCount，临界情况，期望的标题命中Gram个数
    0.5,  # criticalScore，临界情况score值
    1,  # contentK，内容权重
    2,  # titleK，标题权重
    2,  # hitGramsCountLogA，此值越小，命中累计计数对结果的影响越大
    200,  # gramsCountLogA，低长度文本有略微的领先优势，此值越小，低长度文本优势越明显
    10,  # idfGrowthK, gram得分区分度，此值越大，得分梯度越大
    2,  # n-gram中的n，n越大越严格
    'test'  # 相似库名称
)

lib.textPreprocess.loadReplaceMapFromFile("./replace.txt")  # 替换为自己的

text1 = "《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。"

title1 = "《梦游天姥吟留别》"

text2 = "《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。"

title2 = "《水调歌头·文字觑天巧》"

text3 = "伊凡一世富于谋略，为达到自己的目的不择手段，狡猾而残忍。他利用莫斯科优越的地理优势，利用以往积累的财力贿赂金帐汗国统治阶层，又站在对清算封建分裂势力有利的教会一方，抑制以特维尔王公为首的莫斯科邻近各公国。"

title3 = "伊凡一世"

# 导入若干文章
lib.addText(text1,  # 文本
            title1,  # 标题
            "1"  # id，不同文章必须不同
            , 1  # 文章附加权重，至少为1，如果不额外指定，为1就好
            )

lib.addText(text2, title2, "2", 1)

lib.addText(text3, title3, "3", 1)

# 首次插入完毕后，一定要update，至关重要！
# 后续相似库发生更新后，累计插入一些数据后再update！此方法不适合频繁调用！如果不update，仅仅查不到新数据，旧数据仍旧能查到。
# 功能为：更新适配指针、统计指标和score计算器
lib.update()

for result in lib.similaritySearch(
        """《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。
        辛弃疾的《水调歌头》在此之后。
        """,  # 查询内容
        10  # top_k
):
    print(f'id :{result.id}, score: {result.score}, title: {result.title}, text: {result.text}')
```

输出
```text
id :1, score: 0.6464106850592721, title: 《梦游天姥吟留别》, text: 《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。
id :2, score: 0.5641475960385378, title: 《水调歌头·文字觑天巧》, text: 《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。
```

### 3. 重定义参数
在库建立好后，参数仍旧可调整

java代码
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

python代码
```shell
lib.changeArgs(
    3,  # criticalContentHitCount，临界情况，期望的内容命中Gram个数
    3,  # criticalTitleHitCount，临界情况，期望的标题命中Gram个数
    0.5,  # criticalScore，临界情况score值
    1,  # contentK，内容权重
    2,  # titleK，标题权重
    2,  # hitGramsCountLogA，此值越小，命中累计计数对结果的影响越大
    200,  # gramsCountLogA，低长度文本有略微的领先优势，此值越小，低长度文本优势越明显
    10  # idfGrowthK, gram得分区分度，此值越大，得分梯度越大
)
```

### 4. 库的保存和加载

#### 保存

java代码
```java
File outFile = new File("./test-lib"); // 替换为自己的路径
TextSimilaritySearch.save(textSimilaritySearch,outFile);
```
python代码
```shell
outFile = FILE_CLASS("./test-lib");  # 替换为自己的路径
TextSimilaritySearchClass.save(lib, outFile)
```

#### 加载

java代码
```java
// load
File inFile = new File("./test-lib"); // 替换为自己的路径
TextSimilaritySearch textSimilaritySearch=TextSimilaritySearch.load(inFile);
```

python代码
```shell
inFile = FILE_CLASS("./test-lib")  # 替换为自己的路径
lib = TextSimilaritySearchClass.load(inFile)
```

## 三、时间效率分析

### 1. AC词库匹配时间

```text
词库词数 2493896
输入文本长度 31chars
词库匹配所需时间 0.05ms
匹配时间主要与输入长度有关，受库内文本数量影响小。
```

### 2. 相似检索相关时间

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

## 四、空间效率统计

```text
入库文章数量: 88480(平均398.2071993670886 chars)，磁盘空间占用约1.2GB，内存占用约9G。
入库文章数量: 800003(平均233.0957046411076 chars)，磁盘空间占用约5.1GB，内存占用约24G。
```

## 五、替换库说明（见replace.txt，可自定义追加）

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

## 六、注意事项

```text
当文章非常多的时候，要指定很大的Xss和Xms，例如：
-Xss1024m -Xms30g
```

## 七、TODO

```text
1. 数字目前不支持精确匹配，有需求应追加参数控制。

2. AvgIdfGrowthCalculator用于计算gram得分，并且一定程度上扩大gram得分差距，依靠idfGrowthK参数控制。
目前是线性均匀增加，可继承AvgIdfGrowthCalculator实现S型曲线（AvgIdfGrowthCalculatorSigmoid），扩大期望值附近的得分差距，减少高分之间（低分之间）的差距。
或者实现x^3类型的曲线（AvgIdfGrowthCalculatorCube），减少期望值附近的得分差距，增加高分之间（低分之间）的差距。
```

## 八、效率测试平台说明
```text
处理器：intel i9-12900HX 16核24线程
内存：32G物理内存 4800MHz
操作系统：Windows11
Java版本：OpenJDK20（你可以用JDK8，但内存占用会有所增加）
```

# 兄弟项目

https://github.com/aldebran97/springboot_text_toolkit/tree/master

提供了API访问方式

