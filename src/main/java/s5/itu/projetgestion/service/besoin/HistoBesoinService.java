package s5.itu.projetgestion.service.besoin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.besoin.HistoBesoin;
import s5.itu.projetgestion.repository.besoin.HistoBesoinRepository;

/** Trace chaque changement d'état d'un besoin (dans la transaction de l'appelant). */
@Service
@Transactional
public class HistoBesoinService {

    public static final String CREATION = "CREATION";
    public static final String VALIDATION = "VALIDATION";
    public static final String VALIDATION_AUTO = "VALIDATION_AUTOMATIQUE";
    public static final String REFUS = "REFUS";
    public static final String OUVERTURE = "OUVERTURE_RECRUTEMENT";
    public static final String POURVU = "POURVU";
    public static final String ANNULATION = "ANNULATION";

    private final HistoBesoinRepository repo;

    public HistoBesoinService(HistoBesoinRepository repo) {
        this.repo = repo;
    }

    public void tracer(Besoin b, String avant, String apres, String action, User user, String commentaire) {
        HistoBesoin h = new HistoBesoin();
        h.setBesoin(b);
        h.setEtatAvant(avant);
        h.setEtatApres(apres);
        h.setAction(action);
        h.setUser(user);
        h.setCommentaire(commentaire == null || commentaire.isBlank() ? null : commentaire.trim());
        repo.save(h);
    }
}
