package io.github.flowersbloom.udp;

import com.alibaba.fastjson.JSON;
import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.github.flowersbloom.udp.packet.HeartbeatPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient {
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    public final User user;
    private InetSocketAddress serverAddress;
    private EventLoopGroup eventLoopGroup;
    public Bootstrap bootstrap;
    public NioDatagramChannel datagramChannel;

    public NettyClient(User user, InetSocketAddress serverAddress, List<ChannelInboundHandler> handlerList) {
        this.user = user;
        this.serverAddress = serverAddress;

        eventLoopGroup = new NioEventLoopGroup(10);
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
            datagramChannel = (NioDatagramChannel) bootstrap.bind(user.getAddress().getPort()).sync().channel();
            log.info("NettyClient start success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startHeartbeatCycleTask();
    }

    private void startHeartbeatCycleTask() {
        HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
            HeartbeatPacket heartbeatPacket = new HeartbeatPacket();
            heartbeatPacket.setUser(user);
            byteBuf.writeLong(BasePacket.generateSerialNumber());
            byteBuf.writeByte(heartbeatPacket.getCommand());
            byteBuf.writeBytes(JSON.toJSONString(heartbeatPacket).getBytes());
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
