package s5.itu.projetgestion.service.besoin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.besoin.BesoinResponse;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;

@Service
public class BesoinService {

    private final BesoinRepository besoinRepository;

    public BesoinService(BesoinRepository besoinRepository) {
        this.besoinRepository = besoinRepository;
    }

    public Besoin getById(Integer id) throws Exception {
        Besoin besoin = besoinRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new Exception("Besoin Introuvable"));
                return besoin;
    }

    @Transactional(readOnly = true)
    public BesoinResponse fiche(Integer id) throws Exception {
        Besoin besoin = besoinRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new Exception("Besoin Introuvable"));

        return new BesoinResponse().fromBesoin(besoin);
    }

}
