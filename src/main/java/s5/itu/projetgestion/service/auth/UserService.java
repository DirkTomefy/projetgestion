package s5.itu.projetgestion.service.auth;

import org.springframework.stereotype.Service;

import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.EmployeRepository;

@Service 
public class UserService {
    
    private final EmployeRepository employeRepository;
    private final AffectationRepository affectationRepository;
    private final UserRepository userRepository;

    public UserService(EmployeRepository employeRepository, AffectationRepository affectationRepository, UserRepository userRepository) {
        this.employeRepository = employeRepository;
        this.affectationRepository = affectationRepository;
        this.userRepository = userRepository;
    }

    public User getById(int id){
        return this.userRepository.findById(id).orElse(null);
    }

    
    public Poste getPosteByUser(User u) throws Exception{

        if(u.getIdEmploye() == null){
            throw new Exception("Utilisateur associé à aucun employé");
        }
        Employe emp = employeRepository.findById(u.getIdEmploye()).orElse(null);

        // affectation en cours (un employé muté a plusieurs affectations dans son historique)
        Affectation affectation = emp == null ? null
                : affectationRepository.findEnCoursParEmploye(emp.getId()).stream().findFirst().orElse(null);

        if(affectation == null){
            throw new Exception("Aucune affectation pour cet employé");
        }
        return affectation.getPoste();

    }



}
