package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstudioTO {

    private Long id;
    private String cliente;
    private String tipo;
    private String estado;
    private byte[] archivo;
    private String archivoUrl;
    private Long userId;
    private String userMail;
    private String createdDate;

}
