package io.github.flowersbloom.udp.transfer;

import com.alibaba.fastjson.JSON;
import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.handler.MessageListener;
import io.github.flowersbloom.udp.NettyConstant;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.github.flowersbloom.udp.packet.VideoDataPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DataPacketTransfer implements MessageListener {
    private Channel channel;
    private InetSocketAddress address;
    private BasePacket dataPacket;
    private CompletableFuture<Integer> future;
    private boolean sendSlice = false;
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

    public DataPacketTransfer isSendSlice(boolean sendSlice) {
        this.sendSlice = sendSlice;
        return this;
    }

    public TransferFuture execute() {
        TransferFuture transferFuture = new TransferFuture();
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 3 && !confirm; i++) {
                if (!sendSlice) {
                    sendDataPacket(dataPacket);
                }else {
                    sendMultipleSlice();
                }
                try {
                    future.get(NettyConstant.MSG_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
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

    private void sendMultipleSlice() {
        VideoDataPacket videoDataPacket = (VideoDataPacket) dataPacket;
        List<VideoDataPacket> packetList = videoDataPacket.transformSliceList(1000);
        for (VideoDataPacket packet : packetList) {
            sendDataPacket(packet);
        }
    }

    private void sendDataPacket(BasePacket dataPacket) {
        String json = JSON.toJSONString(dataPacket);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        log.info("bytes length:{}", bytes.length);
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
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
