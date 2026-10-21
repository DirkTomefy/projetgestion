package s5.itu.projetgestion.service.rh;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import s5.itu.projetgestion.dto.rh.OffrePubliqueDto;
import s5.itu.projetgestion.dto.rh.TypeInformationDto;
import s5.itu.projetgestion.dto.rh.UniteFiltreDto;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.personnel.TypeInformation;
import s5.itu.projetgestion.repository.rh.OffreEmploiRepository;
import s5.itu.projetgestion.repository.personnel.TypeInformationRepository;

import java.util.List;
import java.util.Map;

@Service
public class OffrePubliqueService {

    private static final Map<String, String> LIBELLES_TYPES = Map.of(
            TypeInformation.EXPERIENCE_PRO, "Expérience professionnelle",
            TypeInformation.EDUCATION, "Formation / diplôme",
            TypeInformation.QUALITE, "Qualité / compétence");

    private final OffreEmploiRepository offreRepo;
    private final TypeInformationRepository typeInformationRepo;
    private final FileStorageService fileStorageService;

    public OffrePubliqueService(OffreEmploiRepository offreRepo, TypeInformationRepository typeInformationRepo,
                                FileStorageService fileStorageService) {
        this.offreRepo = offreRepo;
        this.typeInformationRepo = typeInformationRepo;
        this.fileStorageService = fileStorageService;
    }

    public List<OffrePubliqueDto> listerOffres(Integer idUnite) {
        return offreRepo.findOffresVisibles(null, idUnite).stream().map(OffrePubliqueService::toDto).toList();
    }

    public OffrePubliqueDto trouverOffre(Integer idOffre) {
        return offreRepo.findOffresVisibles(idOffre, null).stream().findFirst()
                .map(OffrePubliqueService::toDto)
                .orElseThrow(() -> new OffreIntrouvableException(
                        "Cette offre n'existe pas ou n'est plus ouverte aux candidatures"));
    }

    public boolean estOuverte(Integer idOffre) {
        return offreRepo.estVisible(idOffre);
    }

    public Resource ficheOffre(Integer idOffre) {
        Integer idDocument = offreRepo.findIdDocumentVisible(idOffre)
                .orElseThrow(() -> new OffreIntrouvableException("Fiche de l'offre indisponible"));
        try {
            return fileStorageService.loadAsResource(idDocument);
        } catch (IllegalArgumentException e) {
            throw new OffreIntrouvableException("Fiche de l'offre indisponible");
        }
    }

    public List<UniteFiltreDto> listerUnitesFiltre() {
        return offreRepo.findUnitesFiltre().stream()
                .map(u -> new UniteFiltreDto(u.getId(), u.getType(), u.getLibelle(), u.getProfondeur()))
                .toList();
    }

    public List<TypeInformationDto> listerTypesInformation() {
        return typeInformationRepo.findAllByOrderByIdAsc().stream()
                .map(t -> new TypeInformationDto(t.getLibelle(),
                        LIBELLES_TYPES.getOrDefault(t.getLibelle(), t.getLibelle())))
                .toList();
    }

    private static OffrePubliqueDto toDto(OffreEmploiRepository.OffrePubliqueRow o) {
        return new OffrePubliqueDto(o.getId(), o.getRef(), o.getDatePublication(), o.getDateLimiteCandidature(),
                o.getTypeContrat(), o.getUnite(), o.getDirection(), o.getPostes(),
                o.getNbPostes().intValue(), Boolean.TRUE.equals(o.getFicheDisponible()));
    }

    public static class OffreIntrouvableException extends ProjetgestionException {
        public OffreIntrouvableException(String message) {
            super(message);
        }
    }
}
