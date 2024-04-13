# 文本处理工具Java实现


### 【1】分词器

（1）目前支持n-gram分词器和DAG分词器，DAG分词器可用自己的语料训练自己的分词器。

（2）对相似语义提供支持。

详细说明 见[README.md](src%2Fmain%2Fjava%2Fcom%2Faldebran%2Ftext%2Ftokenizer%2FREADME.md)

### 【2】海量词库匹配

基于AC自动机实现海量词库的高效匹配，这是一种利用空间换时间的方法。其匹配时间与输入长度成正比，受词库数量影响极小。

详细说明和效率统计 TODO

### 【3】文本相似检索

实现了支持大量数据、多字段、多权重、相似语义的毫秒级相似检索的方法。

详细说明、统计数据和评分数据 见[README.md](src%2Fmain%2Fjava%2Fcom%2Faldebran%2Ftext%2Fsimilarity%2FREADME.md)