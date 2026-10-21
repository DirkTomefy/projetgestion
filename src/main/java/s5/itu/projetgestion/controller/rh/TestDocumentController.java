package s5.itu.projetgestion.controller.rh;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import s5.itu.projetgestion.dto.UploadResult;
import s5.itu.projetgestion.model.rh.Document;
import s5.itu.projetgestion.repository.rh.DocumentRepository;
import s5.itu.projetgestion.service.rh.FileStorageService;
import s5.itu.projetgestion.service.rh.TypeDocumentService;

import java.util.List;

@Controller
@RequestMapping("/documents")
public class TestDocumentController {

    private final FileStorageService fileStorageService;
    private final TypeDocumentService typeDocumentService;
    private final DocumentRepository documentRepository;

    public TestDocumentController(FileStorageService fileStorageService,
                                  TypeDocumentService typeDocumentService,
                                  DocumentRepository documentRepository) {
        this.fileStorageService = fileStorageService;
        this.typeDocumentService = typeDocumentService;
        this.documentRepository = documentRepository;
    }

    // ---------- UI : formulaire + liste ----------

    @GetMapping
    public String list(Model model) {
        List<Document> docs = documentRepository.findAll();
        model.addAttribute("documents", docs);
        model.addAttribute("urlOf", (java.util.function.Function<Document, String>)
                fileStorageService::urlOf);
        return "documents/list";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("types", typeDocumentService.findAll());
        return "documents/upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("typeDocumentId") Integer typeDocumentId,
                         RedirectAttributes ra) {
        try {
            Document d = fileStorageService.saveUpload(file, typeDocumentId);
            ra.addFlashAttribute("message",
                    "Fichier enregistré : document #" + d.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/documents";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document introuvable"));
        Resource resource = fileStorageService.loadAsResource(id);
        String original = fileStorageService.originalNameOf(doc);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + original + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping(value = "/api/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<UploadResult> apiUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("typeDocumentId") Integer typeDocumentId) {

        Document doc = fileStorageService.saveUpload(file, typeDocumentId);

        UploadResult result = new UploadResult(
                doc.getId(),
                doc.getTypeDocument().getLibelle(),
                doc.getLien(),
                fileStorageService.urlOf(doc),
                fileStorageService.originalNameOf(doc)
        );
        return ResponseEntity.ok(result);
    }
}