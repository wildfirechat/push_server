# 野火IM 推送服务
作为野火IM的推送服务的演示，支持小米、华为、魅族和苹果apns。

#### 编译
```
mvn package
```

#### 修改配置
本演示服务有5个配置文件，分别是```application.properties```, ```apns.properties```, ```meizu.properties```, ```hms.properties```和```xiaomi.properties``` 。
分别配置服务的端口和相关推送的配置。
请直接在工程的resource目录下修改打包进工程。或者放到jar包所在的目录下的```config```目录下。

#### 运行
```
nohup java -jar push-XXXXX.jar > push.log 2>&1 &
```

#### 鸣谢
1. [TypeBuilder](https://github.com/ikidou/TypeBuilder) 一个用于生成泛型的简易Builder

#### LICENSE
UNDER MIT LICENSE. 详情见LICENSE文件

