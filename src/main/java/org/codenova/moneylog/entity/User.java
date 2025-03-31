package org.codenova.moneylog.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Builder
public class User {
    private int id;
    private String email;
    private String password;
    private String nickname;
    private String picture;
    private String provider;
    private String verified;
    private LocalDate datetime;
}
