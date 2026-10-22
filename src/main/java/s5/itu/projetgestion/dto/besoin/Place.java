package s5.itu.projetgestion.dto.besoin;

import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;

public record Place(Poste poste, Employe remplace) {}