package io.github.flowersbloom.packet;

import com.alibaba.fastjson.JSON;
import io.github.flowersbloom.command.BizCommand;
import io.github.flowersbloom.udp.entity.User;
import io.github.flowersbloom.udp.packet.BasePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

import java.util.List;

@Data
public class ActiveDataPacket extends BasePacket {
    List<User> activeList;

    public ActiveDataPacket() {
        this.command = BizCommand.ACTIVE_DATA_PACKET;
    }

    @Override
    public ByteBuf toNewBuf() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        this.serialNumber = generateSerialNumber();
        byteBuf.writeLong(this.serialNumber);
        byteBuf.writeByte(this.command);
        String json = JSON.toJSONString(this);
        byteBuf.writeBytes(json.getBytes());
        return byteBuf;
    }

    @Override
    public List<ByteBuf> toNewBufList(long serialNumber) {
        return null;
    }
}
