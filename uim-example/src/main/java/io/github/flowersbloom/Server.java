package io.github.flowersbloom;

import io.github.flowersbloom.handler.MessagePushHandler;
import io.github.flowersbloom.udp.NettyServer;

import java.util.Arrays;

public class Server {

    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer(
                8080,
                Arrays.asList(
                        new MessagePushHandler()
                )
        );
        nettyServer.run();
    }
}
