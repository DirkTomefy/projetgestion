package s5.itu.projetgestion.service.onboarding;

import org.springframework.stereotype.Service;

@Service
public class OnboardingService {
    public void demarrer(Integer idContrat) {
        System.out.println("[BOUCHON] Onboarding démarré pour le contrat " + idContrat);
    }
}