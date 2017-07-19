# spark 操作 hbase

Example Reference [Hbase rdd](https://github.com/unicredit/hbase-rdd)


# Build 
> spark 2.0 later version 修改 HbaseReadSupport.scala  
```scala
trait HBaseReadSupport {
  implicit def toHBaseSC(spark: SparkSession): HBaseSC = new HBaseSC(spark.sparkContext)
}
```

> maven pom.xml 依赖包
```xml
<properties>
    <scala_version>2.11</scala_version>
    <scala.version>2.11.8</scala.version>
    <spark.version>2.1.1</spark.version>
    <hbase.version>1.3.0</hbase.version>
</properties>

<repositories>
    <repository>
        <id>aliyun</id>
        <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    </repository>
    <repository>
        <id>maven2</id>
        <url>http://repo1.maven.org/maven2</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <releases>
            <enabled>true</enabled>
        </releases>
    </repository></repositories>

<dependencies>
    <!-- spark -->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-sql_${scala_version}</artifactId>
        <version>${spark.version}</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-core_${scala_version}</artifactId>
        <version>${spark.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- hbase -->
    <dependency>
        <groupId>org.apache.hbase</groupId>
        <artifactId>hbase-common</artifactId>
        <version>${hbase.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hbase</groupId>
        <artifactId>hbase-client</artifactId>
        <version>${hbase.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hbase</groupId>
        <artifactId>hbase-server</artifactId>
        <version>${hbase.version}</version>
    </dependency>
</dependencies>
```

# spark submit packages
```bash
spark-submit --master yarn  \
    --packages org.apache.hbase:hbase-hadoop-compat:1.3.0,org.apache.hbase:hbase-server:1.3.0,org.apache.hbase:hbase-common:1.3.0,org.apache.hbase:hbase-client:1.3.0 \
    --repositories http://maven.aliyun.com/nexus/content/groups/public/
    ...
```

# toHBaseBulk function
[Hbase rdd](https://github.com/unicredit/hbase-rdd) 提供的是通用模板, 在一些情况下可以自定义 toHBaseBulk function, 可提高效率, 例如：RDD[(K, Map[Q, A])] toHbaseBulk 方法 中对 Map 进行 flatMap， 假如你的数据集为RDD[(K, Q, A)]，没有必要对其进行转换为RDD[(K, Map[Q, A])]来适应， 可以进行适当改写。 
```scala
final class HFileRDDSimple[K: Writes, Q: Writes, C: ClassTag, A: ClassTag, V: ClassTag](mapRdd: RDD[(K, Map[Q, A])], ck: GetCellKey[C, A, V], kvf: KeyValueWrapperF[C, V]) extends HFileRDDHelper {
  /**
   * Load the underlying RDD to HBase, using HFiles.
   *
   * Simplified form, where all values are written to the
   * same column family.
   *
   * The RDD is assumed to be an instance of `RDD[(K, Map[Q, A])]`,
   * where the first value is the rowkey and the second is a map that
   * associates column names to values.
   */
  def toHBaseBulk(tableNameStr: String, family: String, numFilesPerRegionPerFamily: Int = 1)
                 (implicit config: HBaseConfig, ord: Ordering[C]): Unit = {
    require(numFilesPerRegionPerFamily > 0)
    val wk = implicitly[Writes[K]]
    val wq = implicitly[Writes[Q]]
    val ws = implicitly[Writes[String]]

    val conf = config.get
    val tableName = TableName.valueOf(tableNameStr)
    val connection = ConnectionFactory.createConnection(conf)
    val regionLocator = connection.getRegionLocator(tableName)
    val table = connection.getTable(tableName)

    val partitioner = getPartitioner(regionLocator, numFilesPerRegionPerFamily)

    val rdd = mapRdd.flatMap {
      case (k, m) =>
        val keyBytes = wk.write(k)
        m map { case (h, v) => ck((keyBytes, wq.write(h)), v) }
    }

    saveAsHFile(getPartitionedRdd(rdd, kvf(ws.write(family)), partitioner), table, regionLocator, connection)
  }
}
```
> 在HFileSupport.scala中添加代码， 来自适应 RDD[((K, Q, A))]

 ```scala
 // edit HFileSupport.scala
 
 trait HFileSupport {
 // add RDD implicit
  implicit def toHFileRDDTuple[K: Writes, Q: Writes, A: ClassTag](rdd: RDD[(K, Q, A)])(implicit writer: Writes[A]): HFileRDDTuple[K, Q, CellKey, A, A] =
    new HFileRDDTuple[K, Q, CellKey, A, A](rdd, gc[A], kvf[A])
 }
 
 // add class
 final class HFileRDDTuple[K: Writes, Q: Writes, C: ClassTag, A: ClassTag, V: ClassTag](mapRdd: RDD[(K, Q, A)], ck: GetCellKey[C, A, V], kvf: KeyValueWrapperF[C, V]) extends HFileRDDHelper {
   def toHBaseBulk(tableNameStr: String, family: String, numFilesPerRegionPerFamily: Int = 1)
                  (implicit config: HBaseConfig, ord: Ordering[C]): Unit = {
     require(numFilesPerRegionPerFamily > 0)
     val wk = implicitly[Writes[K]]
     val wq = implicitly[Writes[Q]]
     val ws = implicitly[Writes[String]]
 
     val conf = config.get
     val tableName = TableName.valueOf(tableNameStr)
     val connection = ConnectionFactory.createConnection(conf)
     val regionLocator = connection.getRegionLocator(tableName)
     val table = connection.getTable(tableName)
 
     val partitioner = getPartitioner(regionLocator, numFilesPerRegionPerFamily)
 
     val rdd = mapRdd.map {
       case (k, h, v) =>
         ck((wk.write(k), wq.write(h)), v)
     }
 
     saveAsHFile(getPartitionedRdd(rdd, kvf(ws.write(family)), partitioner), table, regionLocator, connection)
   }
 }
```
 
 


