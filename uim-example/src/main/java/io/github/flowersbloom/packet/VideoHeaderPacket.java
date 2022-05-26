package io.github.flowersbloom.packet;

import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class VideoHeaderPacket extends BasePacket {
    int bytesLength;
    int totalCount;

    public VideoHeaderPacket() {
        this.command = BizCommand.VIDEO_HEADER_PACKET;
    }

    @Override
    public ByteBuf toNewBuf(long serialNumber) {
        ByteBuf byteBuf = super.toNewBuf(serialNumber);
        int totalCount = (bytesLength / DEFAULT_SLICE_LENGTH) +
                (bytesLength % DEFAULT_SLICE_LENGTH == 0 ? 0 : 1);
        this.setTotalCount(totalCount);
        byteBuf.writeInt(totalCount);
        return byteBuf;
    }
}
