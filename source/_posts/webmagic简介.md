---
title: webmagic简介
categories:
- webmagic
date: 2018-1-31 19:47:00
---

# webmagic介绍
## 简单架构
![](https://o364p1r5a.qnssl.com/blog/14642364175906.jpg)
WebMagic项目代码分为核心和扩展两部分。核心部分(webmagic-core)是一个精简的、模块化的爬虫实现，而扩展部分则包括一些便利的、实用性的功能。WebMagic的架构设计参照了Scrapy，目标是尽量的模块化，并体现爬虫的功能特点。

扩展部分(webmagic-extension)提供一些便捷的功能，例如注解模式编写爬虫等。同时内置了一些常用的组件，便于爬虫开发
## 设计思想
WebMagic由四个组件(Downloader、PageProcessor、Scheduler、Pipeline)构成，核心代码非常简单，主要是将这些组件结合并完成多线程的任务。在WebMagic中，你基本上可以对爬虫的功能做任何定制。

WebMagic的核心在webmagic-core包中，其他的包你可以理解为对WebMagic的一个扩展——这和作为用户编写一个扩展是没有什么区别的。

虽然WebMagic的核心足够简单，但是WebMagic也以扩展的方式，实现了很多可以帮助开发的便捷功能。例如基于注解模式的爬虫开发，以及扩展了XPath语法的Xsoup等。这些功能在WebMagic中是可选的，它们的开发目标，就是让使用者开发爬虫尽可能的简单，尽可能的易维护。
## 四大组件
### Downloader
Downloader负责从互联网上下载页面，以便后续处理。WebMagic默认使用了Apache HttpClient作为下载工具。
### PageProcessor
爬虫逻辑，程序的核心  
PageProcessor负责解析页面，抽取有用信息，以及发现新的链接。WebMagic使用Jsoup作为HTML解析工具，并基于其开发了解析XPath的工具Xsoup。
在这四个组件中，PageProcessor对于每个站点每个页面都不一样，是需要使用者定制的部分。
### Scheduler
Scheduler负责管理待抓取的URL，以及一些去重的工作。WebMagic默认提供了JDK的内存队列来管理URL，并用集合来进行去重。也支持使用Redis进行分布式管理。
除非项目有一些特殊的分布式需求，否则无需自己定制Scheduler。
### Pipeline
Pipeline负责抽取结果的处理，包括计算、持久化到文件、数据库等。WebMagic默认提供了“输出到控制台”和“保存到文件”两种结果处理方案。
Pipeline定义了结果保存的方式，如果你要保存到指定数据库，则需要编写对应的Pipeline。对于一类需求一般只需编写一个Pipeline。
## 用于数据流转的对象
WebMagic 主要有3个数据流转对象。分别是Request、Page、ResultItems。  
### Request  
Request是对URL地址的一层封装，一个Request对应一个URL地址。
它是PageProcessor与Downloader交互的载体，也是PageProcessor控制Downloader唯一方式。
除了URL本身外，它还包含一个Key-Value结构的字段extra。你可以在extra中保存一些特殊的属性，然后在其他地方读取，以完成不同的功能。例如附加上一个页面的一些信息等。

### page
Page代表了从Downloader下载到的一个页面——可能是HTML，也可能是JSON或者其他文本格式的内容。
Page是WebMagic抽取过程的核心对象，它提供一些方法可供抽取、结果保存等。在第四章的例子中，我们会详细介绍它的使用。
### ResultItems
ResultItems相当于一个Map，它保存PageProcessor处理的结果，供Pipeline使用。它的API与Map很类似，值得注意的是它有一个字段skip，若设置为true，则不应被Pipeline处理。
### Spider
Spider是WebMagic内部流程的核心。Downloader、PageProcessor、Scheduler、Pipeline都是Spider的一个属性，这些属性是可以自由设置的，通过设置这个属性可以实现不同的功能。Spider也是WebMagic操作的入口，它封装了爬虫的创建、启动、停止、多线程等功能。下面是一个设置各个组件，并且设置多线程和启动的例子。
```
public static void main(String[] args) {
    Spider.create(new GithubRepoPageProcessor())
            //从www.xttblog.com开始抓   
            .addUrl("https://www.xttblog.com")
            //设置Scheduler，使用Redis来管理URL队列
            .setScheduler(new RedisScheduler("localhost"))
            //设置Pipeline，将结果以json方式保存到文件
            .addPipeline(new JsonFilePipeline("D:\\data\\webmagic"))
            //开启5个线程同时执行
            .thread(5)
            //启动爬虫
            .run();
}
```
# 构建项目

## 引入依赖
```
<dependency>  
    <groupId>us.codecraft</groupId>  
    <artifactId>webmagic-core</artifactId>  
    <version>0.7.3</version>  
</dependency>  
<dependency>  
    <groupId>us.codecraft</groupId>  
    <artifactId>webmagic-extension</artifactId>  
    <version>0.7.3</version>  
</dependency>
```
## 定制页面解析代码
四大组件的说明前面讲过了，对于一般的爬虫来讲，只要定制下PageProcessor就行了
```
import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * CSDN博客爬虫
 * 
 * @describe 可以爬取指定用户的csdn博客所有文章，并保存到数据库中。
 * @date 2016-4-30
 * 
 * @author steven
 * @csdn qq598535550
 * @website lyf.soecode.com
 */
public class CsdnBlogPageProcessor implements PageProcessor {

	private static String username = "qq598535550";// 设置csdn用户名
	private static int size = 0;// 共抓取到的文章数量

	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {
		// 列表页
		if (!page.getUrl().regex("http://blog\\.csdn\\.net/" + username + "/article/details/\\d+").match()) {
			// 添加所有文章页
			page.addTargetRequests(page.getHtml().xpath("//div[@id='article_list']").links()// 限定文章列表获取区域
					.regex("/" + username + "/article/details/\\d+")
					.replace("/" + username + "/", "http://blog.csdn.net/" + username + "/")// 巧用替换给把相对url转换成绝对url
					.all());
			// 添加其他列表页
			page.addTargetRequests(page.getHtml().xpath("//div[@id='papelist']").links()// 限定其他列表页获取区域
					.regex("/" + username + "/article/list/\\d+")
					.replace("/" + username + "/", "http://blog.csdn.net/" + username + "/")// 巧用替换给把相对url转换成绝对url
					.all());
			// 文章页
		} else {
			size++;// 文章数量加1
			// 用CsdnBlog类来存抓取到的数据，方便存入数据库
			CsdnBlog csdnBlog = new CsdnBlog();
			// 设置标签（可以有多个，用,来分割）
			csdnBlog.setTags(listToString(page.getHtml()
					.xpath("//div[@class='article_l']/span[@class='link_categories']/a/allText()").all()));
			// 设置类别（可以有多个，用,来分割）
					page.getHtml().xpath("//div[@class='article_title']//span[@class='link_title']/a/text()").get());
			// 设置日期
			csdnBlog.setDate(
					page.getHtml().xpath("//div[@class='article_r']/span[@class='link_postdate']/text()").get());
			// 设置标签组（标签可以有多个，这里用,来分割）
			csdnBlog.setTags(listToString(page.getHtml()
					.xpath("//div[@class='article_l']/span[@class='link_categories']/a/allText()").all()));
			// 设置类别
			csdnBlog.setCategory(
					listToString(page.getHtml().xpath("//div[@class='category_r']/label/span/text()").all()));
			// 设置阅读人数
			csdnBlog.setView(Integer.parseInt(page.getHtml().xpath("//div[@class='article_r']/span[@class='link_view']")
					.regex("(\\d+)人阅读").get()));
			// 设置评论人数
			csdnBlog.setComments(Integer.parseInt(page.getHtml()
					.xpath("//div[@class='article_r']/span[@class='link_comments']").regex("\\((\\d+)\\)").get()));
			// 设置是否原创
			csdnBlog.setCopyright(page.getHtml().regex("bog_copyright").match() ? 1 : 0);
			// 把对象存入数据库
			new CsdnBlogDao().add(csdnBlog);
			// 把对象输出控制台
			System.out.println(csdnBlog);
		}
	}

	// 把list转换为string，用,分割
	public static String listToString(List<String> stringList) {
		if (stringList == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		boolean flag = false;
		for (String string : stringList) {
			if (flag) {
				result.append(",");
			} else {
				flag = true;
			}
			result.append(string);
		}
		return result.toString();
	}

	public static void main(String[] args) {
		long startTime, endTime;
		System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
		startTime = System.currentTimeMillis();
		// 从用户博客首页开始抓，开启5个线程，启动爬虫
		Spider.create(new CsdnBlogPageProcessor()).addUrl("http://blog.csdn.net/" + username).thread(5).run();
		endTime = System.currentTimeMillis();
		System.out.println("【爬虫结束】共抓取" + size + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒，已保存到数据库，请查收！");
	}
}
```
