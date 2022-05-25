package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

import java.util.List;

/**
 * 消息收到报文
 */
@Data
public class AckPacket extends BasePacket {
    public AckPacket() {
        this.command = Command.ACK_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        this.serialNumber = generateSerialNumber();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(Command.ACK_PACKET);
        return byteBuf;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        return null;
    }
}
