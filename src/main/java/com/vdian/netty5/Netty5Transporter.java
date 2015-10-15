package com.vdian.netty5;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.*;

/**
 * 
 * <p>文件名称：Netty5Transporter.java</p>
 * <p>文件描述：</p>
 * <p>版权所有： 版权所有(C)2011-2099</p>
 * <p>公   司： 口袋购物 </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 * <p>完成日期：2015年10月13日 上午8:28:31</p>
 *
 * @version 1.0
 * @author guowu@koudai.com
 */
public class Netty5Transporter implements Transporter {


    public static final String NAME = "NETTY5";
    
    @Override
    public Server bind(URL url, ChannelHandler handler) throws RemotingException {
        System.out.println("netty5 server");
        return new Netty5Server(url,handler);
    }

    @Override
    public Client connect(URL url, ChannelHandler handler) throws RemotingException {
        System.out.println("netty5 client");
        return new Netty5Client(url,handler);
    }
}
