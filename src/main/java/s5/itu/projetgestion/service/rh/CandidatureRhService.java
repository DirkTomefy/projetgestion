package s5.itu.projetgestion.service.rh;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.rh.CandidatureFicheDto;
import s5.itu.projetgestion.dto.rh.CandidatureResumeDto;
import s5.itu.projetgestion.dto.rh.OffreCandidaturesDto;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.rh.Candidat;
import s5.itu.projetgestion.model.rh.Candidature;
import s5.itu.projetgestion.model.rh.Document;
import s5.itu.projetgestion.model.rh.DocumentCandidat;
import s5.itu.projetgestion.model.rh.OffreEmploi;
import s5.itu.projetgestion.repository.rh.CandidatDetailRepository;
import s5.itu.projetgestion.repository.rh.CandidatureRepository;
import s5.itu.projetgestion.repository.rh.DocumentCandidatRepository;
import s5.itu.projetgestion.repository.rh.OffreEmploiRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CandidatureRhService {

    public static final Set<String> ETATS = Set.of(
            Candidature.ETAT_DEPOSEE, Candidature.ETAT_PRESELECTIONNER,
            Candidature.ETAT_REFUSE, Candidature.ETAT_DESISTE);

    private final OffreEmploiRepository offreRepo;
    private final CandidatureRepository candidatureRepo;
    private final CandidatDetailRepository candidatDetailRepo;
    private final DocumentCandidatRepository documentCandidatRepo;
    private final FileStorageService fileStorageService;

    public CandidatureRhService(OffreEmploiRepository offreRepo, CandidatureRepository candidatureRepo,
                                CandidatDetailRepository candidatDetailRepo,
                                DocumentCandidatRepository documentCandidatRepo,
                                FileStorageService fileStorageService) {
        this.offreRepo = offreRepo;
        this.candidatureRepo = candidatureRepo;
        this.candidatDetailRepo = candidatDetailRepo;
        this.documentCandidatRepo = documentCandidatRepo;
        this.fileStorageService = fileStorageService;
    }

    public List<OffreCandidaturesDto> listerOffres() {
        return offreRepo.findOffresAvecCompteurs().stream()
                .map(o -> new OffreCandidaturesDto(o.getId(), o.getRef(), o.getEtat(), o.getDateLimiteCandidature(),
                        o.getPostes(), o.getUnite(), o.getNbCandidatures().intValue(), o.getNbNouvelles().intValue()))
                .toList();
    }

    public List<CandidatureResumeDto> listerCandidatures(Integer idOffre, String etat) {
        if (!offreRepo.existsById(idOffre)) {
            throw new RessourceIntrouvableException("Offre introuvable : " + idOffre);
        }
        String filtre = etat == null || etat.isBlank() ? null : etat;
        if (filtre != null && !ETATS.contains(filtre)) {
            throw new ProjetgestionException("État de candidature inconnu : " + etat);
        }
        return candidatureRepo.findResumesByOffre(idOffre, filtre).stream()
                .map(c -> new CandidatureResumeDto(c.getId(), c.getDateDepot(), c.getEtat(), c.getIdCandidat(),
                        c.getNom(), c.getPrenom(), c.getEmail(), c.getTelephone(), c.getDateNaissance(),
                        c.getNbLignesCv().intValue(), c.getNbPieces().intValue()))
                .toList();
    }

    public CandidatureFicheDto fiche(Integer idCandidature) {
        Candidature c = candidatureRepo.findById(idCandidature)
                .orElseThrow(() -> new RessourceIntrouvableException("Candidature introuvable : " + idCandidature));
        Candidat ca = c.getCandidat();
        OffreEmploi offre = offreRepo.findById(c.getIdOffreEmploi())
                .orElseThrow(() -> new RessourceIntrouvableException("Offre introuvable : " + c.getIdOffreEmploi()));
        return new CandidatureFicheDto(c.getId(), c.getDateDepot(), c.getEtat(), offre.getId(), offre.getRef(),
                new CandidatureFicheDto.Candidat(ca.getId(), ca.getNom(), ca.getPrenom(), ca.getDateNaissance(),
                        ca.getAdresse(), ca.getEmail(), ca.getNumero()),
                lignesCv(ca.getId()), pieces(c.getId()), autresCandidatures(ca.getId(), c.getId()));
    }

    public PieceTelechargee piece(Integer idCandidature, Integer idDocument) {
        Document doc = documentCandidatRepo.findByCandidature_IdOrderByIdAsc(idCandidature).stream()
                .map(DocumentCandidat::getDocument)
                .filter(d -> d.getId().equals(idDocument))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Pièce introuvable pour cette candidature"));
        try {
            return new PieceTelechargee(fileStorageService.loadAsResource(idDocument),
                    fileStorageService.originalNameOf(doc));
        } catch (IllegalArgumentException e) {
            throw new RessourceIntrouvableException("Fichier absent du serveur");
        }
    }

    private List<CandidatureFicheDto.LigneCv> lignesCv(Integer idCandidat) {
        return candidatDetailRepo.findCvOrdonne(idCandidat).stream()
                .map(d -> new CandidatureFicheDto.LigneCv(d.getTypeInformation().getLibelle(), d.getLibelle(),
                        d.getDateDebut(), d.getDateFin()))
                .toList();
    }

    private List<CandidatureFicheDto.Piece> pieces(Integer idCandidature) {
        return documentCandidatRepo.findByCandidature_IdOrderByIdAsc(idCandidature).stream()
                .map(DocumentCandidat::getDocument)
                .map(d -> new CandidatureFicheDto.Piece(d.getId(), d.getTypeDocument().getLibelle(),
                        fileStorageService.originalNameOf(d)))
                .toList();
    }

    private List<CandidatureFicheDto.AutreCandidature> autresCandidatures(Integer idCandidat, Integer idCandidature) {
        List<Candidature> autres = candidatureRepo
                .findByCandidat_IdAndIdNotOrderByDateDepotDescIdDesc(idCandidat, idCandidature);
        Map<Integer, String> refs = offreRepo
                .findAllById(autres.stream().map(Candidature::getIdOffreEmploi).distinct().toList()).stream()
                .collect(Collectors.toMap(OffreEmploi::getId, OffreEmploi::getRef));
        return autres.stream()
                .map(a -> new CandidatureFicheDto.AutreCandidature(a.getId(), a.getIdOffreEmploi(),
                        refs.get(a.getIdOffreEmploi()), a.getDateDepot(), a.getEtat()))
                .toList();
    }

    public record PieceTelechargee(Resource fichier, String nomFichier) { }

    public static class RessourceIntrouvableException extends ProjetgestionException {
        public RessourceIntrouvableException(String message) {
            super(message);
        }
    }
}
