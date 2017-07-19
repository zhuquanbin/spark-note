# 命令行解析

## 样例
> Reference [stackoverflow: best-way-to-parse-command-line-parameters](https://stackoverflow.com/questions/2315912/best-way-to-parse-command-line-parameters)
```scala
import scala.collection.mutable

/**
  * Created by Borey.Zhu on 2017/7/11.
  */
object ParseCommandLine {

  val usage:String =
    "Usage:SnifferAnalyse \n" +
      "   --hbase.rootdir           [path]    *\n" +
      "   --hbase.zookeeper.quorum  [address] *\n" +
      "   --save.hadoop             [address] Data will be only write to hadoop when setting this flag\n" +
      "   --bulk.regions            [number]  Default: 1\n" +
      "   --mapreduce.split.maxsize [size]    Default: 32*1024*1024\n " +
      "   sniffer_logs_path"

  type OptionMap = mutable.HashMap[String, String]
  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    def isSwitch(s: String) = s(0) == '-'

    list match {
      case Nil =>
        map

      case "--hbase.rootdir" :: value :: tail =>
        nextOption(map += ("hbase.rootdir" -> value), tail)

      case "--hbase.zookeeper.quorum" :: value :: tail =>
        nextOption(map += ("hbase.zookeeper.quorum" -> value), tail)

      case "--bulk.regions" :: value :: tail =>
        nextOption(map += ("bulk.regions" -> value), tail)

      case "--save.hadoop" :: value :: tail =>
        nextOption(map += ("save.hadoop" -> value), tail)

      case "--mapreduce.split.maxsize" :: value :: tail =>
        nextOption(map += ("mapreduce.split.maxsize" -> value), tail)

      case opt1 :: opt2 :: tail  =>
        if (!isSwitch(opt1) && !isSwitch(opt2)){
          val logs = map.getOrElse("logs", null)
          if (null == logs){
            nextOption(map += ("logs" -> opt1), list.tail)
          }else{
            nextOption(map += ("logs" -> (logs + "," + opt1)), list.tail)
          }

        }else{
          println(usage)
          sys.exit(1)
        }

      case string :: Nil =>
        val logs = map.getOrElse("logs", null)
        if (null == logs){
          nextOption(map += ("logs" -> string), list.tail)
        }else{
          nextOption(map += ("logs" -> (logs + "," + string)), list.tail)
        }

      case option :: tail =>
        println(usage)
        sys.exit(1)
    }
  }

  def getOptions(args: Array[String]): mutable.HashMap[String,String] = {
    if (args.length == 0) {
      println(usage)
      sys.exit(1)
    }
    val options = nextOption(mutable.HashMap[String,String](), args.toList)

    if (!options.contains("hbase.rootdir") ||
        !options.contains("hbase.zookeeper.quorum") ||
        !options.contains("logs")){
      println(usage)
      sys.exit(1)
    }
    options
  }

  def getOptionValue(x:Option[String]):String = x match {
      case Some(v) => v
      case None => new String()
    }

  def main(args: Array[String]): Unit = {
    println(getOptions(args))
  }
}
```