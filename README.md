# dubbo-netty5
dubbo netty5 通信层插件
引用插件的jar包

<dependency>
     <groupId>com.vdian.netty5</groupId>
     <artifactId>netty5</artifactId>
     <version>0.0.1-SNAPSHOT</version>
</dependency>
客户端加入Netty5插件

可以在dubbo的xml中对标签dubbo:reference 添加client属性，值为netty5

<dubbo:reference client="netty5" ..../>
上面这种方式需要在定于服务标签都加上对应的属性，这样可能比较麻烦，也可以在dubbo.properties文件中添加

dubbo.reference.client=netty5

这样就对所有的订阅服务都走了netty5的通信了。

服务端加入Netty5插件
同样可以通过xml进行配置,在标签dubbo:provider添加server属性，值为netty5
<dubbo:provider server="netty5"..../>
<dubbo:provider transporter="netty5".../>
<dubbo:protocol transporter="netty5".../>
这样就对当前应用所有发布的服务都走了netty5通信

同样也可以采用dubbo.properties进行配置 添加 dubbo.provider.transporter=netty5即可。
