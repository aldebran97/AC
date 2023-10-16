"""
python调用相似检索例子
"""

import os

os.environ['CLASSPATH'] = r"/Users/aldebran/custom/code/AC/out2/AC.jar" # jar包位置，替换为自己的
os.environ['JAVA_HOME'] = r"/Library/Java/JavaVirtualMachines/jdk1.8.0_291.jdk/Contents/Home" # JAVA_HOME

import jnius_config

# 指定了堆内存1G，栈内存512M。根据文章数量和文章长度调整。（文章数量对内存消耗的影响比文章长度大）
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


# （1）开始使用AC自动机，用于海量词库匹配。
ac = AC_CLASS()  # 实例化AC自动机，如果您的词库有包含关系，请用AC_PLUS_CLASS。
ac.addWords(j_list(['word1', 'word2', 'word3']))  # 插入若干词
ac.update()  # 更新失配指针，首次必须调用。之后词库更新不宜频繁调用，应在恰当的时机调用（比如累计追加数量到达阈值，或者间隔时间到达阈值）。
# 词库匹配
for mr in ac.indexOf("001word1002word0003word2"):
    print('index:', mr.index, 'word:', mr.word)

# 保存AC自动机库到磁盘
ac_save_folder = FILE_CLASS("./test-ac-py")  # 替换为自己的路径，是个目录

AC_CLASS.save(ac, ac_save_folder, True)  # 最后一个参数表示是否启用多线程

# 加载AC自动机到内存
ac = AC_CLASS.load(ac_save_folder, True)  # 最后一个参数表示是否启用多线程

# （2）开始使用文本相似检索库，用于海量文本的相似搜索。
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
# lib.allowMultiThreadsSearch = True
for result in lib.similaritySearch(
        """《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。
        辛弃疾的《水调歌头》在此之后。
        """,  # 查询内容
        10  # top_k
):
    print(f'id :{result.id}, score: {result.score}, title: {result.title}, text: {result.text}')

# 修改参数
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

# 导出库
lib_folder = FILE_CLASS("./test-lib-py")  # 替换为自己的路径，是个目录
TextSimilaritySearchClass.save(lib, lib_folder, True)  # 最后一个参数表示是否启用多线程

# 导入库
lib = TextSimilaritySearchClass.load(lib_folder, True)  # 最后一个参数表示是否启用多线程
