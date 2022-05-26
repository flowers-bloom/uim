package io.github.flowersbloom.packet;

import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class ActiveQueryPacket extends BasePacket {
    String userId;

    public ActiveQueryPacket() {
        this.command = BizCommand.ACTIVE_QUERY_PACKET;
    }

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        byteBuf.writeBytes(userId.getBytes());
        return byteBuf;
    }
}
