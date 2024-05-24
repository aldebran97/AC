"""
ac相似检索
@author aldebran
"""
from conf import config
import os

os.environ['CLASSPATH'] = config.jar_path
os.environ['JAVA_HOME'] = config.java_home

import jnius_config

jnius_config.add_options(config.Xms, config.Xmx, config.Xss)

from jnius import autoclass

AC_CLASS = autoclass('com.aldebran.text.ac.AC')
AC_PLUS_CLASS = autoclass('com.aldebran.text.ac.ACPlus')
TextSimilaritySearchClass = autoclass('com.aldebran.text.similarity.TextSimilaritySearch')
ARRAY_LIST_CLASS = autoclass('java.util.ArrayList')
FILE_CLASS = autoclass('java.io.File')
DAG_TOKENIZER_CLASS = autoclass('com.aldebran.text.tokenizer.DAGTokenizer')
NGRAM_TOKENIZER_CLASS = autoclass('com.aldebran.text.tokenizer.NGramTokenizer')
WORD_SIMILARITY_PROCESS_CLASS = autoclass('com.aldebran.text.preprocess.WordSimilarityProcess')
WORD_PARENT_PROCESS_CLASS = autoclass('com.aldebran.text.preprocess.WordParentProcess')
ENGLISH_ROOT_PROCESS_CLASS = autoclass('com.aldebran.text.preprocess.EnglishRootProcess')


def j_list(l: list):
    j_list_obj = ARRAY_LIST_CLASS()
    for e in l: j_list_obj.add(e)
    return j_list_obj


# AC自动机版本的相似检索
class DocumentACIndex():
    def __init__(self,
                 lib_name: str = None,
                 lib_path: str = None,
                 title_k=3.0,
                 content_k=2.0,
                 critical_score=0.5,
                 critical_content_hit_count=2,
                 critical_title_hit_count=2,
                 bm25_k=1.2,
                 bm25_b=0.1,
                 idf_growth_k=10,
                 tokenizer=None,
                 ):
        if not lib_name or not lib_path: return
        if tokenizer is None: tokenizer = NGRAM_TOKENIZER_CLASS()
        self.lib = TextSimilaritySearchClass(
            critical_content_hit_count,
            critical_title_hit_count,
            critical_score,
            content_k,
            title_k,
            bm25_k,
            bm25_b,
            idf_growth_k,
            tokenizer,
            lib_name,
            FILE_CLASS(lib_path)
        )
        pass

    def add_text(self, custom_id, title: str, content: str, extra_data=None, outer_weight=1.0, ):
        if extra_data is not None:
            raise Exception('extra data not supported!')
        self.lib.addText(
            content,
            title,
            str(custom_id),
            outer_weight
        )
        pass

    def get_by_custom_id(self, custom_id):
        result = self.lib.queryById(str(custom_id))
        if result is None: return None
        return {
            'custom_id': result.id,
            'title': result.title,
            'content': result.content,
        }

    def has_text(self, custom_id):
        return self.lib.containsText(str(custom_id))

    def similarity_search(self, text: str, top_k=10, threshold=0.5, ):
        results = []
        for result in self.lib.similaritySearch(text, top_k):
            if result.score >= threshold:
                results.append({
                    'custom_id': result.id,
                    'score': result.score,
                    'title': result.title,
                    'content': result.content
                })
        return results

    # 插入文档后需要更新索引
    def update(self):
        self.lib.update()


def save_index(index: DocumentACIndex):
    TextSimilaritySearchClass.save(index.lib, index.lib.libFolder, True)


def load_index(lib_path: str, tokenizer=None) -> DocumentACIndex:
    assert os.path.isdir(lib_path)
    if tokenizer is None: tokenizer = NGRAM_TOKENIZER_CLASS()
    index = DocumentACIndex()
    index.lib = TextSimilaritySearchClass.load(FILE_CLASS(lib_path), True)
    index.lib.tokenizer = tokenizer
    return index

# if __name__ == '__main__':
#     index = DocumentACIndex('test', './test-lib')
#     index.add_text(1, '你好，欢迎光临', '您好，欢迎光临')
#     index.add_text(2, '再见，我先撤了', '再见，下次再约')
#     index.update()
#     print(index.similarity_search('你好，我先撤了', threshold=0.001))
#     save_index(index)
#     index = None
#     index = load_index('./test-lib', NGRAM_TOKENIZER_CLASS())
#     print(index.similarity_search('你好，我先撤了', threshold=0.001))
#     pass
