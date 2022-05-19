package com.github.flowersbloom;

import com.alibaba.fastjson.JSON;
import com.github.flowersbloom.handler.MessageAcceptHandler;
import com.github.flowersbloom.packet.DataPacket;
import com.github.flowersbloom.packet.HeartbeatPacket;
import com.github.flowersbloom.transfer.DataPacketTransfer;
import com.github.flowersbloom.transfer.TransferFuture;
import com.github.flowersbloom.util.RandomUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
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

        System.out.println("你的身份id为：" + userId);
        String inputTip = "请输入接收者数字身份id和消息内容，以两个英文分号分隔，" +
                "样例如（1;;welcome to uim.），或者输入exit退出：",
                errTip = "格式错误，请重新输入：";
        System.out.println(inputTip);

        Scanner scanner = new Scanner(System.in);
        String in = scanner.nextLine();
        while (!in.equals("exit")) {
            String[] params = in.split(";;");
            if (!nettyClient.checkInput(params)) {
                System.out.println(errTip);
            }else {
                DataPacket dataPacket = new DataPacket();
                dataPacket.setSenderId(userId);
                dataPacket.setReceiverId(params[0]);
                dataPacket.setContent(params[1]);

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
            }
            in = scanner.nextLine();
        }
        nettyClient.shutdown();
    }

    private boolean checkInput(String[] arr) {
        if (arr.length != 2) {
            return false;
        }
        try {
            Long.parseLong(arr[0]);
        }catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * userId长度为16位
     */
    private static String userId = String.valueOf(System.currentTimeMillis()).substring(5) +
            RandomUtil.randomNumber(8);
    private static final InetSocketAddress localAddress = new InetSocketAddress(0);
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
            log.info("NettyClient start success");
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
