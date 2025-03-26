package net.dataprotect.qualitycodeproject.api;

import net.dataprotect.qualitycodeproject.service.RagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/code-analysis")
@CrossOrigin(origins = "*")
public class RagController {

	private final RagService ragService;

	public RagController(RagService ragService) {
		this.ragService = ragService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> analyzeCode(
		@RequestParam("file") MultipartFile file
	) {
		try {
			if (file == null || file.isEmpty()) {
				return ResponseEntity.badRequest()
					.body("Veuillez fournir un fichier PDF valide");
			}

			if (!"application/pdf".equals(file.getContentType())) {
				return ResponseEntity.badRequest()
					.body("Seuls les fichiers PDF sont acceptés");
			}

			String analysisResult = ragService.analyzeCodeQuality(file);
			return ResponseEntity.ok(analysisResult);

		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body("Erreur lors de l'analyse du code: " + e.getMessage());
		}
	}
}