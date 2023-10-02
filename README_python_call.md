# ngram + AC自动机为基础实现的词库检索、文本相似检索

# python代码说明

### 1. 导入依赖 + 准备工作


CLASSPATH中指定jar路径，指定JAVA_HOME

```shell
import os

os.environ['CLASSPATH'] = r"C:\Users\aldebran\user_dir\code\AC\out2\AC.jar"
os.environ['JAVA_HOME'] = r"D:\Program Files\Java\jdk-1.8\bin"

import jnius_config

# 指定了堆内存2G，栈内存1024M。根据文章数量和文章长度调整。（文章数量对内存的影响比文章长度大）
jnius_config.add_options('-Xms2g', '-Xmx2g', '-Xss1024m')

from jnius import autoclass

AC_CLASS = autoclass('com.aldebran.text.ac.AC')
AC_PLUS_CLASS = autoclass('com.aldebran.text.ac.ACPlus')
TextSimilaritySearchClass = autoclass('com.aldebran.text.similarity.TextSimilaritySearch')
ARRAY_LIST_CLASS = autoclass('java.util.ArrayList')
FILE_CLASS = autoclass('java.io.File')

# 定义了Java列表
def j_list(l: list):
    j_list_obj = ARRAY_LIST_CLASS()
    for e in l: j_list_obj.add(e)
    return j_list_obj
```

### 2. 海量词库检索


```shell
ac = AC_CLASS() # 如果您的词库有包含关系，请用AC_PLUS_CLASS
ac.addWords(j_list(['word1', 'word2', 'word3'])) # 插入若干词
ac.update() # 更新失配指针，首次必须调用。之后词库更新不宜频繁调用，应在恰当的时机调用（比如累计追加数量到达阈值，或者间隔时间到达阈值）。
# 词库匹配
for mr in ac.indexOf("001word1002word0003word2"):
    print('index:', mr.index, 'word:', mr.word)
    
ac_lib_file = FILE_CLASS("./test-ac-py")

AC_CLASS.save(ac,ac_lib_file) # 保存库

ac = AC_CLASS.load(ac_lib_file) # 加载库，如果您用了AC_PLUS_CLASS，请用AC_PLUS_CLASS.load
```

输出

```text
index: 3 word: word1
index: 19 word: word2
```

### 2. 文本检索，相似检索

##### 插入查询用法

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

# 如果你想支持多线程检索，设置lib.allowMultiThreadsSearch为True。
# 只有文章数量大于lib.searchDocsUnit才会启用多线程。
# lib.allowMultiThreadsSearch = True

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

```shell
outFile = FILE_CLASS("./test-lib");  # 替换为自己的路径
TextSimilaritySearchClass.save(lib, outFile)
```

#### 加载


```shell
inFile = FILE_CLASS("./test-lib")  # 替换为自己的路径
lib = TextSimilaritySearchClass.load(inFile)
```