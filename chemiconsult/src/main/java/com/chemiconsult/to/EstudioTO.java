package com.chemiconsult.to;

import lombok.Data;

@Data
public class EstudioTO {

    private String tipo;
    private String estado;
    private byte[] archivo;
    private Long userId;
    private String userMail;

}
