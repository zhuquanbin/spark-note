# Spark CSV 

## PySpark (1.5+)
### DataFrame API 读写
```python
df = spark.read.csv("test.csv")
df.write.csv("test/csv")
```
### CSV 字段多行读取
>**Spark2.1以下版本**存在 Bug, 会存在读取异常问题
>>可以通过读取 Hadoop API 读取二进制文件解决
```python
def spark_read_csv_bf(spark, path, schema=None, encoding='utf8'):
    '''
    :param spark:    spark 2.0 sparkSession 
    :param path:     csv path
    :param encoding: 
    :return: DataFrame
    '''
    rdd = spark.sparkContext.binaryFiles(path).values()\
                .flatMap(lambda x: csv.DictReader(io.BytesIO(x)))\
                .map(lambda x : { k:v.decode(encoding) for  k,v in x.iteritems()})
    if schema:
        return spark.createDataFrame(rdd, schema)
    else:
        return rdd.toDF()
```

>**Spark2.2** 修复了此Bug <br> 
>>[[SPARK-19610][SQL] Support parsing multiline CSV files](https://github.com/apache/spark/pull/16976)
```python
df = spark.read.csv('test.csv', wholeFile=True)
```