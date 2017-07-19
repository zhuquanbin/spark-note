# Spark 读取小文件

## Why？
小文件太多会导致spark作业效率低下，生成过多partition，通过合并小文件可提升效率且能有效控制每个partition大小，控制spark stage task 数量， 且也不易造成executer OOM
 
## How to?
```scala
  val max_input_size = 32*1024*1024
  val conf = spark.sparkContext.hadoopConfiguration
  conf.set("mapreduce.input.fileinputformat.split.maxsize", options.getOrElse("mapreduce.split.maxsize", max_input_size.toString))
  val log:RDD[Text] = spark.sparkContext.newAPIHadoopFile(
      path,
      classOf[CombineTextInputFormat],
      classOf[LongWritable],
      classOf[Text] ,
      conf).map(row => row._2)
```
