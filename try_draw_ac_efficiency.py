"""
画统计结果
"""
import json

import matplotlib.pyplot as plt
import matplotlib as mpl

mpl.rcParams["font.sans-serif"] = ["SimHei"]
mpl.rcParams["axes.unicode_minus"] = False


def draw_line_chart(width, height,
                    x_label, y_label,
                    key_x_values: dict, key_y_values, key_colors_map,
                    x_lim, y_lim):
    plt.figure(figsize=(width, height), dpi=200)

    # 去除顶部和右边框框
    ax = plt.axes()
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)

    plt.xlabel(x_label)  # x轴标签
    plt.ylabel(y_label)  # y轴标签
    plt.xlim(x_lim[0], x_lim[1])
    plt.ylim(y_lim[0], y_lim[1])

    keys = list(key_x_values.keys())

    for key in keys:
        x_values = key_x_values[key]
        y_values = key_y_values[key]
        color = key_colors_map[key]
        plt.plot(x_values, y_values, linewidth=1, linestyle="solid", label=key, color=color)

    plt.legend()
    return plt
    pass


with open(r'C:\Users\aldebran\user_dir\code\AC\statistics\效率统计-多线程.txt', mode='r') as fp:
    file_content = fp.read()

data = list(
    map(lambda it: json.loads(it), filter(lambda it: it != '', map(lambda it: it.strip(), file_content.split('\n')))))
print(data)


def draw_memory_chart():
    keys = ['内存空间消耗']
    x_label = '文本数量（单位万）'
    key_x_values = {'内存空间消耗':
                        list(map(lambda it: it['textsCount'] // 10000, data))}
    key_y_values = {'内存空间消耗':
                        list(map(lambda it: it['takeMemorySize'], data))
                    }
    key_color_map = {'内存空间消耗': 'red'}
    plt = draw_line_chart(8, 5, x_label, '内存空间消耗 单位G',
                          key_x_values, key_y_values, key_color_map,
                          x_lim=[0, 180], y_lim=[0, 130])
    plt.savefig(f'result/memory.png')


def draw_disk_chart():
    keys = ['磁盘空间消耗']
    x_label = '文本数量（单位万）'
    key_x_values = {'磁盘空间消耗':
                        list(map(lambda it: it['textsCount'] // 10000, data))}
    key_y_values = {'磁盘空间消耗':
                        list(map(lambda it: it['diskFileSize'], data))
                    }
    key_color_map = {'磁盘空间消耗': 'red'}
    plt = draw_line_chart(8, 5, x_label, '磁盘空间消耗 单位G',
                          key_x_values, key_y_values, key_color_map,
                          x_lim=[0, 180], y_lim=[0, 17])
    plt.savefig(f'result/disk.png')


def draw_search_chart():
    keys = ['相似检索时间']
    x_label = '文本数量（单位万）'
    key_x_values = {'相似检索时间':
                        list(map(lambda it: it['textsCount'] // 10000, data))}
    key_y_values = {'相似检索时间':
                        list(map(lambda it: it['searchTime'], data))
                    }
    print(key_x_values)
    print(key_y_values)
    key_color_map = {'相似检索时间': 'red'}
    plt = draw_line_chart(8, 5, x_label, '相似检索时间 单位ms',
                          key_x_values, key_y_values, key_color_map,
                          x_lim=[0, 180], y_lim=[0, 120])
    plt.savefig(f'result/search.png')


def draw_load_chart():
    keys = ['库加载时间']
    x_label = '文本数量（单位万）'
    key_x_values = {'库加载时间':
                        list(map(lambda it: it['textsCount'] // 10000, data))}
    key_y_values = {'库加载时间':
                        list(map(lambda it: it['loadTime'], data))
                    }
    key_color_map = {'库加载时间': 'red'}
    plt = draw_line_chart(8, 5, x_label, '库加载时间 单位s',
                          key_x_values, key_y_values, key_color_map,
                          x_lim=[0, 180], y_lim=[0, 500])
    plt.savefig(f'result/load.png')


def draw_import_chart():
    keys = ['插入时间', '更新索引时间', '持久化时间', '总入库时间']
    x_label = '文本数量（单位万）'
    key_x_values = {'插入时间': list(map(lambda it: it['textsCount'] // 10000, data))}
    key_x_values['更新索引时间'] = key_x_values['持久化时间'] = key_x_values['总入库时间'] = key_x_values['插入时间']
    key_y_values = {'插入时间': list(map(lambda it: it['insertTime'], data)),
                    '更新索引时间': list(map(lambda it: it['updateTime'], data)),
                    '持久化时间': list(map(lambda it: it['saveTime'], data)),
                    '总入库时间': list(map(lambda it: it['importTime'], data))
                    }
    key_color_map = {'插入时间': 'green',
                     '更新索引时间': 'blue',
                     '持久化时间': 'black',
                     '总入库时间': 'red'
                     }
    plt = draw_line_chart(8, 5, x_label, '时间 单位s',
                          key_x_values, key_y_values, key_color_map,
                          x_lim=[0, 180], y_lim=[0, 2300])
    plt.savefig(f'result/import.png')


# draw_memory_chart()
# draw_disk_chart()
draw_search_chart()
# draw_load_chart()
# draw_import_chart()
