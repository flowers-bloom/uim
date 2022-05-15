package com.github.flowersbloom;

import com.github.flowersbloom.handler.MessagePushHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyServer {
    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.run();
    }

    public static final int PORT = 8080;
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;

    public NettyServer() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                protected void initChannel(NioDatagramChannel channel) throws Exception {
                    channel.pipeline().addLast(new MessagePushHandler());
                }
            });
    }

    public void run() {
        try {
            NioDatagramChannel channel = (NioDatagramChannel) bootstrap.bind(PORT).sync().channel();
            log.info("NettyServer bind " + PORT + " success");

            startHeartbeatDetectCycleTask();

            channel.closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startHeartbeatDetectCycleTask() {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() -> {
            long currentTimeMillis = System.currentTimeMillis();
            Set<Map.Entry<String, Long>> entrySet = NettyConstant.HEARTBEAT_ACTIVE_MAP.entrySet();
            for (Map.Entry<String, Long> entry : entrySet) {
                if (currentTimeMillis - entry.getValue() >= NettyConstant.HEARTBEAT_TIMEOUT_SECONDS * 1000) {
                    NettyConstant.HEARTBEAT_ACTIVE_MAP.remove(entry.getKey());
                    NettyConstant.ADDRESS_ACTIVE_MAP.remove(entry.getKey());
                    log.info("userId:{}, address:{} inactive", entry.getKey(), entry.getValue());
                }
            }
        }, 0, NettyConstant.HEARTBEAT_DETECT_RATE_SECONDS, TimeUnit.SECONDS);
        log.info("start heartbeat detect cycle task");
    }

    public void shutdown() {
        try {
            Future<?> future = eventLoopGroup.shutdownGracefully().sync();
            future.addListener(f -> {
                if (f.isSuccess()) {
                    System.out.println("NettyServer eventLoopGroup shutdown success");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
