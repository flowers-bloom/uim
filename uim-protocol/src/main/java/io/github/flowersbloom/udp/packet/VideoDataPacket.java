package io.github.flowersbloom.udp.packet;

import io.github.flowersbloom.udp.Command;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频数据包
 */
@Data
public class VideoDataPacket extends BasePacket {
    int sliceNum;
    int sliceCount;
    byte[] bytes;

    public VideoDataPacket() {
        this.command = Command.VIDEO_DATA_PACKET;
    }

    public List<VideoDataPacket> transformSliceList(int maxSliceLength) {
        List<VideoDataPacket> list = new ArrayList<>();
        int sliceCount = bytes.length / maxSliceLength + (
                bytes.length % maxSliceLength > 0 ? 1 : 0
                );
        int sliceNum = 1;
        for (int i = 0; i < bytes.length; i+=maxSliceLength) {
            VideoDataPacket packet = new VideoDataPacket();
            list.add(packet);
            packet.setSliceNum(sliceNum++);
            packet.setSliceCount(sliceCount);
            int length = Math.min(bytes.length - i, maxSliceLength);
            byte[] raw = new byte[length];
            System.arraycopy(bytes, i, raw, 0, length);
            packet.setBytes(raw);
        }
        return list;
    }
}
