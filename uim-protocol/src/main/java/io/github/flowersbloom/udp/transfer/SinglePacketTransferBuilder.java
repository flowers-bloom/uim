package io.github.flowersbloom.udp.transfer;

import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * 单报文传输构造器
 */
public class SinglePacketTransferBuilder {
    private Channel channel;
    private InetSocketAddress address;
    private BasePacket dataPacket;

    public SinglePacketTransferBuilder channel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public SinglePacketTransferBuilder dstAddress(InetSocketAddress address) {
        this.address = address;
        return this;
    }

    public SinglePacketTransferBuilder dataPacket(BasePacket dataPacket) {
        this.dataPacket = dataPacket;
        return this;
    }

    public PacketTransfer build() {
        PacketTransfer transfer = new PacketTransfer();
        transfer.setChannel(channel);
        transfer.setAddress(address);
        transfer.setDataPacket(dataPacket);
        transfer.setSlice(false);
        return transfer;
    }
}
