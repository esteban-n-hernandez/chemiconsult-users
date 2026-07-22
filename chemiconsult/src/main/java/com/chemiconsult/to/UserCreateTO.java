package com.chemiconsult.to;

import lombok.Data;

@Data
public class UserCreateTO {
    private String username;
    private String email;
    private String password;
    private String rol;
}
