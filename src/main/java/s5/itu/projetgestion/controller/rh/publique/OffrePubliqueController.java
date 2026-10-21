package s5.itu.projetgestion.controller.rh.publique;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.dto.rh.OffrePubliqueDto;
import s5.itu.projetgestion.dto.rh.TypeInformationDto;
import s5.itu.projetgestion.dto.rh.UniteFiltreDto;
import s5.itu.projetgestion.service.rh.OffrePubliqueService;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class OffrePubliqueController {

    private final OffrePubliqueService service;

    public OffrePubliqueController(OffrePubliqueService service) {
        this.service = service;
    }

    @GetMapping("/offres")
    public List<OffrePubliqueDto> offres(@RequestParam(required = false) Integer idUnite) {
        return service.listerOffres(idUnite);
    }

    @GetMapping("/offres/{id}")
    public OffrePubliqueDto offre(@PathVariable Integer id) {
        return service.trouverOffre(id);
    }

    @GetMapping("/offres/{id}/fiche")
    public ResponseEntity<Resource> fiche(@PathVariable Integer id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"offre_" + id + ".pdf\"")
                .body(service.ficheOffre(id));
    }

    @GetMapping("/unites-filtre")
    public List<UniteFiltreDto> unitesFiltre() {
        return service.listerUnitesFiltre();
    }

    @GetMapping("/types-information")
    public List<TypeInformationDto> typesInformation() {
        return service.listerTypesInformation();
    }
}
