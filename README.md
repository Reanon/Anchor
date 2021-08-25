# Anchor — 开源社区系统

## 📚 项目简介

Anchor 是一套前后端不分离的开源社区系统，基于目前主流 Java Web 技术栈，完成了基本的注册、登录、发帖、评论、点赞、回复功能，使用前缀树实现敏感词过滤，实现了对 UV  和 DAU 统计，并将用户头像等信息存于阿里云 OSS 服务器。

### 🎨 技术选型

![技术选型](https://aliyun-typora-img.oss-cn-beijing.aliyuncs.com/imgs/image-20210724174837196.png)

1、引入 Spring Security 做权限控制，替代拦截器的拦截控制，并使用自己的认证方案替代 Security 认证流程，使权限认证和控制更加方便灵活。

2、利用 Redis 的 set 实现点赞，zset 实现关注，并使用 Redis 存储登录 ticket 和验证码，解决分布式 session 问题。

3、采用 Redis 高级数据类型 HyperLogLog 统计 UV(Unique Visitor)，使用 Bitmap 统计 DAU(Daily Active User)。

4、选取 Kafka 处理发送评论、点赞和关注等系统通知，并使用事件进行封装，构建了强大的异步消息系统。

5、使用 Elasticsearch 做全文搜索，并通过事件封装，增加关键词高亮显示等功能。

6、对热帖排行模块，使用分布式缓存 Redis 和本地缓存 Caffeine 作为多级缓存，避免了缓存雪崩，将 QPS（Queries Per Second） 提升了20倍(10 -> 200)，大大提升了网站访问速度。同时使用 Quartz 定时更新热帖排行。

### 🌌 部署架构

每个都只部署了一台，以下是理想的部署架构：

<img src="https://aliyun-typora-img.oss-cn-beijing.aliyuncs.com/imgs/image-20210724181218502.png" alt="理想架构图" style="zoom:50%;" />

### 🎯 核心技术栈

|                             后端                             |             前端              |
| :----------------------------------------------------------: | :---------------------------: |
| Spring Boot 2.4.8<br/>Spring MVC<br/>ORM：MyBatis<br/>数据库：MySQL 5.7<br/>分布式缓存：Redis<br/>本地缓存：Caffeine<br/>消息队列：Kafka 2.13<br/>搜索引擎：Elasticsearch 6.4.3<br/>安全：Spring Security<br/>邮件任务：Spring Mail<br/>分布式定时任务：Spring Quartz<br/>日志：SLF4J（日志接口） + Logback（日志实现） | Thymeleaf<br/>Jquery<br/>Ajax |

## 🔨 项目部署

### 💻 开发环境

- 操作系统：Windows 10
- 构建工具：Apache Maven
- 集成开发工具：Intellij IDEA
- 应用服务器：Apache Tomcat
- 接口测试工具：Postman
- 压力测试工具：Apache JMeter
- 版本控制工具：Git
- Java 版本：8

### 🎄 功能列表

**注册**

**登录 | 登出**

- 动态生成验证码
- 记住我

**账号设置**

- 修改头像
- 修改密码

**过滤敏感词**

- 前缀树

**帖子模块**

- 发布帖子（过滤敏感词）
- 分页显示所有的帖子
    - 支持按照 「发帖时间」 显示
    - 支持按照 「热度排行」 显示（Spring Quartz）
- 查看帖子详情
- 权限管理（Spring Security + Thymeleaf Security）
    - 未登录用户无法发帖
    - 「版主」 可以看到帖子的置顶和加精按钮并执行相应操作
    - 「管理员」 可以看到帖子的删除按钮并执行相应操作
    - 「普通用户」 无法看到帖子的置顶、加精、删除按钮，也无法执行相应操作

**评论模块**

- 发布对帖子的评论（过滤敏感词）
- 分页显示评论
- 发布对评论的回复（过滤敏感词）
- 权限管理（Spring Security）
    - 未登录用户无法使用评论功能

**私信模块**

- 发送私信（过滤敏感词）
- 私信列表
    - 查询当前用户的会话列表
    - 每个会话只显示一条最新的私信
    - 支持分页显示
- 私信详情
    - 查询某个会话所包含的所有私信
    - 访问私信详情时，将显示的私信设为已读状态
    - 支持分页显示
- 权限管理（Spring Security）
    - 未登录用户无法使用私信功能

**统一处理 404 / 500 异常**

- 普通请求异常
- 异步请求异常

**统一记录日志**

**点赞模块**

- 支持对帖子、评论 / 回复点赞
- 第 1 次点赞，第 2 次取消点赞
- 首页统计帖子的点赞数量
- 详情页统计帖子和评论/回复的点赞数量
- 详情页显示当前登录用户的点赞状态（赞过了则显示已赞）
- 统计我的获赞数量
- 权限管理（Spring Security）
    - 未登录用户无法使用点赞相关功能

**关注模块**

- 关注功能
- 取消关注功能
- 统计用户的关注数和粉丝数
- 我的关注列表（查询某个用户关注的人），支持分页
- 我的粉丝列表（查询某个用户的粉丝），支持分页
- 权限管理（Spring Security）
    - 未登录用户无法使用关注相关功能

**系统通知模块**

- 通知列表
    - 显示评论、点赞、关注三种类型的通知
- 通知详情
    - 分页显示某一类主题所包含的通知
    - 进入某种类型的系统通知详情，则将该页的所有未读的系统通知状态设置为已读
- 未读数量
    - 分别显示每种类型的系统通知的未读数量
    - 显示所有系统通知的未读数量
- 导航栏显示所有消息的未读数量（未读私信 + 未读系统通知）
- 权限管理（Spring Security）
    - 未登录用户无法使用系统通知功能

**搜索模块**

**网站数据统计**（管理员专属）

- 独立访客 UV
    - 支持单日查询和区间日期查询
- 日活跃用户 DAU
    - 支持单日查询和区间日期查询
- 权限管理（Spring Security）
    - 只有管理员可以查看网站数据统计

**优化网站性能**

- 处理每次请求时，都要通过拦截器根据登录凭证查询用户信息，访问的频率非常高。因此将已成功登录的用户信息在缓存 Redis 中保存一段时间，查询用户信息的时候优先从缓存中取值；
    - 若缓存中没有该用户信息，则将其存入缓存；
    - 用户信息变更时清除对应的缓存数据；
- 引入本地缓存 Caffeine，缓存热帖列表和帖子的总数，避免缓存雪崩（这里面还能再加一层二级缓存 Redis）。

### 🎊 界面展示

## 🎡功能逻辑图

> 单向绿色箭头：
>
> - 前端模板 -> Controller：表示这个前端模板中有一个超链接是由这个 Controller 处理的
> - Controller -> 前端模板：表示这个 Controller 会像该前端模板传递数据或者跳转
>
> 双向绿色箭头：表示 Controller 和前端模板之间进行参数的相互传递或使用
>
> 单向蓝色箭头： A -> B，表示 A 方法调用了 B 方法
>
> 单向红色箭头：数据库或缓存操作

### 注册

- 用户注册成功，将用户信息存入 MySQL，但此时该用户状态为未激活
- 向用户发送激活邮件，用户点击链接则激活账号（Spring Mail）

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210204222249.png" />

### 登录 | 登出

登录认证模块跳过了 Spring Secuity 自带的认证机制。主要逻辑如下：

- 进入登录界面，随机生成一个字符串来标识这个将要登录的用户，将这个字符串短暂的存入 Cookie（60 秒）；
- 动态生成验证码，并将验证码及标识该用户的字符串短暂存入 Redis（60 秒）；
- 为登录成功（验证用户名、密码、验证码）的用户随机生成登录凭证且设置状态为有效，并将登录凭证及其状态等信息永久存入 Redis，再在 Cookie 中存一份登录凭证；
- 使用拦截器在所有的请求执行之前，从 Cookie 中获取登录凭证，只要 Redis 中该凭证有效并在有效期内，本次请求就会一直持有该用户信息（使用 ThreadLocal 持有用户信息，保证多台服务器上用户的登录状态同步）；
- 勾选记住我，则延长 Cookie 中登录凭证的有效时间；
- 用户登出，将凭证状态设为无效，并更新 Redis 中该登录凭证的相关信息。

下图是登录模块的功能逻辑图，并没有使用 Spring Security 提供的认证逻辑（我觉得这个模块是最复杂的，这张图其实很多细节还没有画全）

![](https://gitee.com/veal98/images/raw/master/img/20210204233233.png)

### 分页显示所有的帖子

- 支持按照 「发帖时间」显示
- 支持按照 「热度排行」显示（Spring Quartz）
- 将热帖列表和所有帖子的总数存入本地缓存 Caffeine（利用分布式定时任务 Spring Quartz 每隔一段时间就刷新计算帖子的热度/分数 — 见下文，而 Caffeine 里的数据更新不用我们操心，它天生就会自动的更新它拥有的数据，给它一个初始化方法就完事儿）

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210204222822.png" />



### 账号设置

- 修改头像（异步请求）
  - 将用户选择的头像图片文件上传至七牛云服务器
- 修改密码

此处只画出修改头像：

<img width="700px" src="https://gitee.com/veal98/images/raw/master/img/20210206121201.png" />

### 发布帖子（异步请求）

发布帖子（过滤敏感词），将其存入 MySQL

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210206122521.png" />

### 显示评论及相关信息

关于评论模块需要注意的就是评论表的设计，把握其中字段的含义，才能透彻了解这个功能的逻辑。

评论 Comment 的目标类型（帖子，评论） entityType 和 entityId 以及对哪个用户进行评论/回复 targetId 是由前端传递给 DiscussPostController 的

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210207150925.png" />

一个帖子的详情页需要封装的信息大概如下：

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210207151328.png" />

### 添加评论（事务管理）

发布对帖子的评论（过滤敏感词），将其存入 MySQL

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210207122908.png" />

### 私信列表和详情页

<img width="700px" src="https://gitee.com/veal98/images/raw/master/img/20210207161130.png" />

### 发送私信（异步请求）

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210207161500.png" />

### 点赞（异步请求）

将点赞相关信息存入 Redis 的数据结构 set 中。

- 其中，key 命名为  `like:entity:entityType:entityId`，value 即点赞用户的 id。
- 比如 key =  `like:entity:2:246`  value =  `11` 表示用户 11 对实体类型 2 即评论进行了点赞，该评论的 id 是 246

某个用户的获赞数量对应的存储在 Redis 中的 key 是 `like:user:userId`，value 就是这个用户的获赞数量

<img width="700px" src="https://gitee.com/veal98/images/raw/master/img/20210207165837.png"  />

### 我的获赞数量

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210207170003.png" />

### 关注（异步请求）

- 若 A 关注了 B，则 A 是 B 的粉丝 Follower，B 是 A 的目标 Followee
- 关注的目标可以是用户、帖子、题目等，在实现时将这些目标抽象为实体（目前只做了关注用户）

将某个用户关注的实体相关信息存储在 Redis 的数据结构 zset 中：key 是 `followee:userId:entityType` ，对应的 value 是 `zset(entityId, now)` ，以关注的时间进行排序。比如说 `followee:111:3` 对应的value `(20, 2020-02-03-xxxx)`，表明用户 111 关注了一个类型为 3 的实体即人(用户)，关注的这个实体 id 是 20，关注该实体的时间是 2020-02-03-xxxx

同样的，将某个实体拥有的粉丝相关信息也存储在 Redis 的数据结构 zset 中：key 是 `follower:entityType:entityId`，对应的 value 是 `zset(userId, now)`，以关注的时间进行排序

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210207174046.png" />

### 关注列表

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210207175621.png" />

### 发送系统通知

![](https://gitee.com/veal98/images/raw/master/img/20210207182917.png)

### 显示系统通知

![](https://gitee.com/veal98/images/raw/master/img/20210208153059.png)

### 搜索

- 发布事件
  - 发布帖子时，通过消息队列将帖子异步地提交到 Elasticsearch 服务器
  - 为帖子增加评论时，通过消息队列将帖子异步地提交到 Elasticsearch 服务器
- 搜索服务
  - 从 Elasticsearch 服务器搜索帖子
  - 从 Elasticsearch 服务器删除帖子（当帖子从数据库中被删除时）
- 显示搜索结果

![](https://gitee.com/veal98/images/raw/master/img/20210208161936.png)

类似的，置顶、加精也会触发发帖事件，就不再图里面画出来了。

### 置顶加精删除（异步请求）

<img width="660px" src="https://gitee.com/veal98/images/raw/master/img/20210208171729.png" />

### 网站数据统计

- 独立访客 UV
  - 存入 Redis 的 HyperLogLog
  - 支持单日查询和区间日期查询
- 日活跃用户 DAU
  - 存入 Redis 的 Bitmap
  - 支持单日查询和区间日期查询
- 权限管理（Spring Security）
  - 只有管理员可以查看网站数据统计

![](https://gitee.com/veal98/images/raw/master/img/20210208170801.png)

### 帖子热度计算

每次发生点赞（给帖子点赞）、评论（给帖子评论）、加精的时候，就将这些帖子信息存入缓存 Redis 中，然后通过分布式的定时任务 Spring Quartz，每隔一段时间就从缓存中取出这些帖子进行计算分数。

帖子分数/热度计算公式：分数（热度） = 权重 + 发帖距离天数

```java
// 计算权重
double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
// 分数 = 权重 + 发帖距离天数
double score = Math.log10(Math.max(w, 1))
    + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
```

![](https://gitee.com/veal98/images/raw/master/img/20210208173636.png)

## 
