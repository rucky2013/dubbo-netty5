package com.vdian.netty5;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 
 * <p>文件名称：Netty5Client.java</p>
 * <p>文件描述：</p>
 * <p>版权所有： 版权所有(C)2011-2099</p>
 * <p>公   司： 口袋购物 </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 * <p>完成日期：2015年10月13日 上午8:29:24</p>
 *
 * @version 1.0
 * @author guowu@koudai.com
 */
public class Netty5Client extends AbstractClient {

    private static final Logger logger = LoggerFactory.getLogger(Netty5Client.class);

    private Bootstrap bootstrap;

    private io.netty.channel.Channel channel;
    
    public Netty5Client(URL url, ChannelHandler handler) throws RemotingException {
        super(url, wrapChannelHandler(url, handler));
    }

    @Override
    protected void doOpen() throws Throwable {
        EventLoopGroup bossGroup = new NioEventLoopGroup(Constants.DEFAULT_IO_THREADS,Executors.newCachedThreadPool(new NamedThreadFactory("NettyClientBoss", true)));
        final Netty5Handler nettyHandler = new Netty5Handler(getUrl(), this);
        bootstrap = new Bootstrap();
        bootstrap.group(bossGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_TIMEOUT, getTimeout())
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        Netty5CodecAdapter adapter = new Netty5CodecAdapter(getCodec(), getUrl(), Netty5Client.this);
                        ch.pipeline().addLast("decoder", adapter.getDecoder())
                                .addLast("encoder", adapter.getEncoder())
                                .addLast("handler", nettyHandler);
                    }
                });
    }

    @Override
    protected void doClose() throws Throwable {
        //do　nothing
    }

    @Override
    protected void doConnect() throws Throwable {
        long start = System.currentTimeMillis();
        ChannelFuture future = bootstrap.connect(getConnectAddress()).sync();
        try{
            boolean ret = future.awaitUninterruptibly(getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (ret && future.isSuccess()) {
                io.netty.channel.Channel newChannel = future.channel();
                try {
                    // 关闭旧的连接
                    io.netty.channel.Channel oldChannel = Netty5Client.this.channel; // copy reference
                    if (oldChannel != null) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close old netty channel " + oldChannel + " on create new netty channel " + newChannel);
                            }
                            oldChannel.close();
                        } finally {
                            Netty5Channel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                } finally {
                    if (Netty5Client.this.isClosed()) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close new netty channel " + newChannel + ", because the client closed.");
                            }
                            newChannel.close();
                        } finally {
                            Netty5Client.this.channel = null;
                            Netty5Channel.removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        Netty5Client.this.channel = newChannel;
                    }
                }
            } else if (future.cause() != null) {
                throw new RemotingException(this, "client(url: " + getUrl() + ") failed to connect to server "
                        + getRemoteAddress() + ", error message is:" + future.cause().getMessage(), future.cause());
            } else {
                throw new RemotingException(this, "client(url: " + getUrl() + ") failed to connect to server "
                        + getRemoteAddress() + " client-side timeout "
                        + getConnectTimeout() + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client "
                        + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion());
            }
        }finally{
            if (! isConnected()) {
                future.cancel(true);
            }
        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            Netty5Channel.removeChannelIfDisconnected(channel);
        } catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected Channel getChannel() {
        io.netty.channel.Channel c = channel;
        if (c == null || ! c.isOpen())
            return null;
        return Netty5Channel.getOrAddChannel(c, getUrl(), this);
    }
}
