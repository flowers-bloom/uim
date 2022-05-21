package io.github.flowersbloom.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.packet.AckPacket;
import io.github.flowersbloom.udp.packet.ConfirmPacket;
import io.github.flowersbloom.udp.packet.P2PDataPacket;
import io.github.flowersbloom.udp.packet.VideoDataPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageAcceptHandler extends SimpleChannelInboundHandler<DatagramPacket>
                        implements MessageCallback {

    private static final ConcurrentHashMap<Long, PriorityQueue<VideoDataPacket>> MULTIPLE_PACKET_CACHE
            = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf inputBuf = msg.content();
        byte[] dst = new byte[inputBuf.readableBytes()];
        inputBuf.readBytes(dst);
        String json = new String(dst);
        JSONObject jsonObject = JSON.parseObject(json);
        Integer command = (Integer) jsonObject.get("command");
        long serialNumber = (long) jsonObject.get("serialNumber");

        switch (command.byteValue()) {
            case Command.ACK_PACKET:
                AckPacket ackPacket = JSON.parseObject(json, AckPacket.class);
                notice(ackPacket);
                break;
            case Command.P2P_DATA_PACKET:
                P2PDataPacket p2PDataPacket = JSON.parseObject(json, P2PDataPacket.class);
                ConfirmPacket confirmPacket = new ConfirmPacket();
                confirmPacket.setSerialNumber(serialNumber);
                confirmPacket.setSenderId(p2PDataPacket.getSenderId());
                String out = JSON.toJSONString(confirmPacket);
                ctx.channel().writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(out.getBytes(StandardCharsets.UTF_8)),
                        msg.sender())
                );
                log.info("receive msg:{}", json);
                break;
            case Command.VIDEO_DATA_PACKET:
                VideoDataPacket videoDataPacket = JSON.parseObject(json, VideoDataPacket.class);
                PriorityQueue<VideoDataPacket> queue = MULTIPLE_PACKET_CACHE.get(videoDataPacket.getSerialNumber());
                log.info("sliceNum:{} accept, map size:{}", videoDataPacket.getSliceNum(), MULTIPLE_PACKET_CACHE.size());
                if (queue == null) {
                    queue = new PriorityQueue<>(Comparator.comparingInt(VideoDataPacket::getSliceNum));
                    queue.offer(videoDataPacket);
                    MULTIPLE_PACKET_CACHE.put(videoDataPacket.getSerialNumber(), queue);
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(3000);
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PriorityQueue<VideoDataPacket> removeKey = MULTIPLE_PACKET_CACHE.remove(videoDataPacket.getSerialNumber());
                        if (removeKey != null) {
                            log.warn("serialNumber: {} timeout remove", videoDataPacket.getSerialNumber());
                        }
                    });
                }else {
                    queue.offer(videoDataPacket);
                    log.info("queue size:{}", queue.size());
                    if (queue.size() == videoDataPacket.getSliceCount()) {
                        ackPacket = new AckPacket();
                        ackPacket.setSerialNumber(videoDataPacket.getSerialNumber());
                        out = JSON.toJSONString(ackPacket);
                        ctx.channel().writeAndFlush(new DatagramPacket(
                                Unpooled.copiedBuffer(out.getBytes(StandardCharsets.UTF_8)),
                                msg.sender())
                        );

                        //merge
                        String str = "";
                        while (!queue.isEmpty()) {
                            str += queue.poll();
                        }
                        log.info("serialNumber: {} merge to str:{}", videoDataPacket.getSerialNumber(), str);
                        MULTIPLE_PACKET_CACHE.remove(videoDataPacket.getSerialNumber());
                    }
                }
                break;
            default:
                System.out.println("command not found: " + command);
        }
    }
}
