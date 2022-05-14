package com.github.flowersbloom;

import com.alibaba.fastjson.JSON;
import com.github.flowersbloom.handler.MessageAcceptHandler;
import com.github.flowersbloom.packet.DataPacket;
import com.github.flowersbloom.packet.HeartbeatPacket;
import com.github.flowersbloom.transfer.DataPacketTransfer;
import com.github.flowersbloom.transfer.TransferFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient {
    public static void main(String[] args) {
        NettyClient nettyClient = new NettyClient();

        Scanner scanner = new Scanner(System.in);
        String in = scanner.nextLine();
        while (!in.equals("exit")) {
            DataPacket dataPacket = new DataPacket();
            dataPacket.setReceiverId(userId);
            dataPacket.setContent(in);

            DataPacketTransfer transfer = new DataPacketTransfer();
            TransferFuture future = transfer.channel(nettyClient.datagramChannel)
                    .dstAddress(serverAddress)
                    .packet(dataPacket)
                    .execute();
            future.addListener(f -> {
                if (f.isSuccess()) {
                    log.debug("dataPacket send success");
                }
            });

            in = scanner.nextLine();
        }
        nettyClient.shutdown();
    }

    private static final String userId = "1";
    private static final InetSocketAddress localAddress = new InetSocketAddress(9000);
    private static final InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8080);
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private NioDatagramChannel datagramChannel;

    public NettyClient() {
        eventLoopGroup = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                protected void initChannel(NioDatagramChannel channel) throws Exception {
                    channel.pipeline().addLast(new MessageAcceptHandler());
                }
            });
        try {
            datagramChannel = (NioDatagramChannel) bootstrap.bind(localAddress.getPort()).sync().channel();
            System.out.println("NettyClient start success");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startHeartbeatCycleTask();
    }

    public void startHeartbeatCycleTask() {
        HEARTBEAT_EXECUTOR.scheduleAtFixedRate(() -> {
            HeartbeatPacket heartbeatPacket = new HeartbeatPacket();
            heartbeatPacket.setUserId(userId);
            sendMessage(JSON.toJSONString(heartbeatPacket), serverAddress);
        }, 0, NettyConstant.HEARTBEAT_SEND_RATE_SECONDS, TimeUnit.SECONDS);
        System.out.println("start heartbeat send cycle task");
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
                    System.out.println("NettyClient eventLoopGroup shutdown success");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
