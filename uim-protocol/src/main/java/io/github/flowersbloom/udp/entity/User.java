package io.github.flowersbloom.udp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.net.InetSocketAddress;

@Data
@AllArgsConstructor
@ToString
public class User {
    String userId;
    String userNickname;
    String userAvatar;
    InetSocketAddress address;
}
