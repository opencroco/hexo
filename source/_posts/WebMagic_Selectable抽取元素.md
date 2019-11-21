---
title: WebMagic_Selectable抽取元素
categories:
  - webmagic
date: 2018-01-31 23:52:45
---
## webmagic 获取网页内容
Selectable相关的抽取元素链式API是WebMagic的一个核心功能。使用Selectable接口，你可以直接完成页面元素的链式抽取，也无需去关心抽取的细节。

在WebMagic Xsoup 和 自定义Pipeline这篇文章的例子中可以看到，page.getHtml()返回的是一个Html对象，它实现了Selectable接口。这个接口包含一些重要的方法，分为两类：抽取部分和获取结果部分。

抽取部分的API

<div><table><thead><tr><th>
				方法
			</th>
			<th>
				说明
			</th>
			<th>
				示例
			</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>
				xpath(String xpath)
			</td>
			<td>
				使用XPath选择
			</td>
			<td>
				html.xpath("//div[@class=’title’]")
			</td>
		</tr>
		<tr>
			<td>
				$(String selector)
			</td>
			<td>
				使用Css选择器选择
			</td>
			<td>
				html.$("div.title")
			</td>
		</tr>
		<tr>
			<td>
				$(String selector,String attr)
			</td>
			<td>
				使用Css选择器选择
			</td>
			<td>
				html.$("div.title","text")
			</td>
		</tr>
		<tr>
			<td>
				css(String selector)
			</td>
			<td>
				功能同$()，使用Css选择器选择
			</td>
			<td>
				html.css("div.title")
			</td>
		</tr>
		<tr>
			<td>
				links()
			</td>
			<td>
				选择所有链接
			</td>
			<td>
				html.links()
			</td>
		</tr>
		<tr>
			<td>
				regex(String regex)
			</td>
			<td>
				使用正则表达式抽取
			</td>
			<td>
				html.regex("\(.\*?)\")
			</td>
		</tr>
		<tr>
			<td>
				regex(String regex,int group)
			</td>
			<td>
				使用正则表达式抽取，并指定捕获组
			</td>
			<td>
				html.regex("\(.\*?)\",1)
			</td>
		</tr>
		<tr>
			<td>
				replace(String regex, String replacement)
			</td>
			<td>
				替换内容
			</td>
			<td>
				html.replace("\","")
			</td>
		</tr>
	</tbody>
</table>  
</div>

这部分抽取API返回的都是一个Selectable接口，意思是说，抽取是支持链式调用的。

例如，我现在要抓取github上所有的Java项目，这些项目可以在https://github.com/search?l=Java&p=1&q=stars%3A%3E1&s=stars&type=Repositories搜索结果中看到。
为了避免抓取范围太宽，我指定只从分页部分抓取链接。这个抓取规则是比较复杂的，要怎么写呢？
![](http://webmagic.qiniudn.com/oscimages/151454_2T01_190591.png)

首先看到页面的html结构是这个样子的：
![](http://webmagic.qiniudn.com/oscimages/151632_88Oq_190591.png)

那么可以先用CSS选择器提取出这个div，然后在取到所有的链接。为了保险起见，我再使用正则表达式限定一下提取出的URL的格式，那么最终的写法是这样子的：

```
List<String> urls = page.getHtml().css("div.pagination").links().regex(".*/search/\?l=java.*").all();
page.addTargetRequests(urls);
```

## 获取结果的API

当链式调用结束时，我们一般都想要拿到一个字符串类型的结果。这时候就需要用到获取结果的API了。我们知道，一条抽取规则，无论是XPath、CSS选择器或者正则表达式，总有可能抽取到多条元素。WebMagic对这些进行了统一，你可以通过不同的API获取到一个或者多个元素。

<table>
	<thead>
		<tr>
			<th>
				方法
			</th>
			<th>
				说明
			</th>
			<th>
				示例
			</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>
				get()
			</td>
			<td>
				返回一条String类型的结果
			</td>
			<td>
				String link= html.links().get()
			</td>
		</tr>
		<tr>
			<td>
				toString()
			</td>
			<td>
				功能同get()，返回一条String类型的结果
			</td>
			<td>
				String link= html.links().toString()
			</td>
		</tr>
		<tr>
			<td>
				all()
			</td>
			<td>
				返回所有抽取结果
			</td>
			<td>
				List&nbsp;links= html.links().all()
			</td>
		</tr>
		<tr>
			<td>
				match()
			</td>
			<td>
				是否有匹配结果
			</td>
			<td>
				if (html.links().match()){ xxx; }
			</td>
		</tr>
	</tbody>
</table>

我们知道页面只会有一条结果，那么可以使用selectable.get()或者selectable.toString()拿到这条结果。

这里selectable.toString()采用了toString()这个接口，是为了在输出以及和一些框架结合的时候，更加方便。因为一般情况下，我们都只需要选择一个元素！

selectable.all()则会获取到所有元素。

好了，到现在为止，在回过头看看WebMagic Xsoup 和 自定义Pipeline中的GithubRepoPageProcessor，可能就觉得更加清晰了吧？指定main方法，已经可以看到抓取结果在控制台输出了。