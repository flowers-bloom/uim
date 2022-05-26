package io.github.flowersbloom.udp.transfer;

import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
/**
 * 多报文传输构造器
 */
public class MultiplePacketTransferBuilder {
    private Channel channel;
    private InetSocketAddress address;
    private BasePacket headerPacket;
    private BasePacket dataPacket;

    public MultiplePacketTransferBuilder channel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public MultiplePacketTransferBuilder dstAddress(InetSocketAddress address) {
        this.address = address;
        return this;
    }

    public MultiplePacketTransferBuilder headerPacket(BasePacket basePacket) {
        this.headerPacket = basePacket;
        return this;
    }

    public MultiplePacketTransferBuilder dataPacket(BasePacket dataPacket) {
        this.dataPacket = dataPacket;
        return this;
    }

    public PacketTransfer build() {
        PacketTransfer transfer = new PacketTransfer();
        transfer.setChannel(channel);
        transfer.setAddress(address);
        transfer.setHeaderPacket(headerPacket);
        transfer.setDataPacket(dataPacket);
        transfer.setSlice(true);
        return transfer;
    }
}
