# n-gram + AC自动机为基础实现的词库检索、文本相似检索


<p align="center">
<a href="https://github.com/aldebran97/AC/blob/main/README_java.md"> Java版本 </a>
•
<a href="https://github.com/aldebran97/AC/blob/main/README.md"> Python版本 </a>  
</p>

### 一、功能概述

#### 1. 海量词库快速检索

#### 2. 海量文本的快速相似检索


### 二、快速开始（Quick Start）

#### （1）下载源码和制品
```shell
# 以下指令均支持windows powershell / linux shell / macos shell
# 下载源码

git clone https://github.com/aldebran97/AC.git

# 本项目提供了一些JDK发行版的打包制品，存储在artifacts目录下！
# 验证制品可运行

cd artifacts

cp ../replace.txt . 

java -jar AC-jdk8.jar

# 如果有输出证明成功

```
以上，如果你没安装过Java，或者你有自己的JDK发行版，可以自行打包，见[README_install.md](README_install.md)。

#### （2）在Python程序中使用

##### 【1】引入依赖
```python
import os

os.environ['CLASSPATH'] = r"/Users/aldebran/custom/code/AC/artifacts/AC.jar" # jar包位置，替换为自己的
os.environ['JAVA_HOME'] = r"/Library/Java/JavaVirtualMachines/jdk1.8.0_291.jdk/Contents/Home" # JAVA_HOME

import jnius_config

# 指定了堆内存1G，栈内存512M。需要根据文章数量和文章长度调整堆内存。（文章数量对内存消耗的影响比文章长度大）
jnius_config.add_options('-Xms1g', '-Xmx1g', '-Xss512m')

from jnius import autoclass

# class object
AC_CLASS = autoclass('com.aldebran.text.ac.AC')
AC_PLUS_CLASS = autoclass('com.aldebran.text.ac.ACPlus')
TextSimilaritySearchClass = autoclass('com.aldebran.text.similarity.TextSimilaritySearch')
ARRAY_LIST_CLASS = autoclass('java.util.ArrayList')
FILE_CLASS = autoclass('java.io.File')


# python list to java list
def j_list(l: list):
    j_list_obj = ARRAY_LIST_CLASS()
    for e in l: j_list_obj.add(e)
    return j_list_obj
```

##### 【2】相似查询

```python
# 创建相似库
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

# 加载替换库，替换为自己的，如果没有则项目提供的即可，能进一步提升查全率。
lib.textPreprocess.loadReplaceMapFromFile("./replace.txt")

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

# 相似检索
# 如果你想支持多线程检索，设置lib.allowMultiThreadsSearch为True。
# 只有命中数量大于lib.searchDocsUnit才会启用多线程。此值的取值范围最好在[10000-30000]，此值低则检索性能越高，此值高更能节省系统资源。
lib.allowMultiThreadsSearch = True
for result in lib.similaritySearch(
        """《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。
        辛弃疾的《水调歌头》在此之后。
        """,  # 查询内容
        10  # top_k
):
    print(f'id :{result.id}, score: {result.score}, title: {result.title}, text: {result.text}')

```

###### 输出
```text
id :1, score: 0.6464106850592721, title: 《梦游天姥吟留别》, text: 《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。
id :2, score: 0.5641475960385378, title: 《水调歌头·文字觑天巧》, text: 《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。
```

##### 【3】其他

其他方法，包括库的导入、库的导出、参数重定义以及AC自动机词库匹配的使用，见[README_python_call.md](README_python_call.md)。

python调用时，效率仅为java调用的0.55-0.65，但时间在可接受范围内。


### 三、特点概述

#### 1. 查询时间效率高，内存占用高

```text
【1】查询快
210万(平均205 chars)的文本库中，多线程文本相似检索时间133.954ms。

【2】内存占用高
由于查询速度的提升是一种空间换时间的方法，因此有较高的内存占用。
```


#### 2. 查全率（召回率）高

```text
【1】首先，n-gram方式具有位置无关性和通用性，本身很容易查到相似文本。

【2】此外，本程序支持替换库，通过近义词、简称补全、类型追加等方式进一步提升Recall。
```


#### 3. 调节能力和扩展能力强

```text
【1】有很多不同方面的因素能影响查询结果，本程序支持<strong>控制参数</strong>来调节不同因素的影响力。

【2】此外，提供了默认的得分差异化计算函数（AvgIdfGrowthCalculator），可继承扩展，实现自定义的计算方法。（虽然很多情况你不需要这么做）。
```

#### 4. 可控性强

```text
【1】无需训练，由于替换库的存在，相似语义变得可定义。

【2】如果结合其他模型，比如词语类型分析、词语相似分区计算，词语情感分析也可以自动化生成替换库。 TODO
```

#### 5. 适合查询频繁，不频繁更新的场合

