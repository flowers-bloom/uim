package io.github.flowersbloom.udp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.net.InetSocketAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User implements Serializable {
    String userId;
    String userNickname;
    String userAvatar;
    InetSocketAddress address;
}
