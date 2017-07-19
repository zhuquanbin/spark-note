## 问题描述

```bash
Exception in thread "main" org.apache.spark.SparkException: Task not serializable
        at org.apache.spark.util.ClosureCleaner$.ensureSerializable(ClosureCleaner.scala:298)
        at org.apache.spark.util.ClosureCleaner$.org$apache$spark$util$ClosureCleaner$$clean(ClosureCleaner.scala:288)
        at org.apache.spark.util.ClosureCleaner$.clean(ClosureCleaner.scala:108)
        at org.apache.spark.SparkContext.clean(SparkContext.scala:2101)
        at org.apache.spark.rdd.RDD$$anonfun$filter$1.apply(RDD.scala:387)
        at org.apache.spark.rdd.RDD$$anonfun$filter$1.apply(RDD.scala:386)
        at org.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:151)
        at org.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:112)
        at org.apache.spark.rdd.RDD.withScope(RDD.scala:362)
        at org.apache.spark.rdd.RDD.filter(RDD.scala:386)
        at com.wiwide.sniffer.SnifferAnalyse$.main(SnifferAnalyse.scala:130)
        at com.wiwide.sniffer.SnifferAnalyse.main(SnifferAnalyse.scala)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.apache.spark.deploy.SparkSubmit$.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:743)
        at org.apache.spark.deploy.SparkSubmit$.doRunMain$1(SparkSubmit.scala:187)
        at org.apache.spark.deploy.SparkSubmit$.submit(SparkSubmit.scala:212)
        at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:126)
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: java.io.NotSerializableException: com.wiwide.sniffer.SnifferLog$
Serialization stack:
        - object not serializable (class: 
        - field 
        - object 
        at org.apache.spark.serializer.SerializationDebugger$.improveException(SerializationDebugger.scala:40)
        at org.apache.spark.serializer.JavaSerializationStream.writeObject(JavaSerializer.scala:46)
        at org.apache.spark.serializer.JavaSerializerInstance.serialize(JavaSerializer.scala:100)

```


## 解决方法
[Solved Functions Reference](https://github.com/samthebest/dump/blob/master/sams-scala-tutorial/serialization-exceptions-and-memory-leaks-no-ws.md)
* 尽量避免使用class， 用object代替
* 定义的class或对象必须是可序列化的 extends Serializable 