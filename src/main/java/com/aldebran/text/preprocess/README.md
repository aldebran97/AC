### preprocess包用于预处理

一、文本预处理：

【1】Java类/接口:[TextPreprocess.java](TextPreprocess.java)

【2】功能：

（1）利用停止词完成片段分割

（2）获取最小字单元：对于英文来说，最小单元是单词（词根），对于中文来说，最小单元是汉字。

【3】代码示例：

二、词语处理：

【1】Java类/接口:[WordProcess.java](WordProcess.java)，[WordParentProcess.java](WordParentProcess.java)，[WordSimilarityProcess.java](WordSimilarityProcess.java)

【2】功能：

（1）[WordProcess.java](WordProcess.java)是接口，如果你有自己的词语处理方法需要实现这个接口。

（2）[WordParentProcess.java](WordParentProcess.java)用于处理单词的属类，可以是多级的祖先。

（3）[WordSimilarityProcess.java](WordSimilarityProcess.java)
用于处理相似词语，我在这里认为全部词语可以按照相似度分组，在每个分组中选择一个中心词（与组内其他词相似度均值最大的词），所有其他词都对应这个中心词。

（4）[EnglishRootProcess.java](EnglishRootProcess.java)
用于处理英文词根。

【3】输入文件格式：

[WordParentProcess.java](WordParentProcess.java)接受的输入文件格式：

[word_parent.json](..%2F..%2F..%2F..%2F..%2F..%2F..%2Fword_parent.json)
```json
{
  "动物": {
    "猫科": {
      "老虎": null,
      "豹": null
    },
    "犬科": {
      "狐狸": null,
      "熊": null
    }
  }
}
```

解析结果就是老虎父类是猫科，猫科父类是动物。 

[WordSimilarityProcess.java](WordSimilarityProcess.java)接受的输入文件格式：

[word_similarity.json](..%2F..%2F..%2F..%2F..%2F..%2F..%2Fword_similarity.json)
```json
{
  "全": {
    "全部": null,
    "所有": null,
    "全体": null,
    "整体": null,
    "整个": null
  },
  "快乐": {
    "高兴": null,
    "喜悦": null,
    "愉悦": null,
    "兴高采烈": null,
    "心花怒放": null
  },
  "悲伤": {
    "郁闷": null,
    "惆怅": null,
    "忧伤": null,
    "忧郁": null
  },
  "信息": {
    "简介": null,
    "生平": null,
    "介绍": null
  }
}
```

[EnglishRootProcess.java](EnglishRootProcess.java)接受的输入文件格式：

[english_root.txt](..%2F..%2F..%2F..%2F..%2F..%2F..%2Fenglish_root.txt)

```text
# 人民
popul###
# 不，消失掉
###able
```
以三个#开头表示后缀，以三个#结尾表示前缀，否则只要包含即可。
