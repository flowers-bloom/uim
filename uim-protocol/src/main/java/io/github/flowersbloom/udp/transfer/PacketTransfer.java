package io.github.flowersbloom.udp.transfer;

import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.NettyConstant;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.handler.MessageListener;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Data
public class PacketTransfer implements MessageListener {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final CyclicBarrier cyclicBarrier;
    private volatile boolean confirm = false;

    private Channel channel;
    private InetSocketAddress address;
    private BasePacket headerPacket;
    private BasePacket dataPacket;
    private boolean isSlice;


    PacketTransfer() {
        cyclicBarrier = new CyclicBarrier(2);
        MessageCallback.subscribe(this);
    }

    public TransferFuture execute() {
        TransferFuture transferFuture = new TransferFuture();
        CompletableFuture.runAsync(() -> {
            execHeader();
            execData();
            if (!confirm) {
                log.warn("dataPacket send failed, serialNumber:{}", headerPacket.getSerialNumber());
            }
            transferFuture.complete(confirm);
            MessageCallback.unsubscribe(this);
        }, executorService);
        return transferFuture;
    }

    private void execHeader() {
        if (headerPacket != null) {
            for (int i = 0; i < 3 && !confirm; i++) {
                sendPacket(headerPacket);
                try {
                    cyclicBarrier.await(NettyConstant.MSG_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("headerPacket send err:{}", e.getMessage());
                }
            }
        }
    }

    private void execData() {
        confirm = false;
        cyclicBarrier.reset();
        for (int i = 0; i < 3 && !confirm; i++) {
            if (!isSlice) {
                sendPacket(dataPacket);
            }else {
                sendMultipleSlice();
            }
            try {
                cyclicBarrier.await(NettyConstant.MSG_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("dataPacket send err, serialNumber:{}", dataPacket.getSerialNumber());
            }
        }
    }

    private void sendMultipleSlice() {
        List<ByteBuf> bufList = dataPacket.toNewBufList(headerPacket.getSerialNumber());
        for (ByteBuf byteBuf : bufList) {
            channel.writeAndFlush(new DatagramPacket(byteBuf, address));
        }
    }

    private void sendPacket(BasePacket basePacket) {
        ByteBuf byteBuf = basePacket.toNewBuf();
        channel.writeAndFlush(new DatagramPacket(byteBuf, address));
    }

    @Override
    public void handle(BasePacket basePacket) {
        if ((basePacket.getSerialNumber() == dataPacket.getSerialNumber() ||
                (headerPacket != null && basePacket.getSerialNumber() == headerPacket.getSerialNumber()))
                && basePacket.getCommand() == Command.ACK_PACKET) {
            confirm = true;
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
