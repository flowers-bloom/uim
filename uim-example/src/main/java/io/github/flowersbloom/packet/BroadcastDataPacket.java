package io.github.flowersbloom.packet;

import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.packet.BasePacket;
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
        this.command = BizCommand.BROADCAST_DATA_PACKET;
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
