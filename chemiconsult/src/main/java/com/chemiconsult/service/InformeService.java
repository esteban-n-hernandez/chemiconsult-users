package com.chemiconsult.service;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.mapper.EstudiosMapper;
import com.chemiconsult.repository.AnalisisRepository;
import com.chemiconsult.supabase.service.SupabaseBucketService;
import com.chemiconsult.to.AnalisisDetalleTO;
import com.chemiconsult.to.LimiteAplicableTO;
import com.chemiconsult.to.ParametroResultadoTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
@Service
public class InformeService {

    private static final String BUCKET = "chemiconsult-bucket";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String LAB_NOMBRE    = "Laboratorio Chemiconsult";
    private static final String LAB_DIRECCION = "San Isidro, Buenos Aires";
    private static final String LAB_TELEFONOS = "4723 5698 / 11 5869 2444";
    private static final String LAB_EMAIL1    = "info@chemiconsult.com.ar";
    private static final String LAB_EMAIL2    = "chemiconsult.secretaria@gmail.com";

    private static final String[] CERT_LINES = {
        "Laboratorio certificado por el Consejo de Fiscalizacion de laboratorios (COFILAB)",
        "Habilitado por el Consejo Profesional de Quimica N°1908009",
        "Habilitado por el Organismo provincial para el desarrollo sostenible (OPDS) N°26",
        "Inscripto en RELADA N°29",
        "Inscripto en el ROLA (Provincia De Cordoba) N°16"
    };

    private static final String FIRMA_NOMBRE    = "Laura Bertello";
    private static final String FIRMA_TITULO    = "Dra. en Quimica";
    private static final String FIRMA_MATRICULA = "M. 4763";

    private final AnalisisRepository analisisRepository;
    private final SupabaseBucketService supabaseBucketService;

    @Autowired
    public InformeService(AnalisisRepository analisisRepository,
                          SupabaseBucketService supabaseBucketService) {
        this.analisisRepository = analisisRepository;
        this.supabaseBucketService = supabaseBucketService;
    }

    @Transactional
    public byte[] generarYPublicar(Long analisisId) {
        AnalisisDE analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Muestra no encontrada"));

        AnalisisDetalleTO detalle = EstudiosMapper.mapEntityToDetalleTO(analisis);

        List<String> sinResultado = detalle.getParametros() == null ? List.of() :
                detalle.getParametros().stream()
                        .filter(p -> p.getValorResultado() == null || p.getValorResultado().isBlank())
                        .map(ParametroResultadoTO::getNombre)
                        .toList();

        if (!sinResultado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los siguientes parámetros no tienen resultado cargado: " + String.join(", ", sinResultado));
        }

        byte[] pdfBytes;
        try {
            pdfBytes = buildPdf(detalle);
        } catch (Exception e) {
            log.error("Error generando PDF para analisis {}", analisisId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar el informe: " + e.getMessage());
        }

        String nro = detalle.getNroProtocolo() != null ? detalle.getNroProtocolo() : String.valueOf(analisisId);
        String path = analisisId + "/informe-" + nro + ".pdf";
        supabaseBucketService.subirArchivoBytes(BUCKET, path, pdfBytes);

        analisis.setArchivoUrl(path);
        analisis.setEstado(EstadoMuestraEnum.COMPLETO);
        analisis.setUpdateDate(LocalDate.now());
        analisisRepository.save(analisis);

        return pdfBytes;
    }

    // ----------------------------------------------------------------
    // PDF construction
    // ----------------------------------------------------------------

    private byte[] buildPdf(AnalisisDetalleTO d) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 110, 72);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        byte[] logoBytes = loadResource("/static/img/LogoTransparente.png");
        writer.setPageEvent(new HeaderFooterEvento(logoBytes));

        doc.open();
        addContenido(doc, d);
        doc.close();

