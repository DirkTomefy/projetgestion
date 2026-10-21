package s5.itu.projetgestion.controller.besoin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.besoin.BesoinResponse;
import s5.itu.projetgestion.service.besoin.BesoinService;

@RestController 
public class BesoinController {
    
    private final BesoinService besoinService;

    public BesoinController(BesoinService besoinService){
        this.besoinService = besoinService;
    } 


    @GetMapping("/api/besoin")
    public List<BesoinResponse> list(){
        return this.besoinService.findAll();
    }


    @GetMapping("/api/besoin/{id}")
    public BesoinResponse show(@PathVariable Integer id){
        try{
            return this.besoinService.fiche(id);
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    } 

}
