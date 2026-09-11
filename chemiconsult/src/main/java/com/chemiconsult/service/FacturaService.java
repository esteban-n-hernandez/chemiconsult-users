package com.chemiconsult.service;

import com.chemiconsult.entity.FacturaDE;
import com.chemiconsult.entity.FacturaItemDE;
import com.chemiconsult.enums.CondicionIVAEnum;
import com.chemiconsult.enums.FacturaEstadoEnum;
import com.chemiconsult.enums.TipoComprobanteEnum;
import com.chemiconsult.repository.ClienteRepository;
import com.chemiconsult.repository.FacturaRepository;
import com.chemiconsult.supabase.service.SupabaseBucketService;
import com.chemiconsult.to.FacturaItemTO;
import com.chemiconsult.to.FacturaResumenTO;
import com.chemiconsult.to.FacturaSolicitudTO;
import com.chemiconsult.wsmtxca.*;
import org.springframework.web.multipart.MultipartFile;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class FacturaService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String BUCKET = "chemiconsult-bucket";

    private static final String LAB_NOMBRE    = "Laboratorio Chemiconsult";
    private static final String LAB_CUIT      = "30-71234567-0";
    private static final String LAB_DIRECCION = "San Isidro, Buenos Aires";
    private static final String LAB_TELEFONOS = "4723 5698 / 11 5869 2444";
    private static final String LAB_EMAIL     = "info@chemiconsult.com.ar";
    private static final String LAB_IVA       = "IVA Responsable Inscripto";

    private static final String[] CERT_LINES = {
            "Laboratorio certificado por el Consejo de Fiscalizacion de laboratorios (COFILAB)",
            "Habilitado por el Consejo Profesional de Quimica N°1908009",
            "Habilitado por el Organismo provincial para el desarrollo sostenible (OPDS) N°26",
    };

    private final FacturaRepository    facturaRepository;
    private final ClienteRepository    clienteRepository;
    private final WsmtxcaPort          wsmtxcaPort;
    private final SupabaseBucketService supabaseBucketService;

    public FacturaService(FacturaRepository facturaRepository, ClienteRepository clienteRepository,
                          WsmtxcaPort wsmtxcaPort, SupabaseBucketService supabaseBucketService) {
        this.facturaRepository    = facturaRepository;
        this.clienteRepository    = clienteRepository;
        this.wsmtxcaPort          = wsmtxcaPort;
        this.supabaseBucketService = supabaseBucketService;
    }

    public record EmitirResult(long id, long numero, String cae, LocalDate caeFechaVencimiento, boolean autorizada) {}
    public record PdfFactura(byte[] pdf, long numero, int puntoVenta, TipoComprobanteEnum tipo) {}
    public record ArchivoFactura(byte[] datos, String nombre) {}

    // ── Emitir ──

    @Transactional
    public EmitirResult emitir(FacturaSolicitudTO req) {
        if (req.getTipoComprobante() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de comprobante requerido");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La factura debe tener al menos un ítem");
        }
        if (req.getClienteNombre() == null || req.getClienteNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre del receptor requerido");
        }

        int                 puntoVenta = req.getPuntoVenta() != null ? req.getPuntoVenta() : 1;
        TipoComprobanteEnum tipo       = req.getTipoComprobante();

        // Calcular totales
        double subtotal = 0, totalIva = 0;
        for (FacturaItemTO item : req.getItems()) {
            double cant  = nvd(item.getCantidad(), 1);
            double pu    = nvd(item.getPrecioUnitario(), 0);
            double aliq  = nvd(item.getAlicuotaIva(), 0);
            subtotal += cant * pu;
            totalIva += cant * pu * aliq / 100;
        }
        double total = subtotal + totalIva;

        // Agrupar IVA por alícuota (para el desglose en ARCA)
        Map<Double, double[]> ivaAcum = new LinkedHashMap<>();
        for (FacturaItemTO item : req.getItems()) {
            double cant  = nvd(item.getCantidad(), 1);
            double pu    = nvd(item.getPrecioUnitario(), 0);
            double aliq  = nvd(item.getAlicuotaIva(), 0);
            ivaAcum.computeIfAbsent(aliq, k -> new double[]{0, 0});
            ivaAcum.get(aliq)[0] += cant * pu;
            ivaAcum.get(aliq)[1] += cant * pu * aliq / 100;
        }
        List<AlicuotaIvaItem> alicuotas = ivaAcum.entrySet().stream()
                .map(e -> new AlicuotaIvaItem(codigoAlicuota(e.getKey()),
                        round(e.getValue()[0]), round(e.getValue()[1])))
                .collect(Collectors.toList());

        // Determinar tipo/número de doc del receptor
        String docNroCrudo = req.getClienteCuit() != null ? req.getClienteCuit().replaceAll("[^0-9]", "") : "";
        int    docTipo     = docNroCrudo.isEmpty() ? 99 : 80;
        String docNro      = docNroCrudo.isEmpty() ? "0" : docNroCrudo;

        // Obtener siguiente número (máx entre ARCA y DB)
        long ultimoArca = wsmtxcaPort.getUltimoComprobanteAutorizado(puntoVenta, tipo);
        long ultimoBD   = facturaRepository.findUltimoNumero(puntoVenta, tipo);
        long numero     = Math.max(ultimoArca, ultimoBD) + 1;

        LocalDate fecha = req.getFechaEmision() != null ? req.getFechaEmision() : LocalDate.now();

        CaeSolicitud solicitud = new CaeSolicitud(
                puntoVenta, tipo,
                2, docTipo, docNro,
                numero, numero, fecha,
                round(total), round(subtotal), round(totalIva),
                alicuotas
        );

        CaeResponse caeResponse = wsmtxcaPort.solicitarCAE(solicitud);

        // Persistir
        FacturaDE factura = new FacturaDE();
        factura.setNumero(numero);
        factura.setPuntoVenta(puntoVenta);
        factura.setTipoComprobante(tipo);
        factura.setFechaEmision(fecha);
        factura.setClienteId(req.getClienteId());
        factura.setClienteNombre(req.getClienteNombre().toUpperCase());
        factura.setClienteCuit(req.getClienteCuit());
        factura.setClienteDireccion(req.getClienteDireccion());
        factura.setClienteCondicionIVA(req.getClienteCondicionIVA());
        factura.setSubtotal(round(subtotal));
        factura.setTotalIva(round(totalIva));
        factura.setTotal(round(total));
        factura.setPresupuestoId(req.getPresupuestoId());

        if (caeResponse.autorizado()) {
            factura.setCae(caeResponse.cae());
            factura.setCaeFechaVencimiento(caeResponse.caeFechaVencimiento());
            factura.setEstado(FacturaEstadoEnum.AUTORIZADA);
        } else {
            factura.setEstado(FacturaEstadoEnum.RECHAZADA);
            factura.setMensajeError(String.join(", ", caeResponse.observaciones()));
        }

        int orden = 0;
        for (FacturaItemTO itemTO : req.getItems()) {
            double cant = nvd(itemTO.getCantidad(), 1);
            double pu   = nvd(itemTO.getPrecioUnitario(), 0);
            double aliq = nvd(itemTO.getAlicuotaIva(), 0);
            double sub  = cant * pu;
            double iva  = sub * aliq / 100;

            FacturaItemDE item = new FacturaItemDE();
            item.setFactura(factura);
            item.setOrden(orden++);
            item.setDescripcion(itemTO.getDescripcion());
            item.setCantidad(cant);
            item.setPrecioUnitario(pu);
            item.setAlicuotaIva(aliq);
            item.setSubtotal(round(sub));
            item.setImporteIva(round(iva));
            item.setTotal(round(sub + iva));
            factura.getItems().add(item);
        }

        FacturaDE saved = facturaRepository.save(factura);
        return new EmitirResult(saved.getId(), numero,
                caeResponse.cae(), caeResponse.caeFechaVencimiento(), caeResponse.autorizado());
    }

    // ── Adjuntar factura externa ──

    @Transactional
    public long adjuntar(Long clienteId, String clienteNombre, String clienteCuit,
                         String clienteDireccion, String condicionIva,
                         String tipo, String fechaEmisionStr,
                         Long numero, Integer puntoVenta, Double total,
                         MultipartFile archivo) {
        if (clienteNombre == null || clienteNombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre del cliente requerido");
        }
        TipoComprobanteEnum tipoEnum = tipo != null ? TipoComprobanteEnum.valueOf(tipo) : TipoComprobanteEnum.B;
        LocalDate fecha = (fechaEmisionStr != null && !fechaEmisionStr.isBlank())
                ? LocalDate.parse(fechaEmisionStr) : LocalDate.now();

        FacturaDE f = new FacturaDE();
        f.setEsExterna(true);
        f.setClienteId(clienteId);
        f.setClienteNombre(clienteNombre.toUpperCase());
        f.setClienteCuit(clienteCuit);
        f.setClienteDireccion(clienteDireccion);
        f.setClienteCondicionIVA(condicionIva != null ? CondicionIVAEnum.valueOf(condicionIva) : null);
        f.setTipoComprobante(tipoEnum);
        f.setFechaEmision(fecha);
        f.setNumero(numero != null ? numero : 0L);
        f.setPuntoVenta(puntoVenta != null ? puntoVenta : 0);
        f.setTotal(total);
        f.setSubtotal(null);
        f.setTotalIva(null);
        f.setEstado(FacturaEstadoEnum.AUTORIZADA);

        if (archivo != null && !archivo.isEmpty()) {
            f.setArchivoNombre(archivo.getOriginalFilename());
        }

        long id = facturaRepository.save(f).getId();

        if (archivo != null && !archivo.isEmpty()) {
            supabaseBucketService.subirArchivo(BUCKET, "facturas/" + id + ".pdf", archivo);
        }

        return id;
    }

    // ── Listar ──

    @Transactional(readOnly = true)
    public List<FacturaResumenTO> listar() {
        return facturaRepository.findAllByOrderByFechaEmisionDescNumeroDesc()
                .stream().map(this::toResumen).toList();
    }

    // ── Listar por cliente ──

    @Transactional(readOnly = true)
    public List<FacturaResumenTO> listarPorCliente(Long userId) {
        return clienteRepository.findByUser_Id(userId)
                .map(c -> facturaRepository.findByClienteIdOrderByFechaEmisionDescNumeroDesc(c.getId())
                        .stream().map(this::toResumen).toList())
                .orElse(List.of());
    }

    // ── Obtener archivo adjunto ──

    public ArchivoFactura getArchivo(Long id) {
        FacturaDE f = findOrThrow(id);
        if (!f.isEsExterna() || f.getArchivoNombre() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esta factura no tiene archivo adjunto");
        }
        byte[] datos = supabaseBucketService.descargarArchivo(BUCKET, "facturas/" + id + ".pdf");
        return new ArchivoFactura(datos, f.getArchivoNombre());
    }

    // ── Detalle ──

    @Transactional(readOnly = true)
    public FacturaResumenTO getDetalle(Long id) {
        return toResumen(findOrThrow(id));
    }

    // ── Anular ──

    @Transactional
    public void anular(Long id) {
        FacturaDE factura = findOrThrow(id);
        if (factura.getEstado() == FacturaEstadoEnum.ANULADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La factura ya está anulada");
        }
        factura.setEstado(FacturaEstadoEnum.ANULADA);
    }

    // ── PDF ──

    @Transactional(readOnly = true)
    public PdfFactura getPdf(Long id) {
        FacturaDE factura = findOrThrow(id);
        try {
            return new PdfFactura(buildPdf(factura), factura.getNumero(), factura.getPuntoVenta(), factura.getTipoComprobante());
        } catch (Exception e) {
            log.error("Error generando PDF factura {}", factura.getNumero(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el PDF: " + e.getMessage());
        }
    }

    // ── Helper ──

    private FacturaDE findOrThrow(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));
    }

    // ── Mapper ──

    private FacturaResumenTO toResumen(FacturaDE e) {
        FacturaResumenTO r = new FacturaResumenTO();
        r.setId(e.getId());
        r.setNumero(e.getNumero());
        r.setPuntoVenta(e.getPuntoVenta());
        r.setTipoComprobante(e.getTipoComprobante());
        r.setFechaEmision(e.getFechaEmision());
        r.setClienteId(e.getClienteId());
        r.setClienteNombre(e.getClienteNombre());
        r.setClienteCuit(e.getClienteCuit());
        r.setClienteCondicionIVA(e.getClienteCondicionIVA());
        r.setSubtotal(e.getSubtotal());
        r.setTotalIva(e.getTotalIva());
        r.setTotal(e.getTotal());
        r.setCae(e.getCae());
        r.setCaeFechaVencimiento(e.getCaeFechaVencimiento());
        r.setEstado(e.getEstado());
        r.setMensajeError(e.getMensajeError());
        r.setCreatedAt(e.getCreatedAt());
        r.setEsExterna(e.isEsExterna());
        r.setArchivoNombre(e.getArchivoNombre());
        if (e.getItems() != null) {
            r.setItems(e.getItems().stream().map(i -> {
                FacturaResumenTO.ItemTO it = new FacturaResumenTO.ItemTO();
                it.setDescripcion(i.getDescripcion());
                it.setCantidad(i.getCantidad());
                it.setPrecioUnitario(i.getPrecioUnitario());
                it.setAlicuotaIva(i.getAlicuotaIva());
                it.setSubtotal(i.getSubtotal());
                it.setImporteIva(i.getImporteIva());
                it.setTotal(i.getTotal());
                return it;
            }).toList());
        }
        return r;
    }

    // ================================================================
    // PDF — diseño de factura electrónica argentina
    // ================================================================

    private byte[] buildPdf(FacturaDE f) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 110, 70);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        byte[] logoBytes = loadResource("/static/img/LogoTransparente.png");
        writer.setPageEvent(new HeaderFooterEvento(logoBytes));

        doc.open();
        addCuerpo(doc, f);
        doc.close();

        return baos.toByteArray();
    }

    private void addCuerpo(Document doc, FacturaDE f) throws Exception {
        Font fTitulo = new Font(Font.HELVETICA, 13, Font.BOLD);
        Font fSub    = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font fLabel  = new Font(Font.HELVETICA, 9,  Font.BOLD);
        Font fValor  = new Font(Font.HELVETICA, 9,  Font.NORMAL);
        Font fMono   = new Font(Font.COURIER,   8,  Font.NORMAL);
        Font fData   = new Font(Font.HELVETICA, 8,  Font.NORMAL);
        Font fDataB  = new Font(Font.HELVETICA, 8,  Font.BOLD);

        // ── Cabecera: emisor | tipo | receptor ──
        PdfPTable cabecera = new PdfPTable(new float[]{42, 16, 42});
        cabecera.setWidthPercentage(100);
        cabecera.setSpacingAfter(10);

        // Columna izquierda: datos emisor
        PdfPCell cEmisor = new PdfPCell();
        cEmisor.setBorder(Rectangle.BOX);
        cEmisor.setPadding(6);
        cEmisor.addElement(new Paragraph(LAB_NOMBRE, fTitulo));
        cEmisor.addElement(new Paragraph("CUIT: " + LAB_CUIT, fSub));
        cEmisor.addElement(new Paragraph(LAB_IVA, fSub));
        cEmisor.addElement(new Paragraph(LAB_DIRECCION, fSub));
        cEmisor.addElement(new Paragraph(LAB_TELEFONOS, fSub));
        cEmisor.addElement(new Paragraph(LAB_EMAIL, fSub));
        cabecera.addCell(cEmisor);

        // Columna central: tipo de comprobante
        String letraTipo = f.getTipoComprobante().name();  // "A", "B" o "C"
        String nroFmt    = String.format("%04d", f.getPuntoVenta()) + "-" + String.format("%08d", f.getNumero());

        PdfPCell cTipo = new PdfPCell();
        cTipo.setBorder(Rectangle.BOX);
        cTipo.setPadding(4);
        cTipo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cTipo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Font fLetra = new Font(Font.HELVETICA, 36, Font.BOLD);
        Paragraph pLetra = new Paragraph(letraTipo, fLetra);
        pLetra.setAlignment(Element.ALIGN_CENTER);
        cTipo.addElement(pLetra);

        Font fCodDesc = new Font(Font.HELVETICA, 7, Font.NORMAL);
        Paragraph pCod = new Paragraph("Cod. " + f.getTipoComprobante().codigo, fCodDesc);
        pCod.setAlignment(Element.ALIGN_CENTER);
        cTipo.addElement(pCod);

        Paragraph pNro = new Paragraph("N° " + nroFmt, new Font(Font.HELVETICA, 9, Font.BOLD));
        pNro.setAlignment(Element.ALIGN_CENTER);
        pNro.setSpacingBefore(4);
        cTipo.addElement(pNro);

        Paragraph pFecha = new Paragraph("Fecha: " + f.getFechaEmision().format(FMT),
                new Font(Font.HELVETICA, 8, Font.NORMAL));
        pFecha.setAlignment(Element.ALIGN_CENTER);
        cTipo.addElement(pFecha);
        cabecera.addCell(cTipo);

        // Columna derecha: datos receptor
        PdfPCell cReceptor = new PdfPCell();
        cReceptor.setBorder(Rectangle.BOX);
        cReceptor.setPadding(6);

        String condIva = condicionIvaStr(f.getClienteCondicionIVA());
        String docLabel = (f.getClienteCuit() != null && !f.getClienteCuit().isBlank()) ? "CUIT: " : "";

        cReceptor.addElement(new Paragraph("Cliente / Receptor:", fLabel));
        cReceptor.addElement(new Paragraph(nvl(f.getClienteNombre()), new Font(Font.HELVETICA, 9, Font.BOLD)));
        if (!docLabel.isEmpty()) {
            cReceptor.addElement(new Paragraph(docLabel + f.getClienteCuit(), fSub));
        }
        cReceptor.addElement(new Paragraph(condIva, fSub));
        if (f.getClienteDireccion() != null && !f.getClienteDireccion().isBlank()) {
            cReceptor.addElement(new Paragraph(f.getClienteDireccion(), fSub));
        }
        cabecera.addCell(cReceptor);

        doc.add(cabecera);

        // ── Tabla de ítems ──
        PdfPTable tabla = new PdfPTable(new float[]{46, 10, 16, 14, 14});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(6);
        tabla.setHeaderRows(1);

        Color hBg = new Color(226, 239, 217);
        Font fH = new Font(Font.HELVETICA, 8, Font.BOLD | Font.ITALIC);
        addHdr(tabla, "Descripción",    fH, hBg, Element.ALIGN_LEFT);
        addHdr(tabla, "Cant.",          fH, hBg, Element.ALIGN_CENTER);
        addHdr(tabla, "Precio unit.",   fH, hBg, Element.ALIGN_RIGHT);
        addHdr(tabla, "IVA %",          fH, hBg, Element.ALIGN_CENTER);
        addHdr(tabla, "Subtotal",       fH, hBg, Element.ALIGN_RIGHT);

        Color rBg = new Color(242, 248, 235);
        boolean shade = true;
        if (f.getItems() != null) {
            for (FacturaItemDE item : f.getItems()) {
                Color bg = shade ? rBg : Color.WHITE;
                addDat(tabla, nvl(item.getDescripcion()),                      fData,  Element.ALIGN_LEFT,   bg);
                addDat(tabla, fmtCant(item.getCantidad()),                     fData,  Element.ALIGN_CENTER, bg);
                addDat(tabla, formatPrecio(item.getPrecioUnitario()),           fDataB, Element.ALIGN_RIGHT,  bg);
                addDat(tabla, fmtAlicuota(item.getAlicuotaIva()),               fData,  Element.ALIGN_CENTER, bg);
                addDat(tabla, formatPrecio(item.getSubtotal()),                fDataB, Element.ALIGN_RIGHT,  bg);
                shade = !shade;
            }
        }
        doc.add(tabla);

        // ── Totales ──
        PdfPTable totales = new PdfPTable(new float[]{70, 30});
        totales.setWidthPercentage(60);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.setSpacingAfter(10);

        boolean discriminaIva = f.getTipoComprobante() == TipoComprobanteEnum.A;
        if (discriminaIva) {
            addTotalRow(totales, "Subtotal neto:", formatPrecio(f.getSubtotal()), fLabel, fValor);
            addTotalRow(totales, "IVA:",          formatPrecio(f.getTotalIva()),  fLabel, fValor);
        }
        Font fTotalBig = new Font(Font.HELVETICA, 10, Font.BOLD);
        addTotalRow(totales, "TOTAL:", formatPrecio(f.getTotal()), fTotalBig, fTotalBig);
        doc.add(totales);

        // ── CAE ──
        PdfPTable caeTable = new PdfPTable(1);
        caeTable.setWidthPercentage(100);
        caeTable.setSpacingBefore(6);

        PdfPCell cCae = new PdfPCell();
        cCae.setBorder(Rectangle.BOX);
        cCae.setBackgroundColor(new Color(248, 248, 248));
        cCae.setPadding(6);

        Paragraph pCae = new Paragraph();
        pCae.add(new Chunk("CAE: ", fLabel));
        pCae.add(new Chunk(f.getCae() != null ? f.getCae() : "SIN CAE — RECHAZADA", fMono));
        pCae.add(new Chunk("   Vencimiento CAE: ", fLabel));
        pCae.add(new Chunk(f.getCaeFechaVencimiento() != null
                ? f.getCaeFechaVencimiento().format(FMT) : "-", fData));
        cCae.addElement(pCae);

        if (f.getMensajeError() != null) {
            Paragraph pErr = new Paragraph("Observación: " + f.getMensajeError(),
                    new Font(Font.HELVETICA, 7, Font.ITALIC, new Color(150, 0, 0)));
            cCae.addElement(pErr);
        }
        caeTable.addCell(cCae);
        doc.add(caeTable);
    }

    // ── Helpers PDF ──

    private void addHdr(PdfPTable t, String txt, Font f, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(4f);
        t.addCell(c);
    }

    private void addDat(PdfPTable t, String txt, Font f, int align, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_TOP);
        c.setPadding(4f);
        c.setBackgroundColor(bg);
        t.addCell(c);
    }

    private void addTotalRow(PdfPTable t, String label, String valor, Font fL, Font fV) {
        PdfPCell cL = new PdfPCell(new Phrase(label, fL));
        cL.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cL.setPadding(3f);
        cL.setBorder(Rectangle.NO_BORDER);
        t.addCell(cL);

        PdfPCell cV = new PdfPCell(new Phrase(valor, fV));
        cV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cV.setPadding(3f);
        cV.setBorder(Rectangle.NO_BORDER);
        t.addCell(cV);
    }

    private String formatPrecio(Double v) {
        if (v == null) return "$0";
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("es", "AR"));
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        return "$" + fmt.format(v);
    }

    private String fmtCant(Double v) {
        if (v == null) return "1";
        return v % 1 == 0 ? String.valueOf(v.intValue()) : String.valueOf(v);
    }

    private String fmtAlicuota(Double v) {
        if (v == null || v == 0) return "0%";
        return v % 1 == 0 ? v.intValue() + "%" : v + "%";
    }

    private String condicionIvaStr(CondicionIVAEnum c) {
        if (c == null) return "Consumidor Final";
        return switch (c) {
            case RESPONSABLE_INSCRIPTO -> "IVA Responsable Inscripto";
            case MONOTRIBUTISTA        -> "Monotributista";
            case EXENTO                -> "IVA Exento";
            case NO_RESPONSABLE        -> "No Responsable";
            case CONSUMIDOR_FINAL      -> "Consumidor Final";
        };
    }

    private String nvl(String s) { return s != null ? s : "-"; }

    private double nvd(Double v, double def) { return v != null ? v : def; }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }

    private int codigoAlicuota(double alicuota) {
        if (alicuota == 0)    return 3;
        if (alicuota == 10.5) return 4;
        if (alicuota == 27)   return 6;
        return 5; // 21% por defecto
    }

    private byte[] loadResource(String path) throws Exception {
        try (InputStream is = FacturaService.class.getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Recurso no encontrado: " + path);
            return is.readAllBytes();
        }
    }

    // ================================================================
    // Header y footer del PDF
    // ================================================================

    private static class HeaderFooterEvento extends PdfPageEventHelper {

        private final byte[] logoBytes;
        private PdfTemplate  tplTotal;
        private BaseFont     bf;
        private int          ultimaPagina = 1;

        HeaderFooterEvento(byte[] logoBytes) { this.logoBytes = logoBytes; }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            try {
                bf       = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                tplTotal = writer.getDirectContent().createTemplate(30, 10);
            } catch (Exception e) { throw new RuntimeException(e); }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            ultimaPagina = writer.getPageNumber();
            try {
                drawHeader(writer, document);
                drawFooter(writer, document);
            } catch (Exception e) { throw new RuntimeException("Error cabecera/pie", e); }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            tplTotal.beginText();
            tplTotal.setFontAndSize(bf, 7);
            tplTotal.showText(String.valueOf(ultimaPagina));
            tplTotal.endText();
        }

        private void drawHeader(PdfWriter writer, Document doc) throws Exception {
            PdfContentByte cb   = writer.getDirectContent();
            float left          = doc.left();
            float right         = doc.right();
            float top           = doc.getPageSize().getTop();

            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(115, 52);
            logo.setAbsolutePosition(left, top - 30 - logo.getScaledHeight());
            cb.addImage(logo);

            float textTop = top - 30;
            float lineH   = 11f;
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            showRight(cb, "Laboratorio Chemiconsult", right, textTop);
            showRight(cb, "San Isidro, Buenos Aires", right, textTop - lineH);
            showRight(cb, "4723 5698 / 11 5869 2444", right, textTop - lineH * 2);
            showRight(cb, "info@chemiconsult.com.ar",  right, textTop - lineH * 3);
            cb.endText();
        }

        private void drawFooter(PdfWriter writer, Document doc) throws Exception {
            PdfContentByte cb  = writer.getDirectContent();
            float left         = doc.left();
            float right        = doc.right();
            float footerTop    = doc.bottom();

            cb.setLineWidth(0.5f);
            cb.setColorStroke(new Color(150, 150, 150));
            cb.moveTo(left, footerTop - 1);
            cb.lineTo(right, footerTop - 1);
            cb.stroke();
            cb.setColorStroke(Color.BLACK);

            float lineH = 8f;
            float y     = footerTop - 12;
            cb.beginText();
            cb.setFontAndSize(bf, 7);
            for (String linea : CERT_LINES) {
                cb.setTextMatrix(left, y);
                cb.showText(linea);
                y -= lineH;
            }
            cb.endText();

            float pagY    = footerTop - 12;
            String pagTxt = "Pagina " + writer.getPageNumber() + " de ";
            float pagW    = bf.getWidthPoint(pagTxt, 7);
            cb.beginText();
            cb.setFontAndSize(bf, 7);
            cb.setTextMatrix(right - pagW - 32, pagY);
            cb.showText(pagTxt);
            cb.endText();
            cb.addTemplate(tplTotal, right - 32, pagY);
        }

        private void showRight(PdfContentByte cb, String text, float right, float y) throws Exception {
            float w = bf.getWidthPoint(text, 8);
            cb.setTextMatrix(right - w, y);
            cb.showText(text);
        }
    }
}
