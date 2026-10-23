package s5.itu.projetgestion.service.organisation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.organisation.UniteOrga;
import s5.itu.projetgestion.repository.organisation.PosteRepository;
import s5.itu.projetgestion.repository.organisation.UniteOrgaRepository;

/**
 * Périmètre organisationnel d'un utilisateur.
 *  - Un manager exprime des besoins et demande des postes pour l'unité de son poste et ses sous-unités
 *    (un directeur pour toute sa direction, un chef de département pour son département et ses services…).
 *  - La RH, le DG et l'administrateur peuvent le faire pour toute l'organisation.
 *  - Le supérieur direct d'un poste se trouve dans l'unité du poste ou dans une unité dont elle dépend.
 */
@Service
@Transactional(readOnly = true)
public class PerimetreService {

    private final UniteOrgaRepository uniteRepo;
    private final PosteRepository posteRepo;
    private final PostesUtilisateurService postesUtilisateur;

    public PerimetreService(UniteOrgaRepository uniteRepo, PosteRepository posteRepo, PostesUtilisateurService postesUtilisateur) {
        this.uniteRepo = uniteRepo;
        this.posteRepo = posteRepo;
        this.postesUtilisateur = postesUtilisateur;
    }

    public boolean toutesUnites(SessionUser su) {
        return su != null && (su.hasRole(Acces.RH) || su.hasRole(Acces.DG) || su.hasRole(Acces.ADMIN));
    }

    /** Unités pour lesquelles l'utilisateur peut exprimer un besoin ou demander un poste. */
    public Set<Integer> unitesDemandables(SessionUser su) {
        List<UniteOrga> toutes = uniteRepo.findAll();
        if (toutesUnites(su)) {
            Set<Integer> ids = new LinkedHashSet<>();
            toutes.forEach(u -> ids.add(u.getId()));
            return ids;
        }
        Map<Integer, List<Integer>> enfants = new HashMap<>();
        for (UniteOrga u : toutes)
            if (u.getUniteMere() != null) enfants.computeIfAbsent(u.getUniteMere().getId(), k -> new java.util.ArrayList<>()).add(u.getId());
        Set<Integer> resultat = new LinkedHashSet<>();
        Deque<Integer> file = new ArrayDeque<>();
        for (Integer idPoste : postesUtilisateur.postesDe(su.getId()))
            posteRepo.findById(idPoste).map(Poste::getUniteOrga).ifPresent(u -> file.add(u.getId()));
        while (!file.isEmpty()) {
            Integer id = file.poll();
            if (resultat.add(id)) file.addAll(enfants.getOrDefault(id, List.of()));
        }
        return resultat;
    }

    public void verifierUnite(SessionUser su, Integer idUnite) {
        if (idUnite != null && !unitesDemandables(su).contains(idUnite))
            throw new AccesRefuseException("Vous ne pouvez demander que pour votre unité et les unités qui en dépendent.");
    }

    /** L'unité elle-même et toutes celles dont elle dépend (jusqu'à la direction). */
    public Set<Integer> uniteEtAscendants(Integer idUnite) {
        Set<Integer> ids = new LinkedHashSet<>();
        UniteOrga u = idUnite == null ? null : uniteRepo.findById(idUnite).orElse(null);
        while (u != null && ids.add(u.getId())) u = u.getUniteMere();
        return ids;
    }
}
