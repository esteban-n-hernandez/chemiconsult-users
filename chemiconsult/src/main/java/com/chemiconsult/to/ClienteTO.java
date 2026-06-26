package com.chemiconsult.to;

import com.chemiconsult.enums.CondicionIVAEnum;
import com.chemiconsult.enums.ProvinciaEnum;
import com.chemiconsult.enums.TipoClienteEnum;
import lombok.Data;

@Data
public class ClienteTO {

    // ── Tipo ──
    private TipoClienteEnum tipoCliente;

    // ── Persona Física ──
    private String nombre;
    private String apellido;
    private String dni;
    private String cuil;

    // ── Empresa ──
    private String razonSocial;
    private String cuit;

    // ── Compartidos ──
    private String email;
    private String telefono;
    private String celular;
    private String direccion;
    private String localidad;
    private ProvinciaEnum provincia;
    private CondicionIVAEnum condicionIVA;
}
