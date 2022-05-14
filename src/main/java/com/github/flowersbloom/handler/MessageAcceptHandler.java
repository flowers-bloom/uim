package com.github.flowersbloom.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.flowersbloom.Command;
import com.github.flowersbloom.packet.AckPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class MessageAcceptHandler extends SimpleChannelInboundHandler<DatagramPacket>
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

        switch (command.byteValue()) {
            case Command.ACK_PACKET:
                AckPacket ackPacket = JSON.parseObject(json, AckPacket.class);
                notice(ackPacket);
                break;
            case Command.DATA_PACKET:
                ackPacket = new AckPacket();
                ackPacket.setSerialNumber(serialNumber);
                String out = JSON.toJSONString(ackPacket);
                ctx.channel().writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(out.getBytes(StandardCharsets.UTF_8)),
                        msg.sender())
                );
                log.info("receive msg:{}", json);
                break;
            default:
                System.out.println("command not found: " + command);
        }
    }
}
