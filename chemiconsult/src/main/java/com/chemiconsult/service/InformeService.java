package com.chemiconsult.service;

import com.chemiconsult.entity.AnalisisArchivoDE;
import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.enums.EstadoMuestraEnum;
import com.chemiconsult.mapper.EstudiosMapper;
import com.chemiconsult.repository.AnalisisArchivoRepository;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class InformeService {

    private static final String BUCKET = "chemiconsult-bucket";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String LAB_NOMBRE = "Laboratorio Chemiconsult";
    private static final String LAB_DIRECCION = "San Isidro, Buenos Aires";
    private static final String LAB_TELEFONOS = "4723 5698 / 11 5869 2444";
    private static final String LAB_EMAIL1 = "info@chemiconsult.com.ar";
    private static final String LAB_EMAIL2 = "chemiconsult.secretaria@gmail.com";

    private static final String[] CERT_LINES = {
            "Laboratorio certificado por el Consejo de Fiscalizacion de laboratorios (COFILAB)",
            "Habilitado por el Consejo Profesional de Quimica N°1908009",
            "Habilitado por el Organismo provincial para el desarrollo sostenible (OPDS) N°26",
            "Inscripto en RELADA N°29",
            "Inscripto en el ROLA (Provincia De Cordoba) N°16",
    };

    private static final String NOTA_ARSENICO =
            "+En aquellas regiones del país con suelos de alto contenido de arsénico, " +
            "la autoridad sanitaria competente podrá admitir valores mayores a 0,01 mg/l con un límite " +
            "máximo de 0,05 mg/l cuando la composición normal del agua de la zona y la imposibilidad " +
            "de aplicar tecnologías de corrección lo hicieran necesario; ello hasta contar con los " +
            "resultados del estudio “Hidroarsenicismo y Saneamiento Básico en la República Argentina " +
            "– Estudios básicos para el establecimiento de criterios y prioridades sanitarias en " +
            "cobertura y calidad de aguas”, cuyos términos fueron elaborados por la Coordinación " +
            "Políticas Socioambientales de la entonces Secretaría de Gobierno de Salud del entonces " +
            "Ministerio de Salud y Desarrollo Social y ex Secretaría de Infraestructura y Política " +
            "Hídrica del entonces Ministerio del Interior, Obras Públicas y Vivienda. La Comisión " +
            "Nacional de Alimentos deberá recomendar el límite máximo admitido para dichas " +
            "regiones del país en base a los estudios antes referidos.";

    private final AnalisisRepository analisisRepository;
    private final AnalisisArchivoRepository analisisArchivoRepository;
    private final SupabaseBucketService supabaseBucketService;

    @Autowired
    public InformeService(AnalisisRepository analisisRepository,
                          AnalisisArchivoRepository analisisArchivoRepository,
                          SupabaseBucketService supabaseBucketService) {
        this.analisisRepository = analisisRepository;
        this.analisisArchivoRepository = analisisArchivoRepository;
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
        String nombreArchivo = "informe-" + nro + ".pdf";
        String path = analisisId + "/" + nombreArchivo;
        supabaseBucketService.subirArchivoBytes(BUCKET, path, pdfBytes);

        AnalisisArchivoDE archivo = new AnalisisArchivoDE();
        archivo.setAnalisis(analisis);
        archivo.setArchivoUrl(path);
        archivo.setNombre(nombreArchivo);
        archivo.setCreatedAt(LocalDate.now());
        analisisArchivoRepository.save(archivo);

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
        Font fTitulo = new Font(Font.HELVETICA, 14, Font.BOLD | Font.UNDERLINE);
        Font fLabel = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fValor = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font fSeccion = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font fSubseccion = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font fNota = new Font(Font.HELVETICA, 8, Font.NORMAL);
        Font fConcl = new Font(Font.HELVETICA, 10, Font.NORMAL);

        // Título centrado y subrayado
        Paragraph titulo = new Paragraph("INFORME DE ANALISIS", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(14);
        doc.add(titulo);

        // Fecha de emisión alineada a la derecha
        Paragraph fechaLine = new Paragraph();
        fechaLine.setAlignment(Element.ALIGN_RIGHT);
        fechaLine.add(new Chunk("Fecha de emisión: ", fLabel));
        fechaLine.add(new Chunk(LocalDate.now().format(FMT), fValor));
        fechaLine.setSpacingAfter(10);
        doc.add(fechaLine);

        addCampoConLinea(doc, "Cliente", upper(d.getCliente()), fLabel, fValor);
        String labelMuestra = (d.getTipoMuestraNombre() != null && !d.getTipoMuestraNombre().isBlank())
                ? d.getTipoMuestraNombre() : d.getMatrizNombre();
        if (labelMuestra != null && !labelMuestra.isBlank()) {
            addCampo(doc, "Muestra", labelMuestra, fLabel, fValor);
        }
        if (d.getPuntoMuestreo() != null && !d.getPuntoMuestreo().isBlank()) {
            addCampo(doc, "Punto de muestreo", d.getPuntoMuestreo(), fLabel, fValor);
        }

        addCampo(doc, "Protocolo de análisis", "N°" + nvl(d.getNroProtocolo()), fLabel, fValor);
        addCampo(doc, "Fecha recepción de la muestra", formatFecha(d.getFechaIngreso()), fLabel, fValor);

        // Separador verde antes de Resultados
        doc.add(buildSeparator(1f, new Color(26, 107, 58)));

        // Encabezado de resultados
        Paragraph secResul = new Paragraph("Resultados", fSeccion);
        secResul.setAlignment(Element.ALIGN_CENTER);
        secResul.setSpacingBefore(10);
        secResul.setSpacingAfter(6);
        doc.add(secResul);

        // Agrupar parámetros por tipoAnalisis y renderizar una tabla por grupo
        List<String> resoluciones = d.getResolucionesAplicadas() != null
                ? d.getResolucionesAplicadas() : List.of();
        List<String> colLabels   = buildResolucionColumnLabels(resoluciones);
        List<String> footnotes   = buildResolucionFootnotes(resoluciones);
        Map<String, List<ParametroResultadoTO>> grupos = agruparPorTipo(d.getParametros());

        for (Map.Entry<String, List<ParametroResultadoTO>> entry : grupos.entrySet()) {
            String tipo = entry.getKey();
            if (!tipo.isEmpty()) {
                Paragraph subSec = new Paragraph(labelTipoAnalisis(tipo), fSubseccion);
                subSec.setSpacingAfter(8);
                doc.add(subSec);
            }
            addTablaGrupo(doc, entry.getValue(), resoluciones, colLabels);
        }

        // Notas de resoluciones (solo cuando hay más de una)
        addNotasResoluciones(doc, footnotes, fNota);

        // Notas de abreviaturas (CAA, Ley 19587)
        addNotas(doc, d, fNota);

        // Párrafo de conclusión
        addConclusion(doc, d, fConcl);

        // Nota arsénico (solo cuando el resultado supera 0,01 mg/l)
        addNotaArsenico(doc, d, fNota);

        // Firma
        addFirma(doc);
    }

    private void addCampo(Document doc, String etiqueta, String valor,
                          Font fLabel, Font fValor) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta + ": ", fLabel));
        p.add(new Chunk(valor, fValor));
        p.setSpacingAfter(3);
        doc.add(p);
    }

    private void addCampoConLinea(Document doc, String etiqueta, String valor,
                                  Font fLabel, Font fValor) throws DocumentException {
        addCampo(doc, etiqueta, valor, fLabel, fValor);
        doc.add(buildSeparator(1f));
    }

    private Element buildSeparator(float width) {
        return buildSeparator(width, new Color(26, 107, 58), 90f);
    }

    private Element buildSeparator(float width, Color color) {
        return buildSeparator(width, color, 90);
    }

    private Element buildSeparator(float width, Color color, float widthPct) {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(widthPct);
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorderWidthBottom(width);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setMinimumHeight(4f);
        cell.setBorderColor(color);
        line.addCell(cell);
        line.setSpacingAfter(4f);
        return line;
    }

    // ----------------------------------------------------------------
    // Agrupación por tipo de análisis
    // ----------------------------------------------------------------

    private Map<String, List<ParametroResultadoTO>> agruparPorTipo(List<ParametroResultadoTO> params) {
        Map<String, List<ParametroResultadoTO>> grupos = new LinkedHashMap<>();
        if (params == null) return grupos;
        for (ParametroResultadoTO p : params) {
            String key = (p.getTipoAnalisis() != null && !p.getTipoAnalisis().isBlank())
                    ? p.getTipoAnalisis() : "";
            grupos.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        // Mover el grupo sin tipo al final si hay otros grupos con tipo
        if (grupos.containsKey("") && grupos.size() > 1) {
            List<ParametroResultadoTO> sinTipo = grupos.remove("");
            grupos.put("", sinTipo);
        }
        return grupos;
    }

    private String labelTipoAnalisis(String tipo) {
        return switch (tipo) {
            case "FISICO_QUIMICO"  -> "Análisis Físico Químico";
            case "BACTERIOLOGICO"  -> "Análisis Bacteriológico";
            case "CONTAMINANTES_ORGANICO"  -> "Contaminantes orgánicos";
            case "HAPN"  -> "Hidrocarburos Aromáticos Polinucleares (HAPN)";
            default -> "Análisis " + tipo;
        };
    }

    // ----------------------------------------------------------------
    // Cabeceras y notas de resoluciones (superíndices cuando hay >1)
    // ----------------------------------------------------------------

    private List<String> buildResolucionColumnLabels(List<String> resoluciones) {
        if (resoluciones.size() <= 1) {
            return resoluciones.stream().map(this::buildLimiteHeader).toList();
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < resoluciones.size(); i++) {
            labels.add("Límites" + superscriptNum(i + 1));
        }
        return labels;
    }

    private List<String> buildResolucionFootnotes(List<String> resoluciones) {
        if (resoluciones.size() <= 1) return List.of();
        List<String> notes = new ArrayList<>();
        for (int i = 0; i < resoluciones.size(); i++) {
            String nombre = resoluciones.get(i);
            if (nombre.endsWith(" - Unico") || nombre.endsWith(" - Único")) {
                nombre = nombre.substring(0, nombre.lastIndexOf(" - "));
            }
            notes.add(superscriptNum(i + 1) + " " + nombre);
        }
        return notes;
    }

    private String superscriptNum(int n) {
        String[] sups = {"", "¹", "²", "³", "⁴", "⁵",
                         "⁶", "⁷", "⁸", "⁹"};
        return n > 0 && n < sups.length ? sups[n] : String.valueOf(n);
    }

    private void addNotasResoluciones(Document doc, List<String> footnotes,
                                      Font fNota) throws DocumentException {
        for (String note : footnotes) {
            doc.add(new Paragraph(note, fNota));
        }
        if (!footnotes.isEmpty()) {
            Paragraph espacio = new Paragraph(" ");
            espacio.setSpacingAfter(2);
            doc.add(espacio);
        }
    }

    // ----------------------------------------------------------------
    // Tabla de resultados
    // ----------------------------------------------------------------

    private void addTablaGrupo(Document doc, List<ParametroResultadoTO> params,
                                List<String> resoluciones,
                                List<String> colLabels) throws DocumentException {
        int numCols = 3 + resoluciones.size() + 1;
        float[] widths = buildWidthsDynamic(colLabels, params);

        PdfPTable tabla = new PdfPTable(numCols);
        tabla.setWidthPercentage(100);
        tabla.setWidths(widths);
        tabla.setSpacingBefore(2);
        tabla.setSpacingAfter(6);
        tabla.setHeaderRows(1);

        Font fHeader = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(0, 0, 0));
        Color headerBg = new Color(226, 239, 217);

        addHeaderCell(tabla, "Analito", fHeader, headerBg);
        addHeaderCell(tabla, "Unidad", fHeader, headerBg);
        addHeaderCell(tabla, "Resultados", fHeader, headerBg);
        for (String label : colLabels) {
            addHeaderCell(tabla, label, fHeader, headerBg);
        }
        addHeaderCell(tabla, "Metodología", fHeader, headerBg);

        Font fParam = new Font(Font.HELVETICA, 9, Font.NORMAL);
        Font fResult = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font fSmall = new Font(Font.HELVETICA, 8, Font.NORMAL);

        for (ParametroResultadoTO p : params) {
            addDataCell(tabla, nvl(p.getNombre()), fParam, Element.ALIGN_LEFT);
            addDataCell(tabla, nvl(p.getUnidad()), fParam, Element.ALIGN_CENTER);
            addDataCell(tabla, nvl(p.getValorResultado()), fResult, Element.ALIGN_CENTER);
            for (String r : resoluciones) {
                addDataCell(tabla, findLimite(p.getLimites(), r), fParam, Element.ALIGN_CENTER);
            }
            addDataCell(tabla, nvl(p.getMetodologiaNombre()), fSmall, Element.ALIGN_LEFT);
        }

        doc.add(tabla);
    }

    private float[] buildWidthsDynamic(List<String> colLabels, List<ParametroResultadoTO> parametros) {
        int numLimCols = colLabels.size();
        int cols = 4 + numLimCols;

        // Arrancar con la longitud del header de cada columna
        int[] maxLen = new int[cols];
        maxLen[0] = 7;   // "Analito"
        maxLen[1] = 6;   // "Unidad"
        maxLen[2] = 10;  // "Resultados"
        for (int i = 0; i < numLimCols; i++) {
            maxLen[3 + i] = colLabels.get(i).length();
        }
        maxLen[cols - 1] = 11; // "Metodología"

        // Medir el contenido real de cada fila
        if (parametros != null) {
            for (ParametroResultadoTO p : parametros) {
                maxLen[0] = Math.max(maxLen[0], slen(p.getNombre()));
                maxLen[1] = Math.max(maxLen[1], slen(p.getUnidad()));
                maxLen[2] = Math.max(maxLen[2], slen(p.getValorResultado()));
                if (p.getLimites() != null) {
                    for (LimiteAplicableTO l : p.getLimites()) {
                        String val = formatLimite(l);
                        for (int i = 0; i < numLimCols; i++) {
                            maxLen[3 + i] = Math.max(maxLen[3 + i], val.length());
                        }
                    }
                }
                maxLen[cols - 1] = Math.max(maxLen[cols - 1], slen(p.getMetodologiaNombre()));
            }
        }

        // Pesos con clamp por tipo de columna
        float[] weights = new float[cols];
        weights[0] = clampW(maxLen[0], 18, 40);
        weights[1] = clampW(maxLen[1], 5, 9);
        weights[2] = clampW(maxLen[2], 8, 13);
        for (int i = 0; i < numLimCols; i++) {
            // Con superíndices el header es corto ("Límites¹"), el contenido manda
            weights[3 + i] = clampW(maxLen[3 + i], 8, 24);
        }
        weights[cols - 1] = clampW(maxLen[cols - 1], 14, 36);

        // Normalizar a 100 %
        float total = 0f;
        for (float w : weights) total += w;
        float[] result = new float[cols];
        for (int i = 0; i < cols; i++) result[i] = weights[i] * 100f / total;
        return result;
    }

    private int slen(String s) {
        return s != null ? s.length() : 0;
    }

    private float clampW(int val, int min, int max) {
        return Math.min(max, Math.max(min, val));
    }

    private String buildLimiteHeader(String origenNombre) {
        String nombre = origenNombre;
        if (nombre.endsWith(" - Unico") || nombre.endsWith(" - Único")) {
            nombre = nombre.substring(0, nombre.lastIndexOf(" - "));
        }
        return "Límites " + nombre;
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
            case "MAX"    -> l.getLimiteMax() != null ? l.getLimiteMax() : "-";
            case "MIN"    -> l.getLimiteMin() != null ? l.getLimiteMin() : "-";
            case "RANGO"  -> (l.getLimiteMin() != null && l.getLimiteMax() != null)
                    ? l.getLimiteMin() + " - " + l.getLimiteMax() : "-";
            case "TEXTO"  -> l.getLimiteTexto() != null ? l.getLimiteTexto() : "-";
            case "AUSENCIA" -> "Ausente";
            default -> "-";
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

    private boolean tieneArsenicoCritico(AnalisisDetalleTO d) {
        if (d.getParametros() == null) return false;
        return d.getParametros().stream()
                .filter(p -> {
                    String nombre = p.getNombre();
                    if (nombre == null) return false;
                    String norm = nombre.toLowerCase()
                            .replace("é", "e").replace("è", "e");
                    return norm.contains("arsen");
                })
                .anyMatch(p -> {
                    String val = p.getValorResultado();
                    if (val == null || val.isBlank()) return false;
                    String clean = val.trim();
                    if (clean.startsWith("<") || clean.startsWith(">")) return false;
                    try {
                        return Double.parseDouble(clean.replace(",", ".")) > 0.01;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
    }

    private void addNotaArsenico(Document doc, AnalisisDetalleTO d, Font fNota) throws DocumentException {
        if (!tieneArsenicoCritico(d)) return;
        Paragraph p = new Paragraph(NOTA_ARSENICO, fNota);
        p.setSpacingBefore(6);
        p.setSpacingAfter(20);
        doc.add(p);
    }

    private void addFirma(Document doc) throws Exception {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(35);
        tabla.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Celda con imagen de firma (si existe) o espacio vacío
        PdfPCell celdaFirma;
        try (InputStream is = InformeService.class.getResourceAsStream("/static/img/firma.png")) {
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

    private String nvl(String s) {
        return s != null && !s.isBlank() ? s : "-";
    }

    private String upper(String s) {
        return s != null ? s.toUpperCase() : "-";
    }

    private String formatFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) return "-";
        try {
            return LocalDate.parse(fechaStr).format(FMT);
        } catch (Exception e) {
            return fechaStr;
        }
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
            float left = doc.left();
            float right = doc.right();
            float top = ps.getTop();

            // Logo en esquina superior izquierda
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(115, 52);
            logo.setAbsolutePosition(left, top - 30 - logo.getScaledHeight());
            cb.addImage(logo);

            // Datos de contacto alineados a la derecha
            float textTop = top - 30;
            float lineH = 11f;
            cb.beginText();
            cb.setFontAndSize(bf, 8);
            showRight(cb, LAB_NOMBRE, right, textTop);
            showRight(cb, LAB_DIRECCION, right, textTop - lineH);
            showRight(cb, LAB_TELEFONOS, right, textTop - lineH * 2);
            showRight(cb, LAB_EMAIL1, right, textTop - lineH * 3);
            showRight(cb, LAB_EMAIL2, right, textTop - lineH * 4);
            cb.endText();
        }

        private void drawFooter(PdfWriter writer, Document doc) throws Exception {
            PdfContentByte cb = writer.getDirectContent();
            float left = doc.left();
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
            float y = footerTop - 12;
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
