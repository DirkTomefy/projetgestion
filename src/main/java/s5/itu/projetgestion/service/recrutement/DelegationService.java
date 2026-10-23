package s5.itu.projetgestion.service.recrutement;

import java.time.LocalDate;
import java.util.Objects;

import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.recrutement.Delegation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.repository.recrutement.DelegationRepository;

@Service
public class DelegationService {

    private final DelegationRepository delegationRepository;
    private final s5.itu.projetgestion.service.notification.NotificationService notificationService;

    public DelegationService(DelegationRepository delegationRepository,
                             s5.itu.projetgestion.service.notification.NotificationService notificationService) {
        this.delegationRepository = delegationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Delegation creerDelegation(Delegation delegation) {
        if (delegation == null || delegation.getUserDelegant() == null
                || delegation.getUserDelegue() == null) {
            throw new IllegalArgumentException("Le délégant et le délégué sont obligatoires");
        }
        if (delegation.getUserDelegant() == delegation.getUserDelegue()
                || (delegation.getUserDelegant().getId() != null
                && delegation.getUserDelegant().getId().equals(delegation.getUserDelegue().getId()))) {
            throw new IllegalArgumentException("Impossible de se déléguer à soi-même");
        }
        if (delegation.getDateDebut() == null || delegation.getDateFin() == null
                || delegation.getDateFin().isBefore(delegation.getDateDebut())) {
            throw new IllegalArgumentException("La période de délégation est invalide");
        }
        boolean chevauchement = delegationRepository
                .findByUserDelegantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                        delegation.getUserDelegant(), delegation.getDateFin(), delegation.getDateDebut())
                .stream()
                .anyMatch(existante -> !Objects.equals(existante.getId(), delegation.getId()));
        if (chevauchement) {
            throw new IllegalArgumentException("Deux délégations du même valideur ne peuvent pas se chevaucher");
        }
        Delegation enregistree = this.delegationRepository.save(delegation);
        notificationService.notifier(delegation.getUserDelegue().getId(), "DELEGATION",
                nom(delegation.getUserDelegant()) + " vous délègue ses validations du " + jour(delegation.getDateDebut())
                        + " au " + jour(delegation.getDateFin()), "/delegations");
        return enregistree;
    }
    

    @Transactional(readOnly = true)
    public java.util.List<s5.itu.projetgestion.dto.recrutement.DelegationDto> lister(Integer idUser) {
        java.time.LocalDate j = java.time.LocalDate.now();
        return delegationRepository.rechercher(idUser == null ? 0 : idUser).stream().map(d -> new s5.itu.projetgestion.dto.recrutement.DelegationDto(
                d.getId(), d.getUserDelegant().getId(), nom(d.getUserDelegant()), d.getUserDelegue().getId(), nom(d.getUserDelegue()),
                d.getDateDebut(), d.getDateFin(),
                d.getDateDebut().isAfter(j) ? "A_VENIR" : d.getDateFin().isBefore(j) ? "TERMINEE" : "EN_COURS")).toList();
    }

    /** Fin anticipée par le délégant (ou la RH) : supprimée si elle n'a pas commencé, sinon arrêtée hier. */
    @Transactional
    public void annuler(Integer id, s5.itu.projetgestion.dto.auth.SessionUser su) {
        Delegation d = delegationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Délégation introuvable : " + id));
        if (!d.getUserDelegant().getId().equals(su.getId()) && !su.hasRole("RH") && !su.hasRole("ADMIN"))
            throw new s5.itu.projetgestion.err.AccesRefuseException("Seul le délégant peut mettre fin à sa délégation.");
        java.time.LocalDate j = java.time.LocalDate.now();
        if (d.getDateFin().isBefore(j)) throw new IllegalStateException("Cette délégation est déjà terminée.");
        if (d.getDateDebut().isAfter(j.minusDays(1))) delegationRepository.delete(d);
        else d.setDateFin(j.minusDays(1));
    }

    private static String nom(s5.itu.projetgestion.model.auth.User u) {
        return u.getNomComplet() == null ? u.getLogin() : u.getNomComplet() + " (" + u.getLogin() + ")";
    }

    private static String jour(java.time.LocalDate d) {
        return d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
