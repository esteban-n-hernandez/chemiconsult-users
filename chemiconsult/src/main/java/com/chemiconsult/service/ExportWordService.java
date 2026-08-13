package com.chemiconsult.service;

import com.chemiconsult.to.AnalisisDetalleTO;
import com.chemiconsult.to.LimiteAplicableTO;
import com.chemiconsult.to.ParametroResultadoTO;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportWordService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String COLOR_VERDE = "1A6B3A";
    private static final String COLOR_HEADER_BG = "E2EFD9";

    public byte[] generarDocx(AnalisisDetalleTO d) throws Exception {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            agregarEncabezado(doc, d);
            agregarDatosGenerales(doc, d);
            agregarResultados(doc, d);
            if (d.getObservaciones() != null && !d.getObservaciones().isBlank()) {
                agregarObservaciones(doc, d.getObservaciones());
            }

            doc.write(out);
            return out.toByteArray();
        }
    }

    // ── Secciones principales ────────────────────────────────────────────────

    private void agregarEncabezado(XWPFDocument doc, AnalisisDetalleTO d) {
        XWPFParagraph titulo = doc.createParagraph();
        titulo.setAlignment(ParagraphAlignment.CENTER);
        titulo.setSpacingAfter(80);
        XWPFRun rTitulo = titulo.createRun();
        rTitulo.setText("INFORME DE ANALISIS");
        rTitulo.setBold(true);
        rTitulo.setFontSize(14);
        rTitulo.setUnderline(UnderlinePatterns.SINGLE);

        XWPFParagraph fechaPar = doc.createParagraph();
        fechaPar.setAlignment(ParagraphAlignment.RIGHT);
        fechaPar.setSpacingAfter(100);
        XWPFRun rFechaLabel = fechaPar.createRun();
        rFechaLabel.setText("Fecha de emisión: ");
        rFechaLabel.setBold(true);
        rFechaLabel.setFontSize(10);
        XWPFRun rFechaVal = fechaPar.createRun();
        rFechaVal.setText(LocalDate.now().format(FMT));
        rFechaVal.setFontSize(10);
    }

    private void agregarDatosGenerales(XWPFDocument doc, AnalisisDetalleTO d) {
        campo(doc, "Cliente", nvl(d.getCliente()).toUpperCase());
        separador(doc);

        String labelMuestra = (d.getTipoMuestraNombre() != null && !d.getTipoMuestraNombre().isBlank())
                ? d.getTipoMuestraNombre() : d.getMatrizNombre();
        if (labelMuestra != null && !labelMuestra.isBlank()) {
            campo(doc, "Muestra", labelMuestra);
        }
        if (d.getMatrizNombre() != null && !d.getMatrizNombre().isBlank()) {
            campo(doc, "Matriz", d.getMatrizNombre());
        }
        if (d.getPuntoMuestreo() != null && !d.getPuntoMuestreo().isBlank()) {
            campo(doc, "Punto de muestreo", d.getPuntoMuestreo());
        }
        campo(doc, "Protocolo de análisis", "N°" + nvl(d.getNroProtocolo()));
        campo(doc, "Fecha recepción de la muestra", formatFecha(d.getFechaIngreso()));
        campo(doc, "Fecha estimada de entrega", formatFecha(d.getFechaEntrega()));

        separadorVerde(doc);
    }

    private void agregarResultados(XWPFDocument doc, AnalisisDetalleTO d) {
        if (d.getParametros() == null || d.getParametros().isEmpty()) return;

        XWPFParagraph secPar = doc.createParagraph();
        secPar.setAlignment(ParagraphAlignment.CENTER);
        secPar.setSpacingBefore(100);
        secPar.setSpacingAfter(80);
        XWPFRun rSec = secPar.createRun();
        rSec.setText("Resultados");
        rSec.setBold(true);
        rSec.setFontSize(11);
        rSec.setColor(COLOR_VERDE);

        List<String> resoluciones = d.getResolucionesAplicadas() != null
                ? d.getResolucionesAplicadas() : List.of();
        List<String> colLabels = buildColLabels(resoluciones);
        List<String> footnotes = buildFootnotes(resoluciones);

        Map<String, List<ParametroResultadoTO>> grupos = agruparPorTipo(d.getParametros());

        for (Map.Entry<String, List<ParametroResultadoTO>> entry : grupos.entrySet()) {
            String tipo = entry.getKey();
            if (!tipo.isEmpty()) {
                XWPFParagraph subPar = doc.createParagraph();
                subPar.setSpacingBefore(140);
                subPar.setSpacingAfter(60);
                XWPFRun rSub = subPar.createRun();
                rSub.setText(tipo);
                rSub.setBold(true);
                rSub.setFontSize(10);
                rSub.setColor(COLOR_VERDE);
            }
            agregarTablaGrupo(doc, entry.getValue(), resoluciones, colLabels);
        }

        if (!footnotes.isEmpty()) {
            espacio(doc);
            for (String note : footnotes) {
                XWPFParagraph notePar = doc.createParagraph();
                notePar.setSpacingAfter(20);
                XWPFRun noteRun = notePar.createRun();
                noteRun.setText(note);
                noteRun.setFontSize(8);
            }
        }
    }

    private void agregarTablaGrupo(XWPFDocument doc,
                                    List<ParametroResultadoTO> params,
                                    List<String> resoluciones,
                                    List<String> colLabels) {
        // Columns: Analito | Resultado | Unidad | (Límites per resolución...) | Metodología
        int numCols = 3 + resoluciones.size() + 1;
        List<String> headers = new ArrayList<>(Arrays.asList("Analito", "Resultado", "Unidad"));
        headers.addAll(colLabels);
        headers.add("Metodología");

        XWPFTable tabla = doc.createTable(1, numCols);
        tabla.setWidth("100%");

        XWPFTableRow headerRow = tabla.getRow(0);
        while (headerRow.getTableCells().size() < numCols) headerRow.addNewTableCell();
        for (int i = 0; i < numCols; i++) {
            XWPFTableCell cell = headerRow.getCell(i);
            cell.setColor(COLOR_HEADER_BG);
            XWPFParagraph p = cell.getParagraphArray(0);
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();
            r.setText(headers.get(i));
            r.setBold(true);
            r.setFontSize(9);
        }

        for (ParametroResultadoTO param : params) {
            XWPFTableRow row = tabla.createRow();
            while (row.getTableCells().size() < numCols) row.addNewTableCell();
            celdaTabla(row, 0, nvl(param.getNombre()), ParagraphAlignment.LEFT);
            celdaTabla(row, 1, nvl(param.getValorResultado()), ParagraphAlignment.CENTER);
            celdaTabla(row, 2, nvl(param.getUnidad()), ParagraphAlignment.CENTER);
            for (int i = 0; i < resoluciones.size(); i++) {
                celdaTabla(row, 3 + i, findLimite(param.getLimites(), resoluciones.get(i)), ParagraphAlignment.CENTER);
            }
            celdaTabla(row, 3 + resoluciones.size(), nvl(param.getMetodologiaNombre()), ParagraphAlignment.LEFT);
        }

        espacio(doc);
    }

    private void agregarObservaciones(XWPFDocument doc, String obs) {
        seccion(doc, "Observaciones");
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(200);
        XWPFRun r = p.createRun();
        r.setText(obs);
        r.setFontSize(10);
    }

    // ── Agrupación y etiquetas ───────────────────────────────────────────────

    private Map<String, List<ParametroResultadoTO>> agruparPorTipo(List<ParametroResultadoTO> params) {
        Map<String, List<ParametroResultadoTO>> result = new LinkedHashMap<>();
        List<ParametroResultadoTO> sinTipo = new ArrayList<>();
        for (ParametroResultadoTO p : params) {
            String tipo = (p.getTipoAnalisis() != null && !p.getTipoAnalisis().isBlank())
                    ? p.getTipoAnalisis() : null;
            if (tipo == null) {
                sinTipo.add(p);
            } else {
                result.computeIfAbsent(tipo, k -> new ArrayList<>()).add(p);
            }
        }
        if (!sinTipo.isEmpty()) result.put("", sinTipo);
        return result;
    }

    private List<String> buildColLabels(List<String> resoluciones) {
        if (resoluciones.size() <= 1) {
            return resoluciones.stream()
                    .map(r -> "Límites " + cleanNombre(r))
                    .collect(Collectors.toList());
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < resoluciones.size(); i++) {
            labels.add("Límites" + superscript(i + 1));
        }
        return labels;
    }

    private List<String> buildFootnotes(List<String> resoluciones) {
        if (resoluciones.size() <= 1) return List.of();
        List<String> notes = new ArrayList<>();
        for (int i = 0; i < resoluciones.size(); i++) {
            notes.add(superscript(i + 1) + " " + cleanNombre(resoluciones.get(i)));
        }
        return notes;
    }

    private String cleanNombre(String nombre) {
        if (nombre.endsWith(" - Unico") || nombre.endsWith(" - Único")) {
            return nombre.substring(0, nombre.lastIndexOf(" - "));
        }
        return nombre;
    }

    private String superscript(int n) {
        String[] sups = {"", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};
        return n > 0 && n < sups.length ? sups[n] : String.valueOf(n);
    }

    private String findLimite(List<LimiteAplicableTO> limites, String origen) {
        if (limites == null) return "—";
        return limites.stream()
                .filter(l -> origen.equals(l.getOrigenNombre()))
                .findFirst()
                .map(this::formatLimite)
                .orElse("—");
    }

    private String formatLimite(LimiteAplicableTO l) {
        if (l.getTipoLimite() == null) return "—";
        return switch (l.getTipoLimite()) {
            case "MAX"      -> l.getLimiteMax() != null ? l.getLimiteMax() : "—";
            case "MIN"      -> l.getLimiteMin() != null ? l.getLimiteMin() : "—";
            case "RANGO"    -> (l.getLimiteMin() != null && l.getLimiteMax() != null)
                    ? l.getLimiteMin() + " – " + l.getLimiteMax() : "—";
            case "TEXTO"    -> l.getLimiteTexto() != null ? l.getLimiteTexto() : "—";
            case "AUSENCIA" -> "Ausente";
            default         -> "—";
        };
    }

    // ── Helpers de formato ───────────────────────────────────────────────────

    private void campo(XWPFDocument doc, String label, String valor) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(40);
        XWPFRun rLabel = p.createRun();
        rLabel.setText(label + ": ");
        rLabel.setBold(true);
        rLabel.setFontSize(10);
        XWPFRun rVal = p.createRun();
        rVal.setText(valor);
        rVal.setFontSize(10);
    }

    private void seccion(XWPFDocument doc, String titulo) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(160);
        p.setSpacingAfter(80);
        XWPFRun r = p.createRun();
        r.setText(titulo.toUpperCase());
        r.setBold(true);
        r.setFontSize(11);
        r.setColor(COLOR_VERDE);
    }

    private void separador(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(80);
        CTPPr pPr = p.getCTP().addNewPPr();
        CTPBdr bdr = pPr.addNewPBdr();
        CTBorder bottom = bdr.addNewBottom();
        bottom.setVal(STBorder.SINGLE);
        bottom.setSz(BigInteger.valueOf(6));
        bottom.setColor(COLOR_VERDE);
    }

    private void separadorVerde(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(120);
        p.setSpacingAfter(120);
        CTPPr pPr = p.getCTP().addNewPPr();
        CTPBdr bdr = pPr.addNewPBdr();
        CTBorder bottom = bdr.addNewBottom();
        bottom.setVal(STBorder.SINGLE);
        bottom.setSz(BigInteger.valueOf(12));
        bottom.setColor(COLOR_VERDE);
    }

    private void espacio(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(80);
    }

    private void celdaTabla(XWPFTableRow row, int idx, String texto, ParagraphAlignment align) {
        while (row.getTableCells().size() <= idx) row.addNewTableCell();
        XWPFTableCell cell = row.getCell(idx);
        XWPFParagraph p = cell.getParagraphArray(0);
        p.setAlignment(align);
        XWPFRun r = p.createRun();
        r.setText(texto);
        r.setFontSize(9);
    }

    private String nvl(String s) {
        return s != null && !s.isBlank() ? s : "—";
    }

    private String formatFecha(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try {
            String fecha = iso.split("T")[0];
            String[] p = fecha.split("-");
            return p[2] + "/" + p[1] + "/" + p[0];
        } catch (Exception e) {
            return iso;
        }
    }
}