        return baos.toByteArray();
    }

    private void addContenido(Document doc, AnalisisDetalleTO d) throws Exception {
        Font fTitulo    = new Font(Font.HELVETICA, 14, Font.BOLD | Font.UNDERLINE);
        Font fLabel     = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fValor     = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font fSeccion   = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font fSubseccion= new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fNota      = new Font(Font.HELVETICA, 8, Font.NORMAL);
        Font fConcl     = new Font(Font.HELVETICA, 10, Font.NORMAL);

        // Título centrado y subrayado
        Paragraph titulo = new Paragraph("INFORME DE ANALISIS", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(14);
        doc.add(titulo);

        // Fecha de emisión alineada a la derecha
        Paragraph fechaLine = new Paragraph();
        fechaLine.setAlignment(Element.ALIGN_RIGHT);
        fechaLine.add(new Chunk("Fecha de emision: ", fLabel));
        fechaLine.add(new Chunk(LocalDate.now().format(FMT), fValor));
        fechaLine.setSpacingAfter(10);
        doc.add(fechaLine);

        // Campos con línea separadora bajo cada uno
        addCampoConLinea(doc, "Cliente", upper(d.getCliente()), fLabel, fValor);
        addCampoConLinea(doc, "Punto de muestreo", nvl(d.getPuntoMuestreo()), fLabel, fValor);
        if (d.getTipoMuestraNombre() != null) {
            addCampoConLinea(doc, "Tipo de muestra", d.getTipoMuestraNombre(), fLabel, fValor);
        }
        addCampoConLinea(doc, "Protocolo de analisis", "N°" + nvl(d.getNroProtocolo()), fLabel, fValor);
        addCampoConLinea(doc, "Fecha recepcion de la muestra", formatFecha(d.getFechaIngreso()), fLabel, fValor);

        // Separador fuerte
        doc.add(buildSeparator(1f));

        // Encabezado de resultados
        Paragraph secResul = new Paragraph("Resultados", fSeccion);
        secResul.setAlignment(Element.ALIGN_CENTER);
        secResul.setSpacingBefore(10);
        secResul.setSpacingAfter(6);
        doc.add(secResul);

        Paragraph subSec = new Paragraph("Analisis Fisico Quimico", fSubseccion);
        subSec.setSpacingAfter(8);
        doc.add(subSec);

        // Tabla de resultados
        addTabla(doc, d);

        // Notas debajo de la tabla
        addNotas(doc, d, fNota);

        // Párrafo de conclusión
        addConclusion(doc, d, fConcl);

        // Firma
        addFirma(doc);
    }

    private void addCampoConLinea(Document doc, String etiqueta, String valor,
                                   Font fLabel, Font fValor) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta + ": ", fLabel));
        p.add(new Chunk(valor, fValor));
        p.setSpacingAfter(3);
        doc.add(p);
        doc.add(buildSeparator(0.5f));
    }

    private Element buildSeparator(float width) {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorderWidthBottom(width);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setMinimumHeight(4f);
        cell.setBorderColor(new Color(180, 180, 180));
        line.addCell(cell);
        line.setSpacingAfter(4f);
        return line;
    }

    // ----------------------------------------------------------------
    // Tabla de resultados
    // ----------------------------------------------------------------

    private void addTabla(Document doc, AnalisisDetalleTO d) throws DocumentException {
        List<String> resoluciones = d.getResolucionesAplicadas() != null
                ? d.getResolucionesAplicadas() : List.of();

        int numCols = 3 + resoluciones.size() + 1;
        float[] widths = buildWidths(resoluciones.size());

        PdfPTable tabla = new PdfPTable(numCols);
        tabla.setWidthPercentage(100);
        tabla.setWidths(widths);
        tabla.setSpacingBefore(2);
        tabla.setSpacingAfter(6);
        tabla.setHeaderRows(1);

        Font fHeader = new Font(Font.HELVETICA, 9, Font.BOLD);
        Color headerBg = new Color(200, 200, 200);

        addHeaderCell(tabla, "Analito", fHeader, headerBg);
        addHeaderCell(tabla, "Unidad", fHeader, headerBg);
        addHeaderCell(tabla, "Resultados", fHeader, headerBg);
        for (String r : resoluciones) {
            addHeaderCell(tabla, buildLimiteHeader(r), fHeader, headerBg);
        }
        addHeaderCell(tabla, "Metodologia", fHeader, headerBg);

        Font fParam  = new Font(Font.HELVETICA, 9, Font.NORMAL);
        Font fResult = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font fSmall  = new Font(Font.HELVETICA, 8, Font.NORMAL);

        if (d.getParametros() != null) {
            for (ParametroResultadoTO p : d.getParametros()) {
                addDataCell(tabla, nvl(p.getNombre()), fParam, Element.ALIGN_LEFT);
                addDataCell(tabla, nvl(p.getUnidad()), fParam, Element.ALIGN_CENTER);
                addDataCell(tabla, nvl(p.getValorResultado()), fResult, Element.ALIGN_CENTER);
                for (String r : resoluciones) {
                    addDataCell(tabla, findLimite(p.getLimites(), r), fParam, Element.ALIGN_CENTER);
                }
                addDataCell(tabla, nvl(p.getMetodologiaNombre()), fSmall, Element.ALIGN_LEFT);
            }
        }

        doc.add(tabla);
    }

    private float[] buildWidths(int n) {
        return switch (n) {
            case 0 -> new float[]{30f, 8f, 12f, 50f};
            case 1 -> new float[]{24f, 8f, 10f, 25f, 33f};
            case 2 -> new float[]{22f, 7f, 9f, 21f, 21f, 20f};
            case 3 -> new float[]{20f, 6f, 8f, 18f, 18f, 18f, 12f};
            default -> {
                float each = 13f;
                float meta = 100f - 18f - 6f - 7f - each * n;
                float[] w = new float[3 + n + 1];
                w[0] = 18f; w[1] = 6f; w[2] = 7f;
                for (int i = 0; i < n; i++) w[3 + i] = each;
                w[3 + n] = Math.max(meta, 10f);
                yield w;
            }
        };
    }

    private String buildLimiteHeader(String origenNombre) {
        String nombre = origenNombre;
        if (nombre.endsWith(" - Unico") || nombre.endsWith(" - Único")) {
            nombre = nombre.substring(0, nombre.lastIndexOf(" - "));
        }
        return "Limites " + nombre;
    }

    private String findLimite(List<LimiteAplicableTO> limites, String origen) {
        if (limites == null) return "-";
        return limites.stream()
                .filter(l -> origen.equals(l.getOrigenNombre()))
                .findFirst()
                .map(this::formatLimite)
                .orElse("-");
    }

    private String formatLimite(LimiteAplicableTO l) {
        if (l.getTipoLimite() == null) return "-";
        return switch (l.getTipoLimite()) {
            case "MAX"   -> l.getLimiteMax() != null ? fmtNum(l.getLimiteMax()) : "-";
            case "MIN"   -> l.getLimiteMin() != null ? fmtNum(l.getLimiteMin()) : "-";
            case "RANGO" -> (l.getLimiteMin() != null && l.getLimiteMax() != null)
                    ? fmtNum(l.getLimiteMin()) + "-" + fmtNum(l.getLimiteMax()) : "-";
            case "TEXTO" -> l.getLimiteTexto() != null ? l.getLimiteTexto() : "-";
            default      -> "-";
        };
    }

    private String fmtNum(Double v) {
        if (v == null) return "-";
        if (v == Math.floor(v) && !Double.isInfinite(v)) return String.valueOf(v.longValue());
        return String.valueOf(v).replace(".", ",");
    }

    // ----------------------------------------------------------------
    // Notas, conclusión, firma
    // ----------------------------------------------------------------

    private void addNotas(Document doc, AnalisisDetalleTO d, Font fNota) throws DocumentException {
        List<String> resoluciones = d.getResolucionesAplicadas() != null
                ? d.getResolucionesAplicadas() : List.of();

        if (resoluciones.stream().anyMatch(r -> r.startsWith("CAA"))) {
            doc.add(new Paragraph("CAA: Codigo Alimentario Argentino", fNota));
        }
        if (resoluciones.stream().anyMatch(r -> r.startsWith("Ley 19587"))) {
            Paragraph nota = new Paragraph(
                    "*segun Ley 19587 - Decreto 351/79 - VAC: Valor aconsejable / VA: Valor aceptable / LT: Limite tolerable",
                    fNota);
            doc.add(nota);
        }
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(4);
        doc.add(espacio);
    }

    private void addConclusion(Document doc, AnalisisDetalleTO d, Font fConcl) throws DocumentException {
        String texto = (d.getObservaciones() != null && !d.getObservaciones().isBlank())
                ? d.getObservaciones()
                : buildAutoConclusion(d);

        Paragraph p = new Paragraph(texto, fConcl);
        p.setSpacingBefore(6);
        p.setSpacingAfter(30);
        doc.add(p);
    }

    private String buildAutoConclusion(AnalisisDetalleTO d) {
        if (d.getParametros() == null || d.getParametros().isEmpty()) return "";
        List<String> noOk = d.getParametros().stream()
                .filter(p -> p.getLimites() != null &&
                        p.getLimites().stream().anyMatch(l -> Boolean.FALSE.equals(l.getCumple())))
                .map(ParametroResultadoTO::getNombre)
                .toList();

        if (noOk.isEmpty()) {
            return "Respecto a los parámetros analizados, los resultados cumplen con la normativa aplicable.";
        }
        return "Respecto a los parámetros analizados, se detectaron incumplimientos en: "
                + String.join(", ", noOk) + ".";
    }

    private void addFirma(Document doc) throws DocumentException {
        Font fFirma = new Font(Font.HELVETICA, 9, Font.NORMAL);

        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(35);
        tabla.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell espacio = new PdfPCell(new Phrase(" "));
        espacio.setFixedHeight(40f);
        espacio.setBorderWidthTop(0);
        espacio.setBorderWidthLeft(0);
        espacio.setBorderWidthRight(0);
        espacio.setBorderWidthBottom(0.8f);
        espacio.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(espacio);

        for (String linea : new String[]{FIRMA_NOMBRE, FIRMA_TITULO, FIRMA_MATRICULA}) {
            PdfPCell cell = new PdfPCell(new Phrase(linea, fFirma));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }

        doc.add(tabla);
    }

    // ----------------------------------------------------------------
    // Table cell helpers
    // ----------------------------------------------------------------

    private void addHeaderCell(PdfPTable tabla, String texto, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        tabla.addCell(cell);
    }

    private void addDataCell(PdfPTable tabla, String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        tabla.addCell(cell);
    }

    // ----------------------------------------------------------------
    // Utilities
    // ----------------------------------------------------------------

    private String nvl(String s) { return s != null && !s.isBlank() ? s : "-"; }
    private String upper(String s) { return s != null ? s.toUpperCase() : "-"; }

    private String formatFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) return "-";
        try { return LocalDate.parse(fechaStr).format(FMT); } catch (Exception e) { return fechaStr; }
    }

    private byte[] loadResource(String path) throws Exception {
        try (InputStream is = InformeService.class.getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Recurso no encontrado: " + path);
            return is.readAllBytes();
        }
    }

    // ================================================================
    // Page event: header y footer en cada página
    // ================================================================

    private static class HeaderFooterEvento extends PdfPageEventHelper {

        private final byte[] logoBytes;
        private PdfTemplate tplTotal;
        private BaseFont bf;
        private int ultimaPagina = 1;

        HeaderFooterEvento(byte[] logoBytes) {
            this.logoBytes = logoBytes;
        }

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
                throw new RuntimeException("Error dibujando página", e);
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
            Rectangle ps = doc.getPageSize();
            float left  = doc.left();
            float right = doc.right();
            float top   = ps.getTop();

            // Logo en esquina superior izquierda
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(115, 52);
            logo.setAbsolutePosition(left, top - 10 - logo.getScaledHeight());
            cb.addImage(logo);

            // Datos de contacto alineados a la derecha
            float textTop = top - 10;
            float lineH   = 11f;
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            showRight(cb, LAB_NOMBRE,    right, textTop);
            showRight(cb, LAB_DIRECCION, right, textTop - lineH);
            showRight(cb, LAB_TELEFONOS, right, textTop - lineH * 2);
            showRight(cb, LAB_EMAIL1,    right, textTop - lineH * 3);
            showRight(cb, LAB_EMAIL2,    right, textTop - lineH * 4);
            cb.endText();

            // Línea separadora bajo el encabezado (dentro del margen superior)
            cb.setLineWidth(0.5f);
            cb.setColorStroke(new Color(150, 150, 150));
            cb.moveTo(left, doc.top() + 8);  // justo encima del área de contenido
            cb.lineTo(right, doc.top() + 8);
            cb.stroke();
            cb.setColorStroke(Color.BLACK);
        }

        private void drawFooter(PdfWriter writer, Document doc) throws Exception {
            PdfContentByte cb = writer.getDirectContent();
            float left  = doc.left();
            float right = doc.right();

            // Tope del área de pie = bottom del contenido
            float footerTop = doc.bottom();   // e.g. 72 para margen inferior de 72pt

            // Línea separadora en el tope del pie
            cb.setLineWidth(0.5f);
            cb.setColorStroke(new Color(150, 150, 150));
            cb.moveTo(left, footerTop - 1);
            cb.lineTo(right, footerTop - 1);
            cb.stroke();
            cb.setColorStroke(Color.BLACK);

            // Texto de certificación: 5 líneas de 8pt
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

            // Número de página alineado a la derecha en la última línea
            float pagY = footerTop - 12 - lineH * 4;
            int pageNum = writer.getPageNumber();
            String pagText = "Pagina " + pageNum + " de ";
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
