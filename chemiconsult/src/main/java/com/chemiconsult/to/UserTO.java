package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTO {

    private Long id;
    private String username;
    private String email;
    private String rol;
}
