Databricks Spark 知识库
=====================================

* [最佳实践](best_practices/README.md)
    * [避免使用 GroupByKey](best_practices/prefer_reducebykey_over_groupbykey.md)
    * [不要将大型 RDD 的所有元素拷贝到请求驱动者](best_practices/dont_call_collect_on_a_very_large_rdd.md)
* [常规故障处理](troubleshooting/README.md)
    * [Job aborted due to stage failure: Task not serializable](troubleshooting/java_io_not_serializable_exception.md)
    * [缺失依赖](troubleshooting/missing_dependencies_in_jar_files.md)
    * [执行 start-all.sh 错误 - Connection refused](troubleshooting/port_22_connection_refused.md)
    * [Spark 组件之间的网络连接问题](troubleshooting/connectivity_issues.md)
* [性能 & 优化](performance_optimization/README.md)
    * [一个 RDD 有多少个分区](performance_optimization/how_many_partitions_does_an_rdd_have.md)
    * [数据本地性](performance_optimization/data_locality.md)
* [Spark Streaming](spark_streaming/README.md)
    * [ERROR OneForOneStrategy](spark_streaming/error_oneforonestrategy.md)

## Copyright

本文翻译自: http://databricks.gitbooks.io/databricks-spark-knowledge-base/ 著作权归原作者所有。

## License

此内容使用的授权许可请查看[这里](LICENSE)。