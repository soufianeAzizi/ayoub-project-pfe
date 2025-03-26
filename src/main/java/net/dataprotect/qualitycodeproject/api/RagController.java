package net.dataprotect.qualitycodeproject.api;

import net.dataprotect.qualitycodeproject.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class RagController {

	@Autowired
	private RagService chatAiService;


	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String ask(
		@RequestParam(name = "question") String question,
		@RequestParam(name = "file",required = false) MultipartFile file
	) throws Exception {
		if (file != null && !file.isEmpty()) {
			return chatAiService.ragChat(question, file);
		}
		return chatAiService.ragChat(question);
	}
}
