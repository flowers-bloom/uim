package io.github.flowersbloom.udp.packet;

import com.alibaba.fastjson.JSON;
import io.github.flowersbloom.udp.Command;
import io.github.flowersbloom.udp.entity.User;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

import java.util.List;

/**
 * 心跳报文
 */
@Data
public class HeartbeatPacket extends BasePacket {
    User user;

    public HeartbeatPacket() {
        this.command = Command.HEARTBEAT_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        this.serialNumber = generateSerialNumber();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(this.command);
        byteBuf.writeBytes(JSON.toJSONString(this).getBytes());
        return byteBuf;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        return null;
    }
}
