package io.github.flowersbloom.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyServer {
    public int port;
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;

    public NettyServer(int port, List<ChannelInboundHandler> handlerList) {
        this.port = port;
        eventLoopGroup = new NioEventLoopGroup();
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
    }

    public void run() {
        try {
            NioDatagramChannel channel = (NioDatagramChannel) bootstrap.bind(port).sync().channel();
            log.info("NettyServer bind " + port + " success");

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
