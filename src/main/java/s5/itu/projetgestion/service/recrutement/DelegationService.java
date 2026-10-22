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

    public DelegationService(DelegationRepository delegationRepository) {
        this.delegationRepository = delegationRepository;
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
        return this.delegationRepository.save(delegation);
    }
    
}
