# FancyNews

2022 Java小学期 Android大作业 ：新闻APP

## Feature

- 从[给定的API](https://api2.newsminer.net/svc/news/queryNewsList?size=15&startDate=2021-08-20&endDate=2021-08-30&words=拜登&categories=科技)中获取新闻，并分类以列表形式展示，阅读后变灰。

- 分类选项卡可以添加/删除。

- 支持历史记录、收藏功能，且能本地离线浏览（视频除外），支持清除本地缓存。

- API中的新闻可根据关键词、发布时间和类别搜索。

- 从预设或自定义的**RSS源**中获取新闻。

## Detailed Description

[BLOG](https://adu2021.github.io/undefined/2022javaDebug/)

## Note

API provided by [Tsinghua University KEG](https://newsminer.net/)

Developed with Android Studio

Libraries Used:

- Android Jetpack (Navigation, ROOM, ...)

- RxJava3

- Gson

- Picasso

- (Glide)

- [SmartRefreshLayout](https://github.com/scwang90/SmartRefreshLayout)

- maybe more...