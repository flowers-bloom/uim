package io.github.flowersbloom;

import io.github.flowersbloom.handler.MessageAcceptHandler;
import io.github.flowersbloom.udp.NettyClient;
import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.packet.P2PDataPacket;
import io.github.flowersbloom.packet.VideoDataPacket;
import io.github.flowersbloom.packet.VideoHeaderPacket;
import io.github.flowersbloom.udp.transfer.MultiplePacketTransferBuilder;
import io.github.flowersbloom.udp.transfer.SinglePacketTransferBuilder;
import io.github.flowersbloom.udp.transfer.TransferFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

@Slf4j
public class Client {
    private static final InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8080);

    public static void main(String[] args) {
        User user = new User("2", "tom", "",
                new InetSocketAddress(9001));
        NettyClient nettyClient = new NettyClient(
                user,
                serverAddress,
                new ChannelInboundHandler[]{new MessageAcceptHandler()}
        );

        execBatchTask(nettyClient.datagramChannel);

//        run(nettyClient, user);

        //nettyClient.shutdown();
        try {
            nettyClient.datagramChannel.closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void execBatchTask(NioDatagramChannel channel) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                sendFileStream(channel);
            }).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendFileStream(NioDatagramChannel channel) {
        try {
            FileInputStream inputStream = new FileInputStream("codec.h264");
            byte[] bytes = new byte[25000];
            int n;

            n = inputStream.read(bytes);

            VideoHeaderPacket videoHeaderPacket = new VideoHeaderPacket();
            videoHeaderPacket.setBytesLength(n);
            VideoDataPacket videoDataPacket = new VideoDataPacket();
            if (n == bytes.length) {
                videoDataPacket.setBytes(bytes);
            }else {
                byte[] dst = new byte[n];
                System.arraycopy(bytes, 0, dst, 0, n);
                videoDataPacket.setBytes(dst);
            }

            long cur = System.currentTimeMillis();
            MultiplePacketTransferBuilder builder = new MultiplePacketTransferBuilder();
            TransferFuture future = builder.channel(channel)
                    .dstAddress(new InetSocketAddress("localhost", 9000))
                    .headerPacket(videoHeaderPacket)
                    .dataPacket(videoDataPacket)
                    .build()
                    .execute();
            future.addListener(f -> {
                if (f.isSuccess()) {
                    log.info("videoPacket send success, serialNumber:{}, cost:{} ms",
                            videoHeaderPacket.getSerialNumber(),
                            (System.currentTimeMillis() - cur));
                }
            });

            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void run(NettyClient nettyClient, User user) {
        System.out.println("你的身份id为：" + user.getUserId());
        String inputTip = "请输入接收者数字身份id和消息内容，以两个英文分号分隔，" +
                "样例如（1;;welcome to uim.），或者输入exit退出：",
                errTip = "格式错误，请重新输入：";
        System.out.println(inputTip);

        Scanner scanner = new Scanner(System.in);
        String in = scanner.nextLine();
        while (!in.equals("exit")) {
            String[] params = in.split(";;");
            if (!checkInput(params)) {
                System.out.println(errTip);
            }else {
                P2PDataPacket p2PDataPacket = new P2PDataPacket();
                p2PDataPacket.setSenderId(user.getUserId());
                p2PDataPacket.setReceiverId(params[0]);
                p2PDataPacket.setContent(params[1]);

                SinglePacketTransferBuilder builder = new SinglePacketTransferBuilder();
                TransferFuture future = builder.channel(nettyClient.datagramChannel)
                        .dstAddress(serverAddress)
                        .dataPacket(p2PDataPacket)
                        .build()
                        .execute();
                future.addListener(f -> {
                    if (f.isSuccess()) {
                        log.debug("dataPacket send success");
                    }
                });
            }
            in = scanner.nextLine();
        }
    }

    private static boolean checkInput(String[] arr) {
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
}
