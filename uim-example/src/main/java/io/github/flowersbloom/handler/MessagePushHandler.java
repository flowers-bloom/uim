package io.github.flowersbloom.handler;

import com.alibaba.fastjson.JSON;
import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.NettyConstant;
import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.udp.handler.MessageCallback;
import io.github.flowersbloom.udp.packet.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Slf4j
public class MessagePushHandler extends SimpleChannelInboundHandler<DatagramPacket>
                        implements MessageCallback {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        long serialNumber = byteBuf.readLong();
        byte command = byteBuf.readByte();
        byte[] dst = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(dst);

        switch (command) {
            case Command.HEARTBEAT_PACKET:
                HeartbeatPacket heartbeatPacket = JSON.parseObject(new String(dst), HeartbeatPacket.class);
                User user = Objects.requireNonNull(heartbeatPacket.getUser(), "user is null");
                user.setAddress(msg.sender());
                if (!NettyConstant.USER_ACTIVE_MAP.containsKey(user.getUserId())) {
                    NettyConstant.USER_ACTIVE_MAP.put(user.getUserId(), user);
                    log.info("user:{} active", user);
                }
                NettyConstant.HEARTBEAT_ACTIVE_MAP.put(user.getUserId(), System.currentTimeMillis());
                break;
            case Command.P2P_DATA_PACKET:
                P2PDataPacket p2PDataPacket = JSON.parseObject(new String(dst), P2PDataPacket.class);
                byteBuf = ByteBufAllocator.DEFAULT.buffer();
                byteBuf.writeLong(serialNumber);
                byteBuf.writeByte(p2PDataPacket.getCommand());
                byteBuf.writeBytes(dst);
                user = NettyConstant.USER_ACTIVE_MAP.get(p2PDataPacket.getReceiverId());
                if (user != null) {
                    ctx.channel().writeAndFlush(new DatagramPacket(
                            byteBuf, user.getAddress()
                    ));
                }else {
                    //如果对方不在线，直接返回ack
                    sendAckPacket(serialNumber, ctx.channel(), msg.sender());
                }
                break;
            case Command.BROADCAST_DATA_PACKET:
                BroadcastDataPacket broadcastDataPacket = JSON.parseObject(new String(dst), BroadcastDataPacket.class);
                Set<Map.Entry<String, User>> entrySet = NettyConstant.USER_ACTIVE_MAP.entrySet();
                for (Map.Entry<String, User> entry : entrySet) {
                    User value = entry.getValue();
                    if (!broadcastDataPacket.getSenderId().equals(value.getUserId())) {
                        ctx.channel().writeAndFlush(new DatagramPacket(
                                Unpooled.copiedBuffer(dst), value.getAddress()
                        ));
                    }
                }
                break;
            case Command.CONFIRM_PACKET:
                ConfirmPacket confirmPacket = JSON.parseObject(new String(dst), ConfirmPacket.class);
                user = NettyConstant.USER_ACTIVE_MAP.get(confirmPacket.getSenderId());
                if (user != null) {
                    sendAckPacket(serialNumber, ctx.channel(), user.getAddress());
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
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        AckPacket ackPacket = new AckPacket();
        String out = JSON.toJSONString(ackPacket);
        byteBuf.writeLong(serialNumber);
        byteBuf.writeByte(ackPacket.getCommand());
        byteBuf.writeBytes(out.getBytes());
        channel.writeAndFlush(new DatagramPacket(byteBuf, senderAddress));
    }
}
