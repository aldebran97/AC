# n-gram + AC自动机为基础实现的词库检索、文本相似检索

# 简介

## 一、功能概述

### 1. AC Class用于海量词库快速检索

### 2. TextSimilaritySearch Class用于大量文本的快速相似检索

## 二、特点概述

### 1. 高效率查询

入库文章数量: 130万(平均205 chars)，单线程文本相似检索时间205.568ms。

入库文章数量: 190万(平均205 chars)，多线程文本相似检索时间89.228ms。

### 2. 高查全率（召回率）

首先n-gram方式本身很容易查到相似文本。

此外，本程序支持替换库，通过近义词、简称补全、类型追加进一步提升召回率。

### 3. 可调参以适应不同的需求

有很多不同方面的因素能影响查询结果，本程序支持<strong>控制参数</strong>来调节不同因素的影响力。

此外，AvgIdfGrowthCalculator提供了默认的得分差异化计算函数，可继承扩展，实现自定义的计算方法。

### 4. 无需训练，可控性好

无需训练，由于替换库的存在，相似语义变得可定义、可控制。

如果结合其他模型，比如<strong>词语类型分析</strong>、<strong>词语相似计算</strong>，也可以帮助生成替换库。（<strong>自动化</strong>）

### 5. 高内存占用

由于查询速度的提升是一种空间换时间的方法，因此有较高的内存占用。

### 6. 适合查询频繁，不频繁更新的场合

由于数据变更会导致失配指针和统计参数变化，需要较长的更新时间。原本设计的使用场合，是大量频繁查询，很少更新或者利用闲时更新。

##### 如果你也想支持频繁更新，就需要采用读写分离：

主库用于查询，小库用于更新和查询。

（1）新增文章时，插入到小库。（小库的数量可以大于1）

（2）查询时候，主库和小库同时查询，整合结果返回。

（3）在合适的时机（比如小库达到了一定数量或者闲时状态），合并为一个主库，持久化、加载并替换正在使用的主库对象。

这属于工程化的内容，我并没有编写相关的功能。

### 7. 多语言支持

本程序提供[Java版本](README_java_call.md)和[Python版本](README_python_call.md)调用方式

## 三、效率统计

### 时间效率分析

#### 相似检索时间 单线程
![search.png](statistics%2Fsearch.png)
```text
入库文章数量: 170万(平均205 chars)，单线程相似检索时间329.908ms。
入库文章数量: 130万(平均205 chars)，单线程相似检索时间205.568ms。
入库文章数量: 40万(平均205 chars)，单线程相似检索时间47.27ms。
```

#### 相似检索时间 多线程
![search-mul.png](statistics%2Fsearch-mul.png)
```text
入库文章数量: 190万(平均205 chars)，多线程相似检索时间89.228ms。
入库文章数量: 170万(平均205 chars)，多线程相似检索时间90.932ms。
入库文章数量: 130万(平均205 chars)，多线程相似检索时间75.47ms。
```

#### 入库时间 = （插入时间+更新索引时间+导出时间）
![import.png](statistics%2Fimport.png)
```text
入库文章数量: 170万(平均205 chars)，插入时间632.025s，刷新索引时间193.835s，持久化时间1385.131s，总入库时间2210.991s。
入库文章数量: 130万(平均205 chars)，插入时间544.053s，刷新索引时间109.223s，持久化时间801.225s，总入库时间1454.501s。
入库文章数量: 40万(平均205 chars)，插入时间100.121s，刷新索引时间28.322s，持久化时间282.966s，总入库时间411.409s。
```


#### 加载时间
![load.png](statistics%2Fload.png)
```text
入库文章数量: 170万(平均205 chars)，加载库时间490.736s。
入库文章数量: 130万(平均205 chars)，加载库时间331.85s。
入库文章数量: 40万(平均205 chars)，加载库时间114.389s。
```

<font color="blue">TODO 多线程导入导出</font>

### 空间效率统计

#### 内存空间消耗
![memory.png](statistics%2Fmemory.png)
```text
入库文章数量: 170万(平均205 chars)，内存占用约121.63801880180836G。
入库文章数量: 130万(平均205 chars)，内存占用约92.1009111776948G。
入库文章数量: 40万(平均205 chars)，内存占用约35.15116659551859G。
```

#### 持久化 磁盘空间消耗
![disk.png](statistics%2Fdisk.png)
```text
入库文章数量: 170万(平均205 chars)，磁盘空间占用约16.23755175806582G。
入库文章数量: 130万(平均205 chars)，磁盘空间占用约12.883799713104963G。
入库文章数量: 40万(平均205 chars)，磁盘空间占用约4.758458849973977G。
```

## 三、安装

安装本程序，见[README_install.md](README_install.md)

## 四、使用方法

以下都有JAVA和python代码

java说明见[README_java_call.md](README_java_call.md)

java代码示例见[TempTest.java](src%2Fcom%2Faldebran%2Ftext%2FTempTest.java)

python说明见[README_python_call.md](README_python_call.md)

python代码示例见[python_call_example.py](python_call_example.py)

python调用时，效率仅为java调用的0.55-0.65，但时间在可接受范围内。

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

# 停止词，英文
am
is
are
the

# 相似词语
高兴 快乐

# 类别追加，简称补全
SUV SUV运动型多用途汽车
```

原则

```text
（1）停止词是必备的，减少干扰。一行只需一个词（字）。

（2）相似词语替换可进一步增加召回率（查全率），不过对于知识检索领域，没有相似词库也能取得较好的效果。

（3）类别追加，增加搜索的通用性，比如追加全称，或者追加一个类型。例如“老虎 猫科”，这会显著增加召回率（查全率），但是精确率可能会有所降低（误杀率提高）。
（score可能会变得比较高，但排名不变）

（4）无论n-gram中的n多大，如果一个词是单字词，最好扩展为双字，能增加查全率。比如“狗 狗狗”。
```

## 六、注意事项

```text
1. 当文章非常多的时候，要指定很大的Xss和Xms，例如：
-Xss1024m -Xms30g

```

## 七、TODO

```text
1. 数字目前不支持精确匹配，有需求应追加参数控制。

2. AvgIdfGrowthCalculator采用线性均匀增加方式计算gram idf差异化得分，依靠idfGrowthK参数控制区分度。

可继承AvgIdfGrowthCalculator实现Sigmoid类S型曲线，扩大期望值附近的得分差距，减少高分之间（低分之间）的差距。
或者实现x^3类S型曲线，减少期望值附近的得分差距，增加高分之间（低分之间）的差距。

3. 文本相似库多线程导入导出，显著增加效率。

4. 利用map@1, map@10等评分指标，利用开源数据集计算评分值。
```

## 八、效率测试平台说明

```text
处理器：AMD EPYC 7763 Processor 12核24线程 云服务器
内存：256G物理内存
操作系统：Ubuntu20
Java版本：GrailVM JDK 21
```

# 兄弟项目

https://github.com/aldebran97/springboot_text_toolkit/tree/master

如果您不用Java或者Python，提供了API访问方式，并且此项目也用作测试。

