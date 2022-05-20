package io.github.flowersbloom.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.packet.AckPacket;
import io.github.flowersbloom.udp.packet.ConfirmPacket;
import io.github.flowersbloom.udp.packet.DataPacket;
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
                DataPacket dataPacket = JSON.parseObject(json, DataPacket.class);
                ConfirmPacket confirmPacket = new ConfirmPacket();
                confirmPacket.setSerialNumber(serialNumber);
                confirmPacket.setSenderId(dataPacket.getSenderId());
                String out = JSON.toJSONString(confirmPacket);
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
