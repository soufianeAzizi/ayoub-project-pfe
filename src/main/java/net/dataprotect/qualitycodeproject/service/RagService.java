package net.dataprotect.qualitycodeproject.service;

import lombok.extern.slf4j.Slf4j;
import net.dataprotect.qualitycodeproject.config.RagDataLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RagService {

	private final ChatClient chatClient;
	private final EmbeddingModel embeddingModel;
	private final RagDataLoader ragDataLoader;

	@Value("classpath:/prompts/prompt-template.st")
	private Resource promptResource;

	public RagService(ChatClient.Builder builder,
					  EmbeddingModel embeddingModel,
					  RagDataLoader ragDataLoader) {
		this.chatClient = builder.build();
		this.embeddingModel = embeddingModel;
		this.ragDataLoader = ragDataLoader;
	}

	public String analyzeCodeQuality(MultipartFile file) throws Exception {
		File tempFile = File.createTempFile("uploaded", ".pdf");
		try {
			file.transferTo(tempFile);
			Resource fileResource = new FileSystemResource(tempFile);

			SimpleVectorStore vectorStore = (SimpleVectorStore) ragDataLoader.vectorStore(embeddingModel, fileResource);
			List<Document> relevantDocuments = vectorStore.similaritySearch("code quality analysis");

			List<String> context = relevantDocuments.stream()
				.map(Document::getText)
				.toList();

			PromptTemplate promptTemplate = new PromptTemplate(promptResource);
			Prompt prompt = promptTemplate.create(Map.of("context", context));

			return chatClient.prompt(prompt).call().content();
		} finally {
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
	}
}