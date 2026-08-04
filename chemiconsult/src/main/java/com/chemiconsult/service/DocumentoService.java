package com.chemiconsult.service;

import com.chemiconsult.entity.CategoriaDocumentoDE;
import com.chemiconsult.entity.DocumentoDE;
import com.chemiconsult.mapper.DocumentoMapper;
import com.chemiconsult.repository.CategoriaDocumentoRepository;
import com.chemiconsult.repository.DocumentoRepository;
import com.chemiconsult.supabase.service.SupabaseBucketService;
import com.chemiconsult.to.DocumentoTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentoService {

    private static final String BUCKET = "chemiconsult-bucket";
    private static final String PREFIX = "documentos";

    private final DocumentoRepository documentoRepository;
    private final CategoriaDocumentoRepository categoriaRepository;
    private final SupabaseBucketService supabaseBucketService;

    @Autowired
    public DocumentoService(DocumentoRepository documentoRepository,
                            CategoriaDocumentoRepository categoriaRepository,
                            SupabaseBucketService supabaseBucketService) {
        this.documentoRepository = documentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.supabaseBucketService = supabaseBucketService;
    }

    public List<DocumentoTO> getAll() {
        return DocumentoMapper.toTO(documentoRepository.findAllByOrderByCreatedDateDesc());
    }

    public DocumentoTO getById(Long id) {
        return DocumentoMapper.toTO(findOrThrow(id));
    }

    public DocumentoTO create(String nombre, String descripcion, Long categoriaId,
                               String fechaVencimiento, MultipartFile file) {
        DocumentoDE entity = new DocumentoDE();
        entity.setNombre(nombre);
        entity.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion : null);
        entity.setCategoria(findCategoria(categoriaId));
        if (fechaVencimiento != null && !fechaVencimiento.isBlank()) {
            entity.setFechaVencimiento(LocalDate.parse(fechaVencimiento));
        }
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        entity = documentoRepository.save(entity);

        String safeName = sanitizeFilename(file.getOriginalFilename());
        String path = PREFIX + "/" + entity.getId() + "/" + safeName;
        supabaseBucketService.subirArchivo(BUCKET, path, file);
        entity.setArchivoUrl(path);
        entity.setNombreArchivo(file.getOriginalFilename());
        return DocumentoMapper.toTO(documentoRepository.save(entity));
    }

    public DocumentoTO updateMetadata(Long id, String nombre, String descripcion,
                                       Long categoriaId, String fechaVencimiento) {
        DocumentoDE entity = findOrThrow(id);
        entity.setNombre(nombre);
        entity.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion : null);
        entity.setCategoria(findCategoria(categoriaId));
        entity.setFechaVencimiento(
                (fechaVencimiento != null && !fechaVencimiento.isBlank())
                        ? LocalDate.parse(fechaVencimiento) : null);
        entity.setUpdateDate(LocalDateTime.now());
        return DocumentoMapper.toTO(documentoRepository.save(entity));
    }

    public byte[] descargarArchivo(Long id) {
        DocumentoDE entity = findOrThrow(id);
        return supabaseBucketService.descargarArchivo(BUCKET, entity.getArchivoUrl());
    }

    public void delete(Long id) {
        DocumentoDE entity = findOrThrow(id);
        if (entity.getArchivoUrl() != null) {
            supabaseBucketService.eliminarArchivo(BUCKET, entity.getArchivoUrl());
        }
        documentoRepository.deleteById(id);
    }

    private CategoriaDocumentoDE findCategoria(Long categoriaId) {
        if (categoriaId == null) return null;
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Categoría no encontrada: " + categoriaId));
    }

    private DocumentoDE findOrThrow(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Documento no encontrado con ID: " + id));
    }

    /** Devuelve un nombre de archivo seguro para usar como clave de storage (sin tildes ni espacios). */
    private static String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) return "archivo";
        // NFD descompone tildes; luego quitamos los diacríticos (non-ASCII tras NFD)
        String base = Normalizer.normalize(original, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        // Espacios → guion bajo; eliminar caracteres problemáticos para URLs/storage
        base = base.replaceAll("\\s+", "_")
                   .replaceAll("[^\\w.\\-]", "_");
        return base.isEmpty() ? "archivo" : base;
    }
}
