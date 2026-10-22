package s5.itu.projetgestion.controller.embauche;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import s5.itu.projetgestion.service.personnel.EmployeService;

@RestController
@RequestMapping("/api/embauches")
public class EmbaucheController {

	private final EmployeService employeService;

	public EmbaucheController(EmployeService employeService) {
		this.employeService = employeService;
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> embaucher(
			@RequestParam(required = false) Integer idProposition) {
		if (idProposition == null) {
			return ResponseEntity.badRequest().body(Map.of(
					"error", "L'identifiant de la proposition est obligatoire."));
		}

		try {
			employeService.embaucher(idProposition);
			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
					"message", "Embauche créée avec succès.",
					"idProposition", idProposition));
		} catch (IllegalArgumentException e) {
			if (e.getMessage() != null && e.getMessage().contains("introuvable")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
						"error", e.getMessage()));
			}
			return ResponseEntity.badRequest().body(Map.of(
					"error", messageOf(e, "Données d'embauche invalides.")));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
					"error", messageOf(e, "La proposition ne peut pas être embauchée.")));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
					"error", "Erreur interne lors de l'embauche."));
		}
	}

	@ExceptionHandler
	public ResponseEntity<Map<String, String>> handleMissingParameter(
			org.springframework.web.bind.MissingServletRequestParameterException e) {
		return ResponseEntity.badRequest().body(Map.of(
				"error", "Le paramètre idProposition est obligatoire."));
	}

	private static String messageOf(RuntimeException exception, String fallback) {
		return exception.getMessage() == null || exception.getMessage().isBlank()
				? fallback : exception.getMessage();
	}


}
