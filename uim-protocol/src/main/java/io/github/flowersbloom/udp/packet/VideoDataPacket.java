package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频数据包
 */
@Data
@Slf4j
public class VideoDataPacket extends BasePacket {
    byte[] bytes;

    public VideoDataPacket() {
        this.command = Command.VIDEO_DATA_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        return null;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        List<ByteBuf> list = new ArrayList<>();
        this.serialNumber = serialNumber;
        for (int i = 0, sliceNum = 1; i < bytes.length; i+=DEFAULT_SLICE_LENGTH, sliceNum++) {
            int length = Math.min(bytes.length - i, DEFAULT_SLICE_LENGTH);
            byte[] raw = new byte[length];
            System.arraycopy(bytes, i, raw, 0, length);

            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
            list.add(byteBuf);
            byteBuf.writeLong(this.serialNumber);
            byteBuf.writeByte(Command.VIDEO_DATA_PACKET);
            byteBuf.writeInt(sliceNum);
            byteBuf.writeBytes(raw);
        }
        return list;
    }
}
