package s5.itu.projetgestion.service.organisation;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.recrutement.DelegationRepository;

/**
 * Postes au nom desquels un utilisateur agit aujourd'hui : ceux qu'il occupe, plus ceux des collègues
 * qui lui ont délégué leurs validations pour la période en cours.
 */
@Service
@Transactional(readOnly = true)
public class PostesUtilisateurService {

    private final UserRepository userRepo;
    private final AffectationRepository affectationRepo;
    private final DelegationRepository delegationRepo;

    public PostesUtilisateurService(UserRepository userRepo, AffectationRepository affectationRepo,
                                    DelegationRepository delegationRepo) {
        this.userRepo = userRepo;
        this.affectationRepo = affectationRepo;
        this.delegationRepo = delegationRepo;
    }

    public Set<Integer> postesDe(Integer idUser) {
        Set<Integer> postes = new LinkedHashSet<>();
        var u = idUser == null ? null : userRepo.findById(idUser).orElse(null);
        if (u == null) return postes;
        ajouter(postes, u.getIdEmploye());
        LocalDate j = LocalDate.now();
        delegationRepo.findByUserDelegueAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(u, j, j)
                .forEach(d -> ajouter(postes, d.getUserDelegant().getIdEmploye()));
        return postes;
    }

    private void ajouter(Set<Integer> postes, Integer idEmploye) {
        if (idEmploye == null) return;
        affectationRepo.findEnCoursParEmploye(idEmploye).forEach(a -> postes.add(a.getPoste().getId()));
    }
}
