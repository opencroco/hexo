---
title: WebMagic Xsoup 和 自定义Pipeline
categories:
  - webmagic
date: 2018-01-31 22:56:45
---
WebMagic的抽取主要用到了Jsoup和官方自带的工具Xsoup。  

Jsoup是一个简单的HTML解析器，同时它支持使用CSS选择器的方式查找元素。关于Jsoup的学习文章，大家可以到这里进行学习！https://github.com/code4craft/jsoup-learning

## Xsoup
Xsoup是WebMagic 作者基于Jsoup开发的一款XPath解析器。

旧版本的 WebMagic 使用的解析器是 HtmlCleaner。再使用过程存在一些问题。主要问题是XPath出错定位不准确，并且其不太合理的代码结构，也难以进行定制。最终作者自己开发了 Xsoup 来取代 HtmlCleaner，使得更加符合爬虫开发的需要。经过测试，Xsoup的性能比HtmlCleaner要快一倍以上。

Xsoup发展到现在，已经支持爬虫常用的语法，以下是一些已支持的语法对照表：
![](http://img.bbs.csdn.net/upload/201704/12/1491987070_762543.png)

扩展的XPath方法用法：  
<table>
	<thead>
		<tr>
			<th>
				Expression
			</th>
			<th>
				Description
			</th>
			<th>
				XPath1.0
			</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>
				text(n)
			</td>
			<td>
				第n个直接文本子节点，为0表示所有
			</td>
			<td>
				text() only
			</td>
		</tr>
		<tr>
			<td>
				allText()
			</td>
			<td>
				所有的直接和间接文本子节点
			</td>
			<td>
				not support
			</td>
		</tr>
		<tr>
			<td>
				tidyText()
			</td>
			<td>
				所有的直接和间接文本子节点，并将一些标签替换为换行，使纯文本显示更整洁
			</td>
			<td>
				not support
			</td>
		</tr>
		<tr>
			<td>
				html()
			</td>
			<td>
				内部html，不包括标签的html本身
			</td>
			<td>
				not support
			</td>
		</tr>
		<tr>
			<td>
				outerHtml()
			</td>
			<td>
				内部html，包括标签的html本身
			</td>
			<td>
				not support
			</td>
		</tr>
		<tr>
			<td>
				regex(@attr,expr,group)
			</td>
			<td>
				这里@attr和group均可选，默认是group0
			</td>
			<td>
				not support
			</td>
		</tr>
	</tbody>
</table>

## 定制Pipeline
Pileline是抽取结束后，进行处理的部分，它主要用于抽取结果的保存，也可以定制Pileline可以实现一些通用的功能。

Pipeline的接口定义如下：
```
public interface Pipeline {
    // ResultItems保存了抽取结果，它是一个Map结构，
    // 在page.putField(key,value)中保存的数据，可以通过ResultItems.get(key)获取
    public void process(ResultItems resultItems, Task task);
}
```
Pipeline其实就是将PageProcessor抽取的结果，继续进行了处理的，其实在Pipeline中完成的功能，你基本上也可以直接在PageProcessor实现，那么为什么会有Pipeline？有以下两个原因：  

- 为了模块分离。“页面抽取”和“后处理、持久化”是爬虫的两个阶段，将其分离开来，一个是代码结构比较清晰，另一个是以后也可能将其处理过程分开，分开在独立的线程以至于不同的机器执行。
- Pipeline的功能比较固定，更容易做成通用组件。每个页面的抽取方式千变万化，但是后续处理方式则比较固定，例如保存到文件、保存到数据库这种操作，这些对所有页面都是通用的。WebMagic中就已经提供了控制台输出、保存到文件、保存为JSON格式的文件几种通用的Pipeline。

在WebMagic里，一个Spider可以有多个Pipeline，使用Spider.addPipeline()即可增加一个Pipeline。这些Pipeline都会得到处理。  
```
spider.addPipeline(new ConsolePipeline()).addPipeline(new FilePipeline())
```
实现输出结果到控制台，并且保存到文件的目标。

在介绍PageProcessor时，我们使用了GithubRepoPageProcessor作为例子，其中某一段代码中，我们将结果进行了保存：
```
public void process(Page page) {
    page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all());
    page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+)").all());
    //保存结果author，这个结果会最终保存到ResultItems中
    page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString());
    page.putField("name", page.getHtml().xpath("//h1[@class='entry-title public']/strong/a/text()").toString());
    if (page.getResultItems().get("name")==null){
        //设置skip之后，这个页面的结果不会被Pipeline处理
        page.setSkip(true);
    }
    page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));
}
```
现在我们想将结果保存到控制台，要怎么做呢？ConsolePipeline可以完成这个工作
```
public class ConsolePipeline implements Pipeline {
    @Override
    public void process(ResultItems resultItems, Task task) {
        System.out.println("get page: " + resultItems.getRequest().getUrl());
        //遍历所有结果，输出到控制台，上面例子中的"author"、"name"、"readme"都是一个key，其结果则是对应的value
        for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
            System.out.println(entry.getKey() + ":\t" + entry.getValue());
        }
    }
}
```
## 将结果保存到MySQL
在Java里，我们有很多方式将数据保存到MySQL，例如jdbc、dbutils、spring-jdbc、MyBatis等工具。这些工具都可以完成同样的事情，只不过功能和使用复杂程度不一样。如果使用jdbc，那么我们只需要从ResultItems取出数据，进行保存即可。

如果我们会使用ORM框架来完成持久化到MySQL的工作，就会面临一个问题：这些框架一般都要求保存的内容是一个定义好结构的对象，而不是一个key-value形式的ResultItems。以MyBatis为例，我们使用MyBatis-Spring可以定义这样一个DAO：
```
public interface JobInfoDAO {
    @Insert("insert into JobInfo (`title`,`salary`,`company`,`description`,`requirement`,`source`,`url`,`urlMd5`) values (#{title},#{salary},#{company},#{description},#{requirement},#{source},#{url},#{urlMd5})")
    public int add(LieTouJobInfo jobInfo);
}
```
## 注解模式
注解模式下，WebMagic内置了一个PageModelPipeline：
```
public interface PageModelPipeline<T> {
    //这里传入的是处理好的对象
    public void process(T t, Task task);
}
```
这时，我们可以很优雅的定义一个JobInfoDaoPipeline，来实现这个功能：
```
@Component("JobInfoDaoPipeline")
public class JobInfoDaoPipeline implements PageModelPipeline<LieTouJobInfo> {
    @Resource
    private JobInfoDAO jobInfoDAO;
    @Override
    public void process(LieTouJobInfo lieTouJobInfo, Task task) {
        //调用MyBatis DAO保存结果
        jobInfoDAO.add(lieTouJobInfo);
    }
}
```
## 基本Pipeline模式
至此，结果保存就已经完成了！那么如果我们使用原始的Pipeline接口，要怎么完成呢？其实答案也很简单，如果你要保存一个对象，那么就需要在抽取的时候，将它保存为一个对象：  
```
public void process(Page page) {
    page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all());
    page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+)").all());
    GithubRepo githubRepo = new GithubRepo();
    githubRepo.setAuthor(page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString());
    githubRepo.setName(page.getHtml().xpath("//h1[@class='entry-title public']/strong/a/text()").toString());
    githubRepo.setReadme(page.getHtml().xpath("//div[@id='readme']/tidyText()").toString());
    if (githubRepo.getName() == null) {
        //skip this page
        page.setSkip(true);
    } else {
        page.putField("repo", githubRepo);
    }
}
```
在Pipeline中，只要使用
```
GithubRepo githubRepo = (GithubRepo)resultItems.get("repo");
```
## WebMagic 自带的Pipeline
WebMagic中已经提供了将结果输出到控制台、保存到文件和JSON格式保存的几个Pipeline：
<table>
	<thead>
		<tr>
			<th>
				类
			</th>
			<th>
				说明
			</th>
			<th>
				备注
			</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>
				ConsolePipeline
			</td>
			<td>
				输出结果到控制台
			</td>
			<td>
				抽取结果需要实现toString方法
			</td>
		</tr>
		<tr>
			<td>
				FilePipeline
			</td>
			<td>
				保存结果到文件
			</td>
			<td>
				抽取结果需要实现toString方法
			</td>
		</tr>
		<tr>
			<td>
				JsonFilePipeline
			</td>
			<td>
				JSON格式保存结果到文件
			</td>
			<td>
				&nbsp;
			</td>
		</tr>
		<tr>
			<td>
				ConsolePageModelPipeline
			</td>
			<td>
				(注解模式)输出结果到控制台
			</td>
			<td>
				&nbsp;
			</td>
		</tr>
		<tr>
			<td>
				FilePageModelPipeline
			</td>
			<td>
				(注解模式)保存结果到文件
			</td>
			<td>
				&nbsp;
			</td>
		</tr>
		<tr>
			<td>
				JsonFilePageModelPipeline
			</td>
			<td>
				(注解模式)JSON格式保存结果到文件
			</td>
			<td>
				想要持久化的字段需要有getter方法
			</td>
		</tr>
	</tbody>
</table>