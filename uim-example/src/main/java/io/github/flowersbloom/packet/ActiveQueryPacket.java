package io.github.flowersbloom.packet;

import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.List;

public class ActiveQueryPacket extends BasePacket {
    public ActiveQueryPacket() {
        this.command = BizCommand.ACTIVE_QUERY_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        this.serialNumber = generateSerialNumber();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(this.command);
        return byteBuf;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        return null;
    }
}
