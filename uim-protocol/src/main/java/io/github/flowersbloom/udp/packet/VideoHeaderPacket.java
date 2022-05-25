package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

import java.util.List;

@Data
public class VideoHeaderPacket extends BasePacket {
    int bytesLength;
    int totalCount;

    @Override
    public ByteBuf toNewBuf() {
        this.serialNumber = generateSerialNumber();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(Command.VIDEO_HEADER_PACKET);
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
