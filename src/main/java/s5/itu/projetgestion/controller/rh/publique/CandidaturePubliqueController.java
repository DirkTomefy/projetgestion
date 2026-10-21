package s5.itu.projetgestion.controller.rh.publique;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import s5.itu.projetgestion.dto.rh.CandidatureDeposeeResponse;
import s5.itu.projetgestion.dto.rh.CandidatureRequest;
import s5.itu.projetgestion.service.rh.CandidaturePubliqueService;

@RestController
@RequestMapping("/api/public/candidatures")
public class CandidaturePubliqueController {

    private final CandidaturePubliqueService service;

    public CandidaturePubliqueController(CandidaturePubliqueService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CandidatureDeposeeResponse deposer(@Valid @RequestPart("candidature") CandidatureRequest candidature,
                                              @RequestPart(value = "cv", required = false) MultipartFile cv) {
        return service.deposer(candidature, cv);
    }
}
