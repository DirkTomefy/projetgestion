package s5.itu.projetgestion.service.personnel;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.model.personnel.Candidat;
import s5.itu.projetgestion.model.postes.Affectation;
import s5.itu.projetgestion.model.postes.Employe;
import s5.itu.projetgestion.repository.postes.AffectationRepository;
import s5.itu.projetgestion.repository.postes.EmployeRepository;

@Service 
public class EmployeService {
    
    private EmployeRepository employeRepository;
    private AffectationRepository affectationRepository;

    public EmployeService(EmployeRepository employeRepository, AffectationRepository affectationRepository) {
        this.employeRepository = employeRepository;
        this.affectationRepository = affectationRepository;
    }

    public void embaucher(Integer idProposition){

    }

    public void creerContrat(PropositionEmbauche propositionEmbauche, Affectation affectation){

    }

    public void creerAffectation(PropositionEmbauche propositionEmbauche,Employe emp ){
        Affectation affectation = new Affectation();
        affectation.setDateDebut(LocalDate.now());
        affectation.setEmploye(emp);
        affectation.setPoste(propositionEmbauche.getBesoinPoste().getPoste());
        this.affectationRepository.save(affectation);
    }

    public Employe creerEmployer(Candidat candidat){
        Employe emp = new Employe();
        emp.setPersonne(candidat.getPersonne());        
        emp.setMatricule(genererMatricule());
        return this.employeRepository.save(emp);
    }
    public String genererMatricule(){
        Long prochain = this.employeRepository.getNextSequenceValue();
        return "EMP000"+prochain; 
    } 

}
