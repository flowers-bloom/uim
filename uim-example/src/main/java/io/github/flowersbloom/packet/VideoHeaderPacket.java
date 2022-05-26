package io.github.flowersbloom.packet;

import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

import java.util.List;

@Data
public class VideoHeaderPacket extends BasePacket {
    int bytesLength;
    int totalCount;

    public VideoHeaderPacket() {
        this.command = BizCommand.VIDEO_HEADER_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        this.serialNumber = generateSerialNumber();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(this.command);
        int totalCount = (bytesLength / DEFAULT_SLICE_LENGTH) +
                (bytesLength % DEFAULT_SLICE_LENGTH == 0 ? 0 : 1);
        this.setTotalCount(totalCount);
        byteBuf.writeInt(totalCount);
        return byteBuf;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        return null;
    }
}
