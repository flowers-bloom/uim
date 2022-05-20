package io.github.flowersbloom.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.NettyConstant;
import io.github.flowersbloom.udp.packet.AckPacket;
import io.github.flowersbloom.udp.packet.ConfirmPacket;
import io.github.flowersbloom.udp.packet.DataPacket;
import io.github.flowersbloom.udp.packet.HeartbeatPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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

        switch (command.byteValue()) {
            case Command.HEARTBEAT_PACKET:
                HeartbeatPacket heartbeatPacket = JSON.parseObject(json, HeartbeatPacket.class);
                if (!NettyConstant.ADDRESS_ACTIVE_MAP.containsKey(heartbeatPacket.getUserId())) {
                    NettyConstant.ADDRESS_ACTIVE_MAP.put(heartbeatPacket.getUserId(), msg.sender());
                    log.info("userId:{}, address:{} active", heartbeatPacket.getUserId(), msg.sender());
                }
                NettyConstant.HEARTBEAT_ACTIVE_MAP.put(heartbeatPacket.getUserId(), System.currentTimeMillis());
                break;
            case Command.DATA_PACKET:
                DataPacket dataPacket = JSON.parseObject(json, DataPacket.class);
                InetSocketAddress address = NettyConstant.ADDRESS_ACTIVE_MAP.get(dataPacket.getReceiverId());
                if (address != null) {
                    ctx.channel().writeAndFlush(new DatagramPacket(
                            Unpooled.copiedBuffer(dst), address
                    ));
                }else {
                    //如果对方不在线，直接返回ack
                    sendAckPacket(serialNumber, ctx.channel(), msg.sender());
                }
                break;
            case Command.CONFIRM_PACKET:
                ConfirmPacket confirmPacket = JSON.parseObject(json, ConfirmPacket.class);
                InetSocketAddress senderAddress = NettyConstant.ADDRESS_ACTIVE_MAP.get(confirmPacket.getSenderId());
                if (senderAddress != null) {
                    sendAckPacket(serialNumber, ctx.channel(), senderAddress);
                }
                break;
            default:
                System.out.println("command not found: " + command);
        }
    }

    /**
     * 发送 ack 报文
     * @param serialNumber
     * @param channel
     * @param senderAddress
     */
    public void sendAckPacket(long serialNumber, Channel channel, InetSocketAddress senderAddress) {
        AckPacket ackPacket = new AckPacket();
        ackPacket.setSerialNumber(serialNumber);
        String out = JSON.toJSONString(ackPacket);
        channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(out.getBytes(StandardCharsets.UTF_8)),
                senderAddress));
    }
}
