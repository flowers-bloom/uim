package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.util.List;

/**
 * 广播报文
 */
@Data
public class BroadcastDataPacket extends BasePacket {
    String senderId;
    String content;

    public BroadcastDataPacket() {
        this.command = Command.BROADCAST_DATA_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        return null;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        return null;
    }
}
