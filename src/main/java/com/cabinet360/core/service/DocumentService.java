package com.cabinet360.core.service;

import com.cabinet360.core.dto.DocumentDto;
import com.cabinet360.core.entity.Document;
import com.cabinet360.core.entity.DossierMedical;
import com.cabinet360.core.exception.DocumentNotFoundException;
import com.cabinet360.core.exception.DossierMedicalNotFoundException;
import com.cabinet360.core.mapper.DocumentMapper;
import com.cabinet360.core.repository.DocumentRepository;
import com.cabinet360.core.repository.DossierMedicalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing medical documents.
 * Provides CRUD operations with proper transaction management and file handling.
 */
@Service
@Transactional(readOnly = true)
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final DocumentMapper documentMapper;

    @Autowired
    public DocumentService(DocumentRepository documentRepository,
                           DossierMedicalRepository dossierMedicalRepository,
                           DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.documentMapper = documentMapper;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Creates a new document in the medical record.
     */
    @Transactional
    public DocumentDto createDocument(DocumentDto documentDto) {
        logger.info("Creating new document: {} for patient: {}",
                documentDto.getNom(), documentDto.getPatientUserId());

        // Validate dossier medical exists
        validateDossierMedicalExists(documentDto.getDossierMedicalId());

        // Set upload date if not provided
        if (documentDto.getDateUpload() == null) {
            documentDto.setDateUpload(LocalDateTime.now());
        }

        // Check for duplicate URLs
        if (documentDto.getUrl() != null && !documentDto.getUrl().isEmpty()) {
            List<Document> existingDocs = documentRepository.findByUrl(documentDto.getUrl());
            if (!existingDocs.isEmpty()) {
                logger.warn("Document with URL {} already exists", documentDto.getUrl());
            }
        }

        // Convert to entity and save
        Document document = documentMapper.toEntity(documentDto);
        Document savedDocument = documentRepository.save(document);

        logger.info("Created document with ID: {}", savedDocument.getId());
        return documentMapper.toDto(savedDocument);
    }

    /**
     * Creates multiple documents in batch.
     */
    @Transactional
    public List<DocumentDto> createDocumentsBatch(List<DocumentDto> documentDtos) {
        logger.info("Creating {} documents in batch", documentDtos.size());

        // Validate all dossiers exist
        documentDtos.forEach(dto -> validateDossierMedicalExists(dto.getDossierMedicalId()));

        // Set upload dates
        documentDtos.forEach(dto -> {
            if (dto.getDateUpload() == null) {
                dto.setDateUpload(LocalDateTime.now());
            }
        });

        // Convert and save all
        List<Document> documents = documentDtos.stream()
                .map(documentMapper::toEntity)
                .toList();

        List<Document> savedDocuments = documentRepository.saveAll(documents);

        logger.info("Created {} documents successfully", savedDocuments.size());
        return savedDocuments.stream()
                .map(documentMapper::toDto)
                .toList();
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Finds a document by its ID.
     */
    public DocumentDto findById(Long id) {
        logger.debug("Finding document by ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        return documentMapper.toDto(document);
    }

    /**
     * Gets all documents with pagination.
     */
    public Page<DocumentDto> findAll(int page, int size, String sortBy, String sortDirection) {
        logger.debug("Finding all documents - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return documentRepository.findAll(pageable)
                .map(documentMapper::toDto);
    }

    /**
     * Gets all documents for a specific medical record.
     */
    public List<DocumentDto> findByDossierMedical(Long dossierMedicalId) {
        logger.debug("Finding documents for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        return documentRepository.findByDossierMedicalId(dossierMedicalId)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Gets all documents for a specific patient.
     */
    public List<DocumentDto> findByPatient(Long patientUserId) {
        logger.debug("Finding documents for patient: {}", patientUserId);

        return documentRepository.findByPatientUserId(patientUserId)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Gets documents by type.
     */
    public List<DocumentDto> findByType(String typeDocument) {
        logger.debug("Finding documents by type: {}", typeDocument);

        return documentRepository.findByTypeDocument(typeDocument)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Searches documents by name (partial match).
     */
    public List<DocumentDto> searchByName(String nom) {
        logger.debug("Searching documents by name: {}", nom);

        return documentRepository.findByNomContainingIgnoreCase(nom)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Searches documents by type (partial match).
     */
    public List<DocumentDto> searchByType(String typeDocument) {
        logger.debug("Searching documents by type: {}", typeDocument);

        return documentRepository.findByTypeDocumentContainingIgnoreCase(typeDocument)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Gets documents for a dossier medical by type.
     */
    public List<DocumentDto> findByDossierAndType(Long dossierMedicalId, String typeDocument) {
        logger.debug("Finding documents for dossier {} with type: {}", dossierMedicalId, typeDocument);

        validateDossierMedicalExists(dossierMedicalId);

        return documentRepository.findByDossierMedicalIdAndTypeDocument(dossierMedicalId, typeDocument)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Gets documents uploaded within a date range.
     */
    public List<DocumentDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding documents uploaded between {} and {}", startDate, endDate);

        return documentRepository.findByDateUploadBetween(startDate, endDate)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Gets documents for a patient within a date range.
     */
    public List<DocumentDto> findByPatientAndDateRange(Long patientUserId,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate) {
        logger.debug("Finding documents for patient {} between {} and {}",
                patientUserId, startDate, endDate);

        return documentRepository.findByPatientAndDateRange(patientUserId, startDate, endDate)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Gets recent documents for a patient.
     */
    public List<DocumentDto> findRecentDocumentsByPatient(Long patientUserId, int limit) {
        logger.debug("Finding {} recent documents for patient: {}", limit, patientUserId);

        return documentRepository.findRecentDocumentsByPatient(patientUserId)
                .stream()
                .limit(limit)
                .map(documentMapper::toDto)
                .toList();
    }

    /**
     * Gets document count for a patient.
     */
    public long countByPatient(Long patientUserId) {
        logger.debug("Counting documents for patient: {}", patientUserId);
        return documentRepository.countByPatientUserId(patientUserId);
    }

    /**
     * Gets document count by type.
     */
    public long countByType(String typeDocument) {
        logger.debug("Counting documents by type: {}", typeDocument);
        return documentRepository.countByTypeDocument(typeDocument);
    }

    /**
     * Checks for duplicate URLs.
     */
    public List<DocumentDto> findByUrl(String url) {
        logger.debug("Finding documents with URL: {}", url);

        return documentRepository.findByUrl(url)
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Updates an existing document.
     */
    @Transactional
    public DocumentDto updateDocument(Long id, DocumentDto documentDto) {
        logger.info("Updating document with ID: {}", id);

        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        // Validate dossier medical if changed
        if (documentDto.getDossierMedicalId() != null &&
                !documentDto.getDossierMedicalId().equals(existingDocument.getDossierMedical().getId())) {
            validateDossierMedicalExists(documentDto.getDossierMedicalId());
        }

        // Update entity using mapper
        documentMapper.updateEntityFromDto(documentDto, existingDocument);

        Document updatedDocument = documentRepository.save(existingDocument);

        logger.info("Updated document with ID: {}", updatedDocument.getId());
        return documentMapper.toDto(updatedDocument);
    }

    /**
     * Partially updates a document.
     */
    @Transactional
    public DocumentDto partialUpdate(Long id, DocumentDto partialDto) {
        logger.info("Partially updating document with ID: {}", id);

        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        // Only update non-null fields
        if (partialDto.getNom() != null) {
            existingDocument.setNom(partialDto.getNom());
        }
        if (partialDto.getUrl() != null) {
            existingDocument.setUrl(partialDto.getUrl());
        }
        if (partialDto.getTypeDocument() != null) {
            existingDocument.setTypeDocument(partialDto.getTypeDocument());
        }
        if (partialDto.getDateUpload() != null) {
            existingDocument.setDateUpload(partialDto.getDateUpload());
        }
        if (partialDto.getPatientUserId() != null) {
            existingDocument.setPatientUserId(partialDto.getPatientUserId());
        }
        if (partialDto.getDossierMedicalId() != null) {
            validateDossierMedicalExists(partialDto.getDossierMedicalId());
            DossierMedical dossier = new DossierMedical();
            dossier.setId(partialDto.getDossierMedicalId());
            existingDocument.setDossierMedical(dossier);
        }

        Document updatedDocument = documentRepository.save(existingDocument);

        logger.info("Partially updated document with ID: {}", updatedDocument.getId());
        return documentMapper.toDto(updatedDocument);
    }

    /**
     * Updates document URL (useful for file moves/renames).
     */
    @Transactional
    public DocumentDto updateDocumentUrl(Long id, String newUrl) {
        logger.info("Updating document URL for ID: {} to: {}", id, newUrl);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        document.setUrl(newUrl);
        Document updatedDocument = documentRepository.save(document);

        logger.info("Updated document URL for ID: {}", updatedDocument.getId());
        return documentMapper.toDto(updatedDocument);
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Deletes a document by ID.
     */
    @Transactional
    public void deleteDocument(Long id) {
        logger.info("Deleting document with ID: {}", id);

        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException("Document not found with ID: " + id);
        }

        documentRepository.deleteById(id);
        logger.info("Deleted document with ID: {}", id);
    }

    /**
     * Deletes multiple documents by IDs.
     */
    @Transactional
    public void deleteDocumentsBatch(List<Long> ids) {
        logger.info("Deleting {} documents in batch", ids.size());

        // Validate all exist
        ids.forEach(id -> {
            if (!documentRepository.existsById(id)) {
                throw new DocumentNotFoundException("Document not found with ID: " + id);
            }
        });

        documentRepository.deleteAllById(ids);
        logger.info("Deleted {} documents successfully", ids.size());
    }

    /**
     * Deletes all documents for a specific dossier medical.
     */
    @Transactional
    public void deleteByDossierMedical(Long dossierMedicalId) {
        logger.info("Deleting all documents for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        List<Document> documents = documentRepository.findByDossierMedicalId(dossierMedicalId);
        documentRepository.deleteAll(documents);

        logger.info("Deleted {} documents for dossier medical: {}", documents.size(), dossierMedicalId);
    }

    /**
     * Deletes all documents for a specific patient.
     */
    @Transactional
    public void deleteByPatient(Long patientUserId) {
        logger.info("Deleting all documents for patient: {}", patientUserId);

        List<Document> documents = documentRepository.findByPatientUserId(patientUserId);
        documentRepository.deleteAll(documents);

        logger.info("Deleted {} documents for patient: {}", documents.size(), patientUserId);
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Validates that a dossier medical exists.
     */
    private void validateDossierMedicalExists(Long dossierMedicalId) {
        if (dossierMedicalId != null && !dossierMedicalRepository.existsById(dossierMedicalId)) {
            throw new DossierMedicalNotFoundException("Dossier medical not found with ID: " + dossierMedicalId);
        }
    }

    /**
     * Checks if a document exists.
     */
    public boolean existsById(Long id) {
        return documentRepository.existsById(id);
    }

    /**
     * Gets document by ID or returns empty.
     */
    public Optional<DocumentDto> findByIdOptional(Long id) {
        return documentRepository.findById(id)
                .map(documentMapper::toDto);
    }

    /**
     * Validates document file extension.
     */
    public boolean isValidFileType(String filename, List<String> allowedExtensions) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return allowedExtensions.contains(extension);
    }

    /**
     * Generates a summary of documents by type for a patient.
     */
    public List<Object[]> getDocumentSummaryByPatient(Long patientUserId) {
        logger.debug("Getting document summary for patient: {}", patientUserId);

        // This would typically be implemented with a custom query
        // For now, return a simple count by type
        return List.of(); // Implement custom query as needed
    }
}