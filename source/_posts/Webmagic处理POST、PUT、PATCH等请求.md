---
title: Webmagic处理POST、PUT、PATCH等请求
categories:
  - webmagic
date: 2018-2-01 00:15:23
---
了解 RESTful 的都知道，POST不是幂等操作，所以理论上每次请求的结果都未必一样，所以原则上是没有办法做“去重”操作的。其次，除了GET请求，其他Method都没有办法自动发现，一般都是手工构建出来的，使用者更应该自己去控制是否重复。