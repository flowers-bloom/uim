package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import io.netty.buffer.ByteBuf;
import lombok.Data;

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
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        byteBuf.writeBytes(senderId.getBytes());
        return byteBuf;
    }
}
