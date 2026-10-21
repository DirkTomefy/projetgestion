package s5.itu.projetgestion.service;

import org.springframework.stereotype.Service;

import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.postes.Affectation;
import s5.itu.projetgestion.model.postes.Employe;
import s5.itu.projetgestion.model.postes.Poste;
import s5.itu.projetgestion.repository.UserRepository;
import s5.itu.projetgestion.repository.postes.AffectationRepository;
import s5.itu.projetgestion.repository.postes.EmployeRepository;

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

        Affectation affectation = affectationRepository.findByEmploye(emp);

        if(affectation == null){
            throw new Exception("Aucune affectation pour cet employé");
        }
        return affectation.getPoste();

    }



}
