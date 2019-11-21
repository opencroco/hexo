---
title: mapper_reducer第一个应用
categories:
- hadoop
date: 2018-1-27 22:47:00
---
# 需求分析
统计输入文件中每个单词出现的次数。基本解决思路就是将文本内容切分成单词，将其中相同的单词聚集在一起，统计其数量作为该单词的出现次数输出。
# 运行流程
- 输入文本内容：  
  Hello Hadoop  
  Bye Hadoop

- 输出结果内容：  
  Bye 1  
  Hadoop 2  
  Hello 1  
  ![jkkkkkk](https://eyutongling.github.io/img/hadoop/mapper.PNG)
# 下载jar包  
  * 我们的要在eclipse上写代码，防止发生报错，功能比较简单，只需要导入下面jar包  
  hadoop-common-2.7.1.jar  
  hadoop-mapreduce-client-core-2.7.1.jar  
# 创建java工程  
1. 打开eclipse，新建Java Project

2. 创建好项目后，新建lib文件夹，把jar包拷进来，添加到buildpath  

3. 新建class doMapper  
```
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
/*
 * 继承Mapper类，并重写map方法
 * 对读取的数据进行处理
 * 输出<key,value>
 */
public class doMapper extends Mapper<LongWritable, Text, Text, IntWritable>  {
	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context)  throws IOException, InterruptedException {
			
		
		String line =value.toString();
		String [] words=line.split(" ");//按空格切割
		
		for (String word : words) {
			//map输出的内容写入到context
			context.write(new Text(word), new IntWritable(1));
		}
	}
}
```
4. 新建class doReduce
```
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
/*
 * 继承Reducer，并重写reduce
 * 读取map输出的<key,valueList>,并计算value的和
 * 输出<key,value>
 */
public class doReduce extends Reducer<Text, IntWritable, Text, IntWritable> {
	@Override
	protected void reduce(Text key, Iterable<IntWritable> values,
Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
		//计算value
		Integer count=0;	
		for (IntWritable value : values) {
			count+=value.get();	
		}
		//输出的内容写入到context
		context.write(key,new IntWritable(count));
	}
}

```
5. 新建class Main 
```
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
/*
 * 配置hadoop的运行参数，程序的执行入口
 */
public class Main {
	public static void main(String[] args) throws Exception {
		
		//创建配置对象
		Configuration conf=new Configuration();
		
		//创建job
		Job job=Job.getInstance(conf, "wordcont");
		
		//设置运行job的类
		job.setJarByClass(Main.class);
		
		//设置mapper类
		job.setMapperClass(doMapper.class);
		
		//设置reduce类
		job.setReducerClass(doReduce.class);
		
		//设置map 输出的key value
		job.setMapOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		//设置reduce设置的key value
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		//设置输入输出的路径（根据自己地址设置）
		FileInputFormat.setInputPaths(job, new Path("hdfs://192.168.192.128:9000/words"));
		FileOutputFormat.setOutputPath(job, new Path("hdfs://192.168.192.128:9000/output"));
		
		//提交job到hadoop，
		boolean b= job.waitForCompletion(true);
		
		if (!b) {
			System.out.println("this task has failed!!!");
		}
	}
}

```
6. 右键项目 ->Export导出文件–>JAR file –> 设置导出目录—>设置Main为程序入口
# 启动hdfs 和yarn
1. 使用xshell或其他工具把WordCount.jar上传到Linux服务器
2. 新建文件words 并上传到hdfs
- vi words   
	Hello Hadoop  
	Bye Hadoop  
- hadoop fs –put words /
3. 运行WordCount.jar
- yarn jar /home/hadoop/jars/WordCount.jar
4. 查看输出文件  
- hadoop fs -cat /output/part-r-00000


