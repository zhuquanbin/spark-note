# Read config file from S3

## why?
任务中的轻量级配置放在s3上， 在spark driver 端上加载过来， 通过参数传递到 map 中执行， 在效率上远高于使用spark read 成 DataFrame， 再进行Join来的快很多倍。

## How to?
```scala

def readSignalFile(path:String)(implicit spark:SparkSession) : BufferedSource = {
  val tpath = new Path(path)
  val tfs = tpath.getFileSystem(spark.sparkContext.hadoopConfiguration)
  // 装换为 scala Source
  Source.fromInputStream(tfs.open(tpath))
}

def get_oui_mac(implicit spark: SparkSession): mutable.HashSet[String] ={
  val oui_path    = "s3n://*/*/configs.txt"
  var oui_mac = new mutable.HashSet[String]()
  for (line <- readSignalFile(oui_path)(spark).getLines()){
    oui_mac += line.trim
  }
  oui_mac
}
```
