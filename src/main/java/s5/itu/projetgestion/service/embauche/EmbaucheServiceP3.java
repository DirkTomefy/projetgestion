package s5.itu.projetgestion.service.embauche;

import org.springframework.stereotype.Service;
import s5.itu.projetgestion.repository.embauche.PropositionEmbaucheRepository;
import s5.itu.projetgestion.service.recrutement.EmployeService;

/**
 * Branche l'embauche de P3 (EmployeService.embaucher) sur l'interface EmbaucheService appelée
 * quand le candidat accepte toutes les conditions. S'exécute dans la transaction de l'acceptation :
 * si l'embauche échoue, l'acceptation est annulée avec elle.
 */
@Service
public class EmbaucheServiceP3 implements EmbaucheService {

    private final EmployeService employeService;
    private final PropositionEmbaucheRepository propositionRepo;

    public EmbaucheServiceP3(EmployeService employeService, PropositionEmbaucheRepository propositionRepo) {
        this.employeService = employeService;
        this.propositionRepo = propositionRepo;
    }

    @Override
    public Integer embaucher(Integer idProposition) {
        employeService.embaucher(idProposition);
        propositionRepo.flush();
        return propositionRepo.findIdContrat(idProposition)
                .orElseThrow(() -> new IllegalStateException(
                        "L'embauche n'a pas créé de contrat pour la proposition " + idProposition));
    }
}
