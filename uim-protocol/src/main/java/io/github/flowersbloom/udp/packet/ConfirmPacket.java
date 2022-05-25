package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

import java.util.List;

/**
 * 确认报文
 */
@Data
public class ConfirmPacket extends BasePacket {
    String senderId;

    public ConfirmPacket() {
        this.command = Command.CONFIRM_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        this.serialNumber = generateSerialNumber();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(Command.CONFIRM_PACKET);
        byteBuf.writeBytes(senderId.getBytes());
        return byteBuf;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        return null;
    }
}
