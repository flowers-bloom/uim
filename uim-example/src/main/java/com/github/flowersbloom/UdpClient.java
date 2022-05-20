package com.github.flowersbloom;

import com.github.flowersbloom.handler.MessageAcceptHandler;
import com.github.flowersbloom.udp.NettyClient;
import com.github.flowersbloom.udp.packet.DataPacket;
import com.github.flowersbloom.udp.transfer.DataPacketTransfer;
import com.github.flowersbloom.udp.transfer.TransferFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Scanner;

@Slf4j
public class UdpClient {
    private static final InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8080);

    public static void main(String[] args) {
        NettyClient nettyClient = new NettyClient(
                serverAddress,
                Arrays.asList(new MessageAcceptHandler())
        );

        System.out.println("你的身份id为：" + NettyClient.userId);
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
                DataPacket dataPacket = new DataPacket();
                dataPacket.setSenderId(NettyClient.userId);
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
