---
title: webmagic入门
categories:
  - webmagic
date: 2018-01-31 20:26:30
---
WebMagic 主要包含两个jar包：webmagic-core-{version}.jar和webmagic-extension-{version}.jar。在项目中添加这两个包的依赖，即可使用WebMagic。WebMagic默认使用Maven管理依赖。

## 使用 Maven 创建 WebMagic 爬虫项目
我们先使用 Eclipse 创建一个 maven 项目。然后在pom.xml 中配置 WebMagic 的依赖。配置信息如下：
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
不使用maven的用户，可以去http://webmagic.io中下载最新的jar包。
## 定制PageProcessor 
在项目中添加了WebMagic的依赖之后，即可开始第一个爬虫的开发了！我们这里拿一个抓取Github信息的例子：
```
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
public class GithubRepoPageProcessor implements PageProcessor {
	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
    @Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {
		// 添加所有文章页
        page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all());
		// 在page.putField(key,value)中保存的数据，可以通过ResultItems.get(key)获取
        page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString());
        page.putField("name", page.getHtml().xpath("//h1[@class='entry-title public']/strong/a/text()").toString());
        if (page.getResultItems().get("name")==null){
            //skip this page
            page.setSkip(true);
        }
        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));
    }
    @Override
    public Site getSite() {
        return site;
    }
    public static void main(String[] args) {
		//启动爬虫
        Spider.create(new GithubRepoPageProcessor()).addUrl("https://github.com/eyutongling").thread(5).run();
    }
}
```
## webmagic配置
我们发现爬取网页内容都是通过实现 PageProcessor 来抓取的。PageProcessor 是 Webmagic 4个重要组件之一。PageProcessor 的主要作用是：负责解析页面，抽取有用信息，以及发现新的链接。  
  
PageProcessor 接口主要有两个方法。  
void process(Page page)：负责处理页面，提取URL获取，提取数据和存储  
Site getSite()：获取站点相关设置信息
关于爬虫的配置，包括编码、抓取间隔、超时时间、重试次数等，也包括一些模拟的参数，例如User Agent、cookie，以及代理的设置使用见下面内容！
### Spider
Spider是爬虫启动的入口。在启动爬虫之前，我们需要使用一个PageProcessor创建一个Spider对象，然后使用run()进行启动。同时Spider的其他组件（Downloader、Scheduler、Pipeline）都可以通过set方法来进行设置。  
![](http://img.bbs.csdn.net/upload/201704/12/1491980534_142624.png)
### Site
对站点本身的一些配置信息，例如编码、HTTP头、超时时间、重试策略等、代理等，都可以通过设置Site对象来进行配置
![](http://img.bbs.csdn.net/upload/201704/12/1491980736_438116.png)