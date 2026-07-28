package com.chemiconsult.to;

import com.chemiconsult.enums.ModuloEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserTO {

    private Long id;
    private String username;
    private String email;
    private String rol;
    private Set<ModuloEnum> modulos;
}
