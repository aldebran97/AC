# n-gram + AC自动机为基础实现的词库检索、文本相似检索

# 安装程序

## 1. 安装JDK，并配置环境变量

### （1）下载符合自己操作系统的JDK版本，并解压

GrailVM JDK: https://www.graalvm.org/downloads/

OpenJDK：https://jdk.java.net/archive/

本程序支持 JDK 8-21 版本

### （2）配置环境变量

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

## 2. 从源码安装此程序

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

## 3. 如果还需要python调用，可利用pyjnius库，不用python可跳过此步

### 安装pyjnius库

```text
# 如果JDK版本发生变更，需要重新安装pyjnius
# pip uninstall pyjnius
pip install pyjnius==1.5.0
```