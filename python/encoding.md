# 编码问题

## 乱码
> 在数据处理遇到乱码时不知道该读取的字符串是什么编码的时候，想要解析成通用的Unicode编码是比较困难的， 这时候需要知道该乱码字符串是什么编码， 使用库 Python库 [chardet](https://github.com/chardet/chardet)可以快速了解该乱码的编码。

## 使用
> install
```bash
pip install chardet
```
> example
```python
import chardet
str1 = ...
encoding = chardet.detect(str1)['encoding']
# to unicode
print str1.decode(encoding)
```