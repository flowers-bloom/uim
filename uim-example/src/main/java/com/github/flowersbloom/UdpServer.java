package com.github.flowersbloom;

import com.github.flowersbloom.handler.MessagePushHandler;
import com.github.flowersbloom.udp.NettyServer;

import java.util.Arrays;

public class UdpServer {

    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer(
                8080,
                Arrays.asList(new MessagePushHandler())
        );
        nettyServer.run();
    }
}
