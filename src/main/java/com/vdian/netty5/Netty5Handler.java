package com.vdian.netty5;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;

/**
 * 
 * <p>文件名称：Netty5Handler.java</p>
 * <p>文件描述：</p>
 * <p>版权所有： 版权所有(C)2011-2099</p>
 * <p>公   司： 口袋购物 </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 * <p>完成日期：2015年10月13日 上午8:29:04</p>
 *
 * @version 1.0
 * @author guowu@koudai.com
 */
 
@io.netty.channel.ChannelHandler.Sharable
public class Netty5Handler extends ChannelHandlerAdapter {

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    private final URL url;

    private final ChannelHandler handler;

    public Netty5Handler(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }


    public Map<String, Channel> getChannels() {
        return channels;
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Netty5Channel channel = Netty5Channel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            if (channel != null) {
                channels.put(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), channel);
            }
            handler.connected(channel);
        } finally {
            Netty5Channel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Netty5Channel channel = Netty5Channel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()));
            handler.disconnected(channel);
        } finally {
            Netty5Channel.removeChannelIfDisconnected(ctx.channel());
        }
    }
    
    

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Netty5Channel channel = Netty5Channel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.caught(channel, cause.getCause());
        } finally {
            Netty5Channel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        Netty5Channel channel = Netty5Channel.getOrAddChannel(channelHandlerContext.channel(), url, handler);
        try {
            handler.received(channel, o);
        } finally {
            Netty5Channel.removeChannelIfDisconnected(channelHandlerContext.channel());
        }
    }

}
