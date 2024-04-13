### tokenizer包用于分词

一、Java类：

（1）[Tokenizer.java](Tokenizer.java)[WordProcess.java](WordProcess.java)是抽象类，如果你有自己的分词器需要实现这个接口。

（2）[NGramTokenizer.java](NGramTokenizer.java)
利用n-gram词包实现的分词器，特点是检索准确率高，但是空间资源消耗大。


（3）[DAGTokenizer.java](DAGTokenizer.java)
利用DAG有向无环图实现的分词器，是一种无监督-无标签的分词器，空间资源消耗小。支持训练自己的分词器

二、代码示例：

（1）NGramTokenizer

【1】基本使用

```java
NGramTokenizer nGramTokenizer = new NGramTokenizer(2);
System.out.println(nGramTokenizer.tokenize("利用n-gram词包实现的分词器，特点是检索准确率高，但是空间资源消耗大。"));
```

输出：
```text
[利用, 用n, n, gram, gram词, 词包, 包实, 实现, 分词, 词器, 特点, 点是, 是检, 检索, 索准, 准确, 确率, 率高, 但是, 是空, 空间, 间资, 资源, 源消, 消耗, 耗大]
```
NGramTokenizer对英文词的处理：除了n-gram之外，单词本身也会出现在结果中。

【2】高级使用
```java
NGramTokenizer nGramTokenizer = new NGramTokenizer(2, Arrays.asList(
                new EnglishRootProcess(new File("./english_root.txt")),
                new WordParentProcess(new File("./word_parent.json")),
                new WordSimilarityProcess(new File("./word_similarity.json"))
        ));
        System.out.println(nGramTokenizer.tokenize("老虎凶猛无匹，棕熊力大无穷，皆为森林之王。"));
```
输出：
```text
[老虎, 猫科, 动物, 虎凶, 凶猛, 猛无, 无匹, 棕熊, 犬科, 动物, 熊力, 犬科, 动物, 力大, 大无, 无穷, 皆为, 为森, 森林, 林之, 之王]
```

词语预处理使用见：[README.md](..%2Fpreprocess%2FREADME.md)

（2）DAGTokenizer

基本使用
```java
DAGTokenizer dagTokenizer = new DAGTokenizer();
dagTokenizer.train(texts); // texts是你的语料库，可以多次调用train方法。你需要在大量语料库中训练。
dagTokenizer.update(); // DAGTokenizer使用前必须更新
System.out.println(dagTokenizer.tokenize("一个需要分词的句子"));
```

高级初始化
```java
DAGTokenizer dagTokenizer = new DAGTokenizer(
        Arrays.asList(2, 3, 4), // 支持的词长，默认支持2-4字词
        -1, // 每隔多少个文档执行一次词库过滤，去除不重要的词，降低空间消耗
        new File("./vocab.txt"), // 自定义词库文件，DAGTokenizer能自动识别词语，但需要大量语料训练。如果你的语料库不够，可以自定义词库。（TODO 目前此项尚未支持）
        Arrays.asList(
            new EnglishRootProcess(new File("./english_root.txt")),
            new WordParentProcess(new File("./word_parent.json")),
            new WordSimilarityProcess(new File("./word_similarity.json"))
        ) // 词语预处理器
        );
```

保存
```java
dagTokenizer.save(new File("./SaveDAGTokenizer"));
```

加载
```java
DAGTokenizer dagTokenizer = new DAGTokenizer();
dagTokenizer.load(new File("./SaveDAGTokenizer"));
System.out.println(dagTokenizer.tokenize("一个需要分词的句子"));
```
