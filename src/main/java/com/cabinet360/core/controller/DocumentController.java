package com.cabinet360.core.controller;

import com.cabinet360.core.dto.DocumentDto;
import com.cabinet360.core.service.DocumentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Document Management
 * Handles medical document operations including upload, download, and metadata management
 */
@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    // Allowed file types for medical documents
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "pdf", "jpg", "jpeg", "png", "gif", "tiff", "doc", "docx", "txt", "rtf"
    );

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Create a new document metadata entry
     * POST /api/v1/documents
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DocumentDto> createDocument(@Valid @RequestBody DocumentDto documentDto) {
        logger.info("📄 Creating new document: {} for patient: {}",
                documentDto.getNom(), documentDto.getPatientUserId());

        try {
            DocumentDto created = documentService.createDocument(documentDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Document creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create multiple documents in batch
     * POST /api/v1/documents/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> createDocumentsBatch(@Valid @RequestBody List<DocumentDto> documents) {
        logger.info("📄 Creating {} documents in batch", documents.size());

        try {
            List<DocumentDto> created = documentService.createDocumentsBatch(documents);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Batch document creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Upload file and create document metadata
     * POST /api/v1/documents/upload
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dossierMedicalId") Long dossierMedicalId,
            @RequestParam("patientUserId") Long patientUserId,
            @RequestParam(value = "typeDocument", required = false) String typeDocument) {

        logger.info("📤 Uploading file: {} for patient: {}", file.getOriginalFilename(), patientUserId);

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            String filename = file.getOriginalFilename();
            if (!documentService.isValidFileType(filename, ALLOWED_EXTENSIONS)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid file type. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS)
                ));
            }

            // TODO: Implement actual file storage (S3, local filesystem, etc.)
            String fileUrl = "/documents/" + System.currentTimeMillis() + "_" + filename;

            // Create document metadata
            DocumentDto documentDto = new DocumentDto.Builder()
                    .nom(filename)
                    .url(fileUrl)
                    .typeDocument(typeDocument != null ? typeDocument : detectFileType(filename))
                    .dateUpload(LocalDateTime.now())
                    .dossierMedicalId(dossierMedicalId)
                    .patientUserId(patientUserId)
                    .build();

            DocumentDto created = documentService.createDocument(documentDto);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "File uploaded successfully",
                    "document", created,
                    "fileUrl", fileUrl
            ));

        } catch (Exception e) {
            logger.error("❌ File upload failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Get document by ID
     * GET /api/v1/documents/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Long id) {
        logger.info("🔍 Fetching document: {}", id);

        try {
            DocumentDto document = documentService.findById(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            logger.error("❌ Document not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all documents with pagination
     * GET /api/v1/documents?page=0&size=10&sortBy=dateUpload&sortDirection=DESC
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<Page<DocumentDto>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateUpload") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.info("🔍 Fetching documents - page: {}, size: {}", page, size);

        Page<DocumentDto> documents = documentService.findAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by medical record
     * GET /api/v1/documents/dossier/{dossierMedicalId}
     */
    @GetMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> getDocumentsByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🔍 Fetching documents for dossier medical: {}", dossierMedicalId);

        try {
            List<DocumentDto> documents = documentService.findByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch documents: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get documents by patient
     * GET /api/v1/documents/patient/{patientUserId}
     */
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (@authUser.patientUserId == #patientUserId and hasRole('PATIENT'))")
    public ResponseEntity<List<DocumentDto>> getDocumentsByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching documents for patient: {}", patientUserId);

        List<DocumentDto> documents = documentService.findByPatient(patientUserId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get recent documents for a patient
     * GET /api/v1/documents/patient/{patientUserId}/recent?limit=5
     */
    @GetMapping("/patient/{patientUserId}/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (@authUser.patientUserId == #patientUserId and hasRole('PATIENT'))")
    public ResponseEntity<List<DocumentDto>> getRecentDocumentsByPatient(
            @PathVariable Long patientUserId,
            @RequestParam(defaultValue = "5") int limit) {

        logger.info("🔍 Fetching {} recent documents for patient: {}", limit, patientUserId);

        List<DocumentDto> documents = documentService.findRecentDocumentsByPatient(patientUserId, limit);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by type
     * GET /api/v1/documents/type/{typeDocument}
     */
    @GetMapping("/type/{typeDocument}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> getDocumentsByType(@PathVariable String typeDocument) {
        logger.info("🔍 Fetching documents by type: {}", typeDocument);

        List<DocumentDto> documents = documentService.findByType(typeDocument);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by dossier and type
     * GET /api/v1/documents/dossier/{dossierMedicalId}/type/{typeDocument}
     */
    @GetMapping("/dossier/{dossierMedicalId}/type/{typeDocument}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> getDocumentsByDossierAndType(
            @PathVariable Long dossierMedicalId,
            @PathVariable String typeDocument) {

        logger.info("🔍 Fetching documents for dossier {} with type: {}", dossierMedicalId, typeDocument);

        try {
            List<DocumentDto> documents = documentService.findByDossierAndType(dossierMedicalId, typeDocument);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch documents: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Search documents by name
     * GET /api/v1/documents/search/name?q=radiology
     */
    @GetMapping("/search/name")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> searchDocumentsByName(@RequestParam String q) {
        logger.info("🔍 Searching documents by name: {}", q);

        List<DocumentDto> documents = documentService.searchByName(q);
        return ResponseEntity.ok(documents);
    }

    /**
     * Search documents by type
     * GET /api/v1/documents/search/type?q=pdf
     */
    @GetMapping("/search/type")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> searchDocumentsByType(@RequestParam String q) {
        logger.info("🔍 Searching documents by type: {}", q);

        List<DocumentDto> documents = documentService.searchByType(q);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by date range
     * GET /api/v1/documents/date-range?start=2023-01-01T00:00:00&end=2023-12-31T23:59:59
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> getDocumentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        logger.info("🔍 Fetching documents between {} and {}", start, end);

        List<DocumentDto> documents = documentService.findByDateRange(start, end);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents for patient by date range
     * GET /api/v1/documents/patient/{patientUserId}/date-range?start=...&end=...
     */
    @GetMapping("/patient/{patientUserId}/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> getDocumentsByPatientAndDateRange(
            @PathVariable Long patientUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        logger.info("🔍 Fetching documents for patient {} between {} and {}", patientUserId, start, end);

        List<DocumentDto> documents = documentService.findByPatientAndDateRange(patientUserId, start, end);
        return ResponseEntity.ok(documents);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Update document metadata
     * PUT /api/v1/documents/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DocumentDto> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDto documentDto) {

        logger.info("🔄 Updating document: {}", id);

        try {
            DocumentDto updated = documentService.updateDocument(id, documentDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Document update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Partially update document
     * PATCH /api/v1/documents/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DocumentDto> partialUpdateDocument(
            @PathVariable Long id,
            @RequestBody DocumentDto partialDto) {

        logger.info("🔄 Partially updating document: {}", id);

        try {
            DocumentDto updated = documentService.partialUpdate(id, partialDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Document partial update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update document URL
     * PATCH /api/v1/documents/{id}/url
     */
    @PatchMapping("/{id}/url")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DocumentDto> updateDocumentUrl(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        logger.info("🔄 Updating document URL: {}", id);

        try {
            String newUrl = request.get("url");
            if (newUrl == null || newUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            DocumentDto updated = documentService.updateDocumentUrl(id, newUrl);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Document URL update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Delete document by ID
     * DELETE /api/v1/documents/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable Long id) {
        logger.info("🗑️ Deleting document: {}", id);

        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Document deleted successfully",
                    "documentId", id.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Document deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete multiple documents
     * DELETE /api/v1/documents/batch
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteDocumentsBatch(@RequestBody List<Long> ids) {
        logger.info("🗑️ Deleting {} documents in batch", ids.size());

        try {
            documentService.deleteDocumentsBatch(ids);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Documents deleted successfully",
                    "deletedCount", ids.size()
            ));
        } catch (Exception e) {
            logger.error("❌ Batch document deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete all documents for a dossier medical
     * DELETE /api/v1/documents/dossier/{dossierMedicalId}
     */
    @DeleteMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDocumentsByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🗑️ Deleting all documents for dossier: {}", dossierMedicalId);

        try {
            documentService.deleteByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All documents deleted for dossier medical",
                    "dossierMedicalId", dossierMedicalId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Documents deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete all documents for a patient
     * DELETE /api/v1/documents/patient/{patientUserId}
     */
    @DeleteMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDocumentsByPatient(@PathVariable Long patientUserId) {
        logger.info("🗑️ Deleting all documents for patient: {}", patientUserId);

        try {
            documentService.deleteByPatient(patientUserId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All documents deleted for patient",
                    "patientUserId", patientUserId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Documents deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // UTILITY ENDPOINTS
    // ========================================

    /**
     * Count documents by patient
     * GET /api/v1/documents/patient/{patientUserId}/count
     */
    @GetMapping("/patient/{patientUserId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Long>> countDocumentsByPatient(@PathVariable Long patientUserId) {
        logger.info("📊 Counting documents for patient: {}", patientUserId);

        long count = documentService.countByPatient(patientUserId);
        return ResponseEntity.ok(Map.of("documentCount", count));
    }

    /**
     * Count documents by type
     * GET /api/v1/documents/type/{typeDocument}/count
     */
    @GetMapping("/type/{typeDocument}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Long>> countDocumentsByType(@PathVariable String typeDocument) {
        logger.info("📊 Counting documents by type: {}", typeDocument);

        long count = documentService.countByType(typeDocument);
        return ResponseEntity.ok(Map.of("documentCount", count));
    }

    /**
     * Check if document exists
     * GET /api/v1/documents/{id}/exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Boolean>> checkDocumentExists(@PathVariable Long id) {
        logger.info("🔍 Checking if document exists: {}", id);

        boolean exists = documentService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Find documents by URL (duplicate check)
     * GET /api/v1/documents/check-duplicates?url=...
     */
    @GetMapping("/check-duplicates")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DocumentDto>> findDocumentsByUrl(@RequestParam String url) {
        logger.info("🔍 Checking for duplicate documents with URL: {}", url);

        List<DocumentDto> duplicates = documentService.findByUrl(url);
        return ResponseEntity.ok(duplicates);
    }

    /**
     * Get allowed file types
     * GET /api/v1/documents/allowed-types
     */
    @GetMapping("/allowed-types")
    public ResponseEntity<Map<String, List<String>>> getAllowedFileTypes() {
        return ResponseEntity.ok(Map.of("allowedExtensions", ALLOWED_EXTENSIONS));
    }

    /**
     * Health check endpoint for document service
     * GET /api/v1/documents/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "DocumentService",
                "timestamp", System.currentTimeMillis(),
                "allowedFileTypes", ALLOWED_EXTENSIONS.size()
        ));
    }

    // ========================================
    // PRIVATE UTILITY METHODS
    // ========================================

    /**
     * Detect file type from filename extension
     */
    private String detectFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "UNKNOWN";
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        return switch (extension) {
            case "pdf" -> "PDF";
            case "jpg", "jpeg", "png", "gif", "tiff" -> "IMAGE";
            case "doc", "docx" -> "DOCUMENT";
            case "txt", "rtf" -> "TEXT";
            default -> "OTHER";
        };
    }
}