package com.chemiconsult.to;

import lombok.Data;

@Data
public class UserPasswordTO {

    private String oldPassword;
    private String newPassword;

}
