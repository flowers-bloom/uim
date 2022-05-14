package com.github.flowersbloom.transfer;

import com.alibaba.fastjson.JSON;
import com.github.flowersbloom.Command;
import com.github.flowersbloom.handler.MessageCallback;
import com.github.flowersbloom.handler.MessageListener;
import com.github.flowersbloom.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DataPacketTransfer implements MessageListener {
    private Channel channel;
    private InetSocketAddress address;
    private BasePacket dataPacket;
    private CompletableFuture<Integer> future;
    private volatile boolean confirm = false;

    public DataPacketTransfer() {
        future = new CompletableFuture<>();
        MessageCallback.subscribe(this);
    }

    public DataPacketTransfer channel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public DataPacketTransfer dstAddress(InetSocketAddress address) {
        this.address = address;
        return this;
    }

    public DataPacketTransfer packet(BasePacket dataPacket) {
        this.dataPacket = dataPacket;
        return this;
    }

    public TransferFuture execute() {
        TransferFuture transferFuture = new TransferFuture();
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 3 && !confirm; i++) {
                sendDataPacket();
                try {
                    future.get(3, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("dataPacket:{} send err:{}", dataPacket, e);
                }
            }
            if (!confirm) {
                log.warn("dataPacket:{} send failed", dataPacket);
            }
            transferFuture.complete(confirm);
            MessageCallback.unsubscribe(this);
        });
        return transferFuture;
    }

    private void sendDataPacket() {
        String json = JSON.toJSONString(dataPacket);
        ByteBuf byteBuf = Unpooled.copiedBuffer(json.getBytes(StandardCharsets.UTF_8));
        channel.writeAndFlush(new DatagramPacket(byteBuf, address));
    }

    @Override
    public void handle(BasePacket basePacket) {
        if (basePacket.getCommand().equals(Command.ACK_PACKET) &&
                basePacket.getSerialNumber().equals(dataPacket.getSerialNumber())) {
            future.complete(1);
            confirm = true;
        }
    }
}
