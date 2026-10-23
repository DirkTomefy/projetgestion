package s5.itu.projetgestion.service.commun;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import s5.itu.projetgestion.config.UploadProperties;
import s5.itu.projetgestion.model.commun.Document;
import s5.itu.projetgestion.model.commun.TypeDocument;
import s5.itu.projetgestion.repository.commun.DocumentRepository;
import s5.itu.projetgestion.repository.commun.TypeDocumentRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    /** Extensions interdites (sécurité minimale). */
    private static final Set<String> FORBIDDEN_EXT = Set.of(
            "exe", "bat", "cmd", "com", "scr", "msi",
            "sh", "ps1", "jar", "js", "jsp", "php"
    );

    private final UploadProperties props;
    private final DocumentRepository documentRepository;
    private final TypeDocumentRepository typeDocumentRepository;

    private Path root;

    public FileStorageService(UploadProperties props,
                              DocumentRepository documentRepository,
                              TypeDocumentRepository typeDocumentRepository) {
        this.props = props;
        this.documentRepository = documentRepository;
        this.typeDocumentRepository = typeDocumentRepository;
    }

    @PostConstruct
    void init() {
        try {
            this.root = Paths.get(props.getDir()).toAbsolutePath().normalize();
            Files.createDirectories(this.root);
            log.info("Répertoire d'upload initialisé : {}", root);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de créer le répertoire d'upload : " + props.getDir(), e);
        }
    }

    @Transactional
    public Document saveUpload(MultipartFile file, Integer typeDocumentId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier fourni");
        }
        if (file.getSize() > props.getMaxSizeBytes()) {
            throw new IllegalArgumentException(
                    "Fichier trop volumineux (max " + props.getMaxSizeBytes() + " octets)");
        }

        String safeName = sanitize(file.getOriginalFilename());
        String ext = extensionOf(safeName);
        if (ext != null && FORBIDDEN_EXT.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("Extension non autorisée : ." + ext);
        }

        TypeDocument type = typeDocumentRepository.findById(typeDocumentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de document introuvable : " + typeDocumentId));

        // Dossier cible : uploads/<année>/<mois>/
        LocalDate today = LocalDate.now();
        Path relDir = Paths.get(String.valueOf(today.getYear()),
                                String.format("%02d", today.getMonthValue()));
        Path targetDir = root.resolve(relDir).normalize();
        if (!targetDir.startsWith(root)) {
            throw new SecurityException("Chemin cible invalide");
        }
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le répertoire cible", e);
        }

        String storedName = UUID.randomUUID() + "__" + safeName;
        Path target = targetDir.resolve(storedName);
        if (!target.startsWith(root)) {
            throw new SecurityException("Chemin cible invalide");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Échec de l'écriture du fichier", e);
        }

        String relativePath = relDir.resolve(storedName).toString().replace('\\', '/');

        Document doc = new Document();
        doc.setTypeDocument(type);
        doc.setLien(relativePath);
        Document saved = documentRepository.save(doc);

        log.info("Document enregistré id={} type={} chemin={}",
                saved.getId(), type.getLibelle(), relativePath);
        return saved;
    }

    public Resource loadAsResource(Integer documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document introuvable : " + documentId));

        Path p = root.resolve(doc.getLien()).normalize();
        if (!p.startsWith(root) || !Files.exists(p)) {
            throw new IllegalArgumentException(
                    "Fichier absent du disque pour le document " + documentId);
        }
        try {
            return new UrlResource(p.toUri());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("URI de fichier invalide", e);
        }
    }

    public String urlOf(Document doc) {
        return props.getUrlPrefix() + doc.getLien();
    }

    public String originalNameOf(Document doc) {
        String name = Paths.get(doc.getLien()).getFileName().toString();
        int idx = name.indexOf("__");
        return idx >= 0 ? name.substring(idx + 2) : name;
    }

    
    private String sanitize(String name) {
        if (name == null) return "fichier";
        String base = Paths.get(name).getFileName().toString();       // retire d'éventuels ../ 
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        base = base.replaceAll("^\\.+", "");                          // pas de leading dots
        if (base.isBlank()) base = "fichier";

        if (base.length() > 120) {
            String ext = extensionOf(base);
            String stem = ext != null ? base.substring(0, base.length() - ext.length() - 1) : base;
            stem = stem.substring(0, Math.min(100, stem.length()));
            base = ext != null ? stem + "." + ext : stem;
        }
        return base;
    }

    private String extensionOf(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0 && i < name.length() - 1) ? name.substring(i + 1) : null;
    }
}