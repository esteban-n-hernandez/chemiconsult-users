package com.chemiconsult.service;

import com.chemiconsult.to.ClienteTO;
import com.chemiconsult.to.ItemPresupuestoTO;
import com.chemiconsult.to.PresupuestoTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Log4j2
@Service
public class PresupuestoService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String LAB_NOMBRE    = "Laboratorio Chemiconsult";
    private static final String LAB_DIRECCION = "San Isidro, Buenos Aires";
    private static final String LAB_TELEFONOS = "4723 5698 / 11 5869 2444";
    private static final String LAB_EMAIL1    = "info@chemiconsult.com.ar";
    private static final String LAB_EMAIL2    = "chemiconsult.secretaria@gmail.com";
    private static final String FIRMANTE      = "Dra. Laura Bertello";

    private static final String[] CERT_LINES = {
            "Laboratorio certificado por el Consejo de Fiscalizacion de laboratorios (COFILAB)",
            "Habilitado por el Consejo Profesional de Quimica N°1908009",
            "Habilitado por el Organismo provincial para el desarrollo sostenible (OPDS) N°26",
            "Inscripto en RELADA N°29",
            "Inscripto en el ROLA (Provincia De Cordoba) N°16",
    };

    private static final List<String> OBS_DEFAULT = List.of(
            "Validez de la oferta: 30 días.",
            "Los precios indicados NO incluyen IVA.",
            "Los precios indicados NO incluyen el muestreo."
    );

    public byte[] generatePresupuesto(PresupuestoTO to) {
        try {
            return buildPdf(to);
        } catch (Exception e) {
            log.error("Error generando presupuesto {}", to.getNumeroPresupuesto(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el presupuesto: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // PDF construction
    // ----------------------------------------------------------------

    private byte[] buildPdf(PresupuestoTO to) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 110, 72);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        byte[] logoBytes = loadResource("/static/img/LogoTransparente.png");
        writer.setPageEvent(new HeaderFooterEvento(logoBytes));

        doc.open();
        addContenido(doc, to);
        doc.close();

        return baos.toByteArray();
    }

    private void addContenido(Document doc, PresupuestoTO to) throws Exception {
        Font fTitulo    = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font fNumero    = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font fFecha     = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Font fFechaLbl  = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font fLabel     = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fValor     = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font fCuerpo    = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font fObsTitle  = new Font(Font.HELVETICA, 10, Font.BOLD | Font.UNDERLINE);
        Font fObs       = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font fFirmante  = new Font(Font.HELVETICA, 10, Font.BOLD);

        // ── Título ──
        Paragraph titulo = new Paragraph("PRESUPUESTO", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(2);
        doc.add(titulo);

        String nroStr = to.getNumeroPresupuesto() != null
                ? String.format("N°%07d", to.getNumeroPresupuesto())
                : "N°-------";
        Paragraph nro = new Paragraph(nroStr, fNumero);
        nro.setAlignment(Element.ALIGN_CENTER);
        nro.setSpacingAfter(6);
        doc.add(nro);

        // ── Fecha ──
        LocalDate fecha = to.getFecha() != null ? to.getFecha() : LocalDate.now();
        Paragraph fechaP = new Paragraph();
        fechaP.setAlignment(Element.ALIGN_CENTER);
        fechaP.add(new Chunk("Fecha: ", fFechaLbl));
        fechaP.add(new Chunk(fecha.format(FMT), fFecha));
        fechaP.setSpacingAfter(14);
        doc.add(fechaP);

        // ── Datos del cliente ──
        String nombreCliente = resolverNombreCliente(to.getCliente());
        Paragraph clienteP = new Paragraph();
        clienteP.add(new Chunk("Cliente: ", fLabel));
        clienteP.add(new Chunk(nombreCliente, fValor));
        clienteP.setSpacingAfter(3);
        doc.add(clienteP);

        if (to.getSolicitadoPor() != null && !to.getSolicitadoPor().isBlank()) {
            Paragraph solicitadoP = new Paragraph();
            solicitadoP.add(new Chunk("Solicitado por: ", fLabel));
            solicitadoP.add(new Chunk(to.getSolicitadoPor(), fValor));
            solicitadoP.setSpacingAfter(10);
            doc.add(solicitadoP);
        }

        doc.add(new Paragraph("De mi mayor consideración,", fCuerpo));

        Paragraph intro = new Paragraph(
                "Envío cotización solicitada por los siguientes análisis:", fCuerpo);
        intro.setSpacingBefore(8);
        intro.setSpacingAfter(8);
        doc.add(intro);

        // ── Tabla ──
        addTabla(doc, to.getItems());

        // ── Observaciones ──
        Paragraph obsTitle = new Paragraph("Observaciones", fObsTitle);
        obsTitle.setSpacingBefore(10);
        obsTitle.setSpacingAfter(4);
        doc.add(obsTitle);

        com.lowagie.text.List listObs = new com.lowagie.text.List(false);
        listObs.setListSymbol(new Chunk("• ", fObs));
        listObs.setIndentationLeft(16f);
        for (String obs : OBS_DEFAULT) {
            listObs.add(new ListItem(obs, fObs));
        }
        doc.add(listObs);

        // ── Cierre ──
        Paragraph cierre = new Paragraph(
                "Esperando que sea de su interés y quedando a su entera disposición, lo saluda muy atentamente,",
                fCuerpo);
        cierre.setSpacingBefore(16);
        cierre.setSpacingAfter(6);
        doc.add(cierre);

        // ── Firma ──
        addFirma(doc, fFirmante);
    }

    // ----------------------------------------------------------------
    // Tabla de ítems
    // ----------------------------------------------------------------

    private void addTabla(Document doc, java.util.List<ItemPresupuestoTO> items) throws DocumentException {
        // Columnas: Matriz | Determinación | Precio unitario | Cantidad muestras | Precio total
        PdfPTable tabla = new PdfPTable(new float[]{12, 46, 14, 14, 14});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(2);
        tabla.setSpacingAfter(10);
        tabla.setHeaderRows(1);

        Font fHeader = new Font(Font.HELVETICA, 9, Font.BOLD | Font.ITALIC);
        Color headerBg = new Color(226, 239, 217);

        addHeaderCell(tabla, "Matriz",            fHeader, headerBg, Element.ALIGN_CENTER);
        addHeaderCell(tabla, "Determinación", fHeader, headerBg, Element.ALIGN_CENTER);
        addHeaderCell(tabla, "Precio\nunitario",  fHeader, headerBg, Element.ALIGN_CENTER);
        addHeaderCell(tabla, "Cantidad\nmuestras",fHeader, headerBg, Element.ALIGN_CENTER);
        addHeaderCell(tabla, "Precio total",       fHeader, headerBg, Element.ALIGN_CENTER);

        Font fData  = new Font(Font.HELVETICA, 9, Font.NORMAL);
        Font fMoney = new Font(Font.HELVETICA, 9, Font.BOLD);
        Color rowBg = new Color(242, 248, 235);

        if (items != null) {
            boolean shade = true;
            for (ItemPresupuestoTO item : items) {
                Color bg = shade ? rowBg : Color.WHITE;
                addDataCell(tabla, nvl(item.getMatriz()),        fData,  Element.ALIGN_LEFT,   bg);
                addDataCell(tabla, nvl(item.getDeterminacion()), fData,  Element.ALIGN_LEFT,   bg);
                addDataCell(tabla, formatPrecio(item.getPrecioUnitario()), fMoney, Element.ALIGN_RIGHT, bg);
                addDataCell(tabla, item.getCantidadMuestras() != null
                        ? String.valueOf(item.getCantidadMuestras()) : "1",
                        fData, Element.ALIGN_CENTER, bg);
                addDataCell(tabla, formatPrecio(item.getTotal()), fMoney, Element.ALIGN_RIGHT, bg);
                shade = !shade;
            }
        }

        doc.add(tabla);
    }

    private void addHeaderCell(PdfPTable tabla, String texto, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        tabla.addCell(cell);
    }

    private void addDataCell(PdfPTable tabla, String texto, Font font, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(5f);
        cell.setBackgroundColor(bg);
        tabla.addCell(cell);
    }

    // ----------------------------------------------------------------
    // Firma
    // ----------------------------------------------------------------

    private void addFirma(Document doc, Font fFirmante) throws Exception {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(35);
        tabla.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell celdaFirma;
        try (InputStream is = PresupuestoService.class.getResourceAsStream("/static/img/firma.png")) {
            if (is != null) {
                Image imgFirma = Image.getInstance(is.readAllBytes());
                imgFirma.scaleToFit(120, 50);
                celdaFirma = new PdfPCell(imgFirma, false);
                celdaFirma.setHorizontalAlignment(Element.ALIGN_CENTER);
            } else {
                celdaFirma = new PdfPCell(new Phrase(" "));
                celdaFirma.setFixedHeight(50f);
            }
        }
        celdaFirma.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(celdaFirma);

        PdfPCell celdaNombre = new PdfPCell(new Phrase(FIRMANTE, fFirmante));
        celdaNombre.setHorizontalAlignment(Element.ALIGN_CENTER);
        celdaNombre.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(celdaNombre);

        doc.add(tabla);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private String resolverNombreCliente(ClienteTO c) {
        if (c == null) return "-";
        if (c.getRazonSocial() != null && !c.getRazonSocial().isBlank()) return c.getRazonSocial();
        String nombre = "";
        if (c.getNombre()   != null) nombre += c.getNombre();
        if (c.getApellido() != null) nombre += " " + c.getApellido();
        return nombre.isBlank() ? "-" : nombre.trim().toUpperCase();
    }

    private String formatPrecio(Double precio) {
        if (precio == null) return "$0";
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("es", "AR"));
        fmt.setMaximumFractionDigits(0);
        fmt.setGroupingUsed(true);
        return "$" + fmt.format(precio.longValue());
    }

    private String nvl(String s) {
        return s != null && !s.isBlank() ? s : "-";
    }

    private byte[] loadResource(String path) throws Exception {
        try (InputStream is = PresupuestoService.class.getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Recurso no encontrado: " + path);
            return is.readAllBytes();
        }
    }

    // ================================================================
    // Page event: header y footer (mismo que InformeService)
    // ================================================================

    private static class HeaderFooterEvento extends PdfPageEventHelper {

        private final byte[] logoBytes;
        private PdfTemplate tplTotal;
        private BaseFont bf;
        private int ultimaPagina = 1;

        HeaderFooterEvento(byte[] logoBytes) { this.logoBytes = logoBytes; }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            try {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                tplTotal = writer.getDirectContent().createTemplate(30, 10);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            ultimaPagina = writer.getPageNumber();
            try {
                drawHeader(writer, document);
                drawFooter(writer, document);
            } catch (Exception e) {
                throw new RuntimeException("Error dibujando cabecera/pie", e);
            }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            tplTotal.beginText();
            tplTotal.setFontAndSize(bf, 7);
            tplTotal.showText(String.valueOf(ultimaPagina));
            tplTotal.endText();
        }

        private void drawHeader(PdfWriter writer, Document doc) throws Exception {
            PdfContentByte cb = writer.getDirectContent();
            float left  = doc.left();
            float right = doc.right();
            float top   = doc.getPageSize().getTop();

            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(115, 52);
            logo.setAbsolutePosition(left, top - 30 - logo.getScaledHeight());
            cb.addImage(logo);

            float textTop = top - 30;
            float lineH   = 11f;
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            showRight(cb, LAB_NOMBRE,    right, textTop);
            showRight(cb, LAB_DIRECCION, right, textTop - lineH);
            showRight(cb, LAB_TELEFONOS, right, textTop - lineH * 2);
            showRight(cb, LAB_EMAIL1,    right, textTop - lineH * 3);
            showRight(cb, LAB_EMAIL2,    right, textTop - lineH * 4);
            cb.endText();
        }

        private void drawFooter(PdfWriter writer, Document doc) throws Exception {
            PdfContentByte cb = writer.getDirectContent();
            float left      = doc.left();
            float right     = doc.right();
            float footerTop = doc.bottom();

            cb.setLineWidth(0.5f);
            cb.setColorStroke(new Color(150, 150, 150));
            cb.moveTo(left, footerTop - 1);
            cb.lineTo(right, footerTop - 1);
            cb.stroke();
            cb.setColorStroke(Color.BLACK);

            float lineH = 8f;
            float y = footerTop - 12;
            cb.beginText();
            cb.setFontAndSize(bf, 7);
            for (String linea : CERT_LINES) {
                cb.setTextMatrix(left, y);
                cb.showText(linea);
                y -= lineH;
            }
            cb.endText();

            float pagY = footerTop - 12 - lineH * 4;
            String pagText = "Pagina " + writer.getPageNumber() + " de ";
            float pagW = bf.getWidthPoint(pagText, 7);
            cb.beginText();
            cb.setFontAndSize(bf, 7);
            cb.setTextMatrix(right - pagW - 32, pagY);
            cb.showText(pagText);
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
