package s5.itu.projetgestion.service.organisation;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.organisation.EmployeFicheDto;
import s5.itu.projetgestion.dto.organisation.EmployeResumeDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.recrutement.Personne;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.EmployeRepository;
import s5.itu.projetgestion.service.service.ContratService;

/** Liste du personnel et fiche employé (identité, poste, contrats). */
@Service
public class EmployeFicheService {

    private final EmployeRepository employeRepo;
    private final AffectationRepository affectationRepo;
    private final ContratService contratService;

    public EmployeFicheService(EmployeRepository employeRepo, AffectationRepository affectationRepo,
            ContratService contratService) {
        this.employeRepo = employeRepo;
        this.affectationRepo = affectationRepo;
        this.contratService = contratService;
    }

    @Transactional(readOnly = true)
    public List<EmployeResumeDto> lister(String recherche) {
        String q = recherche == null ? "" : recherche.trim().toLowerCase(Locale.ROOT);
        return affectationRepo.findPersonnelEnPoste(q).stream()
                .map(a -> new EmployeResumeDto(a.getEmploye().getId(), a.getEmploye().getMatricule(),
                        a.getEmploye().getNom(), a.getEmploye().getPrenoms(), a.getPoste().getLibelle(),
                        a.getPoste().getUniteOrga().getLibelle(), a.getDateDebut()))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeFicheDto fiche(Integer id) {
        Employe e = employeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé introuvable : " + id));
        Personne p = e.getPersonne();   // identité complète si l'employé vient du recrutement
        Affectation a = affectationRepo.findHistorique(id).stream().findFirst().orElse(null);
        Poste poste = a == null ? null : a.getPoste();
        Poste sup = poste == null ? null : poste.getSuperieurDirect();
        return new EmployeFicheDto(e.getId(), e.getMatricule(), e.getNom(), e.getPrenoms(),
                p == null ? null : p.getDateNaissance(),
                e.getAdresse() != null ? e.getAdresse() : p == null ? null : p.getAdresse(),
                p == null ? null : p.getTelephone(), p == null ? null : p.getEmail(), p == null ? null : p.getCin(),
                poste == null ? null : poste.getId(), poste == null ? null : poste.getCode(),
                poste == null ? null : poste.getLibelle(),
                poste == null ? null : poste.getUniteOrga().getLibelle(),
                a == null ? null : a.getDateDebut(),
                sup == null ? null : sup.getLibelle(),
                contratService.listerParEmploye(id));
    }
}
