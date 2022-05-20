package com.github.flowersbloom.udp;

import com.alibaba.fastjson.JSON;
import com.github.flowersbloom.udp.packet.HeartbeatPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient {
    /**
     * userId长度为16位
     */
    public static String userId = String.valueOf(System.currentTimeMillis()).substring(5) +
            randomNumber(8);
    private static final InetSocketAddress localAddress = new InetSocketAddress(0);
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private InetSocketAddress serverAddress;
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    public NioDatagramChannel datagramChannel;

    public NettyClient(InetSocketAddress serverAddress, List<ChannelInboundHandler> handlerList) {
        this.serverAddress = serverAddress;

        eventLoopGroup = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    protected void initChannel(NioDatagramChannel channel) throws Exception {
                    for (ChannelInboundHandler handler : handlerList) {
                        channel.pipeline().addLast(handler);
                    }
                    }
                });
        try {
            datagramChannel = (NioDatagramChannel) bootstrap.bind(localAddress.getPort()).sync().channel();
            log.info("NettyClient start success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startHeartbeatCycleTask();
    }

    private static String randomNumber(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    public void startHeartbeatCycleTask() {
        HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            HeartbeatPacket heartbeatPacket = new HeartbeatPacket();
            heartbeatPacket.setUserId(userId);
            sendMessage(JSON.toJSONString(heartbeatPacket), serverAddress);
        }, 0, NettyConstant.HEARTBEAT_SEND_RATE_SECONDS, TimeUnit.SECONDS);
        log.info("start heartbeat send cycle task");
    }

    public void sendMessage(String msg, InetSocketAddress socketAddress) {
        ByteBuf buffer = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
        datagramChannel.writeAndFlush(new DatagramPacket(buffer, socketAddress));
    }

    public void shutdown() {
        try {
            HEARTBEAT_EXECUTOR.shutdown();
            Future<?> future = eventLoopGroup.shutdownGracefully().sync();
            future.addListener(f -> {
                if (f.isSuccess()) {
                    log.info("NettyClient eventLoopGroup shutdown success");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