```text
由于数据变更会导致失配指针和统计参数变化，需要较长的更新时间。原本设计的使用场合，是大量频繁查询，很少更新或者利用闲时更新。

如果你也想支持频繁更新，就需要采用读写分离：

主库用于查询，小库用于更新和查询。

（1）新增文章时，插入到小库。（小库的数量可以大于1）

（2）查询时候，主库和小库同时查询，整合结果返回。

（3）在合适的时机（比如小库达到了一定数量或者闲时状态），合并为一个主库，持久化、加载并替换正在使用的主库对象。

这属于工程化的内容，我并没有编写相关的功能。
```


#### 6. 多语言支持

###### 本程序提供[Java版本](README_java_call.md)和[Python版本](README_python_call.md)调用方式。

### 四、效率统计

#### （1）相似检索时间 多线程

![search.png](statistics%2Fsearch.png)
```text
【1】在系统配置一定的情况下，检索速度和searchDocsUnit取值有关，此值越低速度越快，消耗CPU资源也越大。此值建议在[10000-30000]范围内。
【2】测试时searchDocsUnit取值为30000，入库文章数量210万，每个文章平均205字符，多线程相似检索时间133.954ms。
```

#### （2）入库时间 = （插入时间+更新索引时间+导出时间）多线程
![import.png](statistics%2Fimport.png)
```text
入库文章数量: 210万(平均205 chars)，插入时间863.619s，刷新索引时间243.139s，持久化时间543.463s，总入库时间1650.221s。
```


#### （3）加载时间 多线程
![load.png](statistics%2Fload.png)
```text
入库文章数量: 210万(平均205 chars)，加载库时间229.188s。
```


#### （4）内存空间消耗
<font color="red">【1】此表不能代表最小内存需求，因为初始堆内存和最大堆内存设置为250G，</font>
<font color="red">Java JVM会受此影响，采用更激进的内存分配策略、更懒惰的内存释放策略，因此图表中的偏大很多。</font><br/>
<font color="red">【2】TODO 还需测试250G内存最多可支持的相似库文本数作为依据。</font>
![memory.png](statistics%2Fmemory.png)

#### （5）持久化 磁盘空间消耗
![disk.png](statistics%2Fdisk.png)
```text
入库文章数量: 210万(平均205 chars)，磁盘空间占用约19.871773404069245G。
```

### 五、评分指标

TODO


### 六、替换库说明

#### （1）格式说明

```text
# 停止词，标点符号
·
`
。
.

# 停止词，中文字词
的
了

# 停止词，英文
am
is
are
the

# 相似词语
高兴 快乐

# 类别追加，简称补全，提供更多信息。
SUV SUV运动型多用途汽车
```

#### （2）原则

```text
（1）停止词是必备的，减少干扰。一行只需一个词（字）。

（2）相似词语替换可进一步增加召回率（查全率），不过对于知识检索领域，没有相似词库也能取得较好的效果。

（3）类别追加，增加搜索的通用性，比如追加全称，或者追加一个类型。例如“老虎 老虎猫科”，这会显著增加召回率（查全率），但是精确率可能会有所降低（误杀率提高）。
（score可能会变得比较高，但排名不变）

（4）n-gram中n至少为2，无论n-gram中的n多大，如果一个词是单字词，最好扩展为双字，能增加查全率。比如“狗 狗狗”。
```

#### （3）其他

【1】如果你没有自己的替换库，可采用默认的替换库[replace.txt](replace.txt)。

【2】TODO，实现自动生成替换库的程序。

### 七、TODO列表

```text
1. 数字目前不支持精确匹配，有需求应追加参数控制。

2. AvgIdfGrowthCalculator采用线性均匀增加方式计算gram idf差异化得分，依靠idfGrowthK参数控制区分度。

可继承AvgIdfGrowthCalculator实现Sigmoid类S型曲线，扩大期望值附近的得分差距，减少高分之间（低分之间）的差距。
或者实现x^3类S型曲线，减少期望值附近的得分差距，增加高分之间（低分之间）的差距。

3. 利用map@1, map@10等评分指标，利用开源数据集计算评分值。

4. 提供根据若干文章自动生成停止词（例如tf-idf），替换词（设计快速相似分区算法，并选举组内的中心词，作为替换中心）。

5. 编写相似库管理工具（TextLibManagement）的代码示例，其支持库的内存磁盘管理和跨库检索。
```

### 八、效率测试平台说明

```text
处理器：AMD EPYC 7763 Processor 12核24线程 云服务器
内存：256G物理内存
操作系统：Ubuntu20
Java版本：GrailVM JDK 21
```

# 兄弟项目

https://github.com/aldebran97/springboot_text_toolkit/tree/master

如果您不用Java或者Python，提供了API访问方式，并且此项目也用作测试。

