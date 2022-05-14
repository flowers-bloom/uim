package com.github.flowersbloom.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.flowersbloom.*;
import com.github.flowersbloom.packet.AckPacket;
import com.github.flowersbloom.packet.DataPacket;
import com.github.flowersbloom.packet.HeartbeatPacket;
import com.github.flowersbloom.transfer.DataPacketTransfer;
import com.github.flowersbloom.transfer.TransferFuture;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


@Slf4j
public class MessagePushHandler extends SimpleChannelInboundHandler<DatagramPacket>
                        implements MessageCallback {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf inputBuf = msg.content();
        byte[] dst = new byte[inputBuf.readableBytes()];
        inputBuf.readBytes(dst);
        String json = new String(dst);

        JSONObject jsonObject = JSON.parseObject(json);
        Integer command = (Integer) jsonObject.get("command");
        long serialNumber = (long) jsonObject.get("serialNumber");

        boolean needAck = false;
        switch (command.byteValue()) {
            case Command.HEARTBEAT_PACKET:
                HeartbeatPacket heartbeatPacket = JSON.parseObject(json, HeartbeatPacket.class);
                if (!NettyConstant.ADDRESS_ACTIVE_MAP.containsKey(heartbeatPacket.getUserId())) {
                    NettyConstant.ADDRESS_ACTIVE_MAP.put(heartbeatPacket.getUserId(), msg.sender());
                }
                NettyConstant.HEARTBEAT_ACTIVE_MAP.put(heartbeatPacket.getUserId(), System.currentTimeMillis());
                break;
            case Command.DATA_PACKET:
                DataPacket dataPacket = JSON.parseObject(json, DataPacket.class);
                InetSocketAddress address = NettyConstant.ADDRESS_ACTIVE_MAP.get(dataPacket.getReceiverId());
                if (address != null) {
                    DataPacketTransfer transfer = new DataPacketTransfer();
                    TransferFuture future = transfer.channel(ctx.channel())
                            .dstAddress(address)
                            .packet(dataPacket)
                            .execute();
                    future.addListener(f -> {
                        if (f.isSuccess()) {
                            log.debug("dataPacket send success");
                        }
                    });
                }
                needAck = true;
                break;
            case Command.ACK_PACKET:
                AckPacket ackPacket = JSON.parseObject(json, AckPacket.class);
                notice(ackPacket);
                break;
            default:
                System.out.println("command not found: " + command);
        }

        //udp消息接收响应
        if (needAck) {
            AckPacket ackPacket = new AckPacket();
            ackPacket.setSerialNumber(serialNumber);
            String out = JSON.toJSONString(ackPacket);
            ctx.channel().writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(out.getBytes(StandardCharsets.UTF_8)),
                    msg.sender())
            );
        }
    }
}
