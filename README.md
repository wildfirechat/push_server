# 野火IM 推送服务
作为野火IM的推送服务的演示，支持小米、华为、魅族和苹果apns。

#### 编译
```
mvn package
```

#### 修改配置
本演示服务有5个配置文件在工程的```config```目录下，分别是```application.properties```, ```apns.properties```, ```meizu.properties```, ```hms.properties```和```xiaomi.properties``` 。
分别配置服务的端口和相关推送的配置。
请正确配置放到jar包所在的目录下的```config```目录下。

#### 运行
在```target```目录找到```push-XXXX.jar```，把jar包和放置配置文件的```config```目录放到一起，然后执行下面命令：
```
java -jar push-XXXXX.jar
```

#### 使用到的开源代码
1. [TypeBuilder](https://github.com/ikidou/TypeBuilder) 一个用于生成泛型的简易Builder

#### LICENSE
UNDER MIT LICENSE. 详情见LICENSE文件
