package io.github.flowersbloom.udp;

import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.udp.packet.HeartbeatPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class NettyClient {
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    public final User user;
    private InetSocketAddress serverAddress;
    private EventLoopGroup eventLoopGroup;
    private DefaultEventExecutorGroup workerGroup;
    public Bootstrap bootstrap;
    public NioDatagramChannel datagramChannel;

    public NettyClient(User user, InetSocketAddress serverAddress, ChannelInboundHandler[] handlers) {
        this.user = user;
        this.serverAddress = serverAddress;

        eventLoopGroup = new NioEventLoopGroup();
        workerGroup = new DefaultEventExecutorGroup(8, new ThreadFactory() {
            AtomicLong count = new AtomicLong(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"worker-pool-" + (count.getAndIncrement()));
            }
        });
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    protected void initChannel(NioDatagramChannel channel) throws Exception {
                        channel.pipeline().addLast(workerGroup, handlers);
                    }
                });
        try {
            datagramChannel = (NioDatagramChannel) bootstrap.bind(user.getAddress().getPort()).sync().channel();
            log.info("NettyClient start success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startHeartbeatCycleTask();
    }

    private void startHeartbeatCycleTask() {
        HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            HeartbeatPacket heartbeatPacket = new HeartbeatPacket();
            heartbeatPacket.setUser(user);
            ByteBuf byteBuf = heartbeatPacket.toNewBuf(0);
            sendMessage(byteBuf, serverAddress);
        }, 0, NettyConstant.HEARTBEAT_SEND_RATE_SECONDS, TimeUnit.SECONDS);
        log.info("start heartbeat send cycle task");
    }

    public void sendMessage(ByteBuf buffer, InetSocketAddress socketAddress) {
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
