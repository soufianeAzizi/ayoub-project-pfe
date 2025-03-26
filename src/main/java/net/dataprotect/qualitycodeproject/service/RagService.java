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
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service @Slf4j
public class RagService {

	private ChatClient chatClient;
	private EmbeddingModel embeddingModel;
	private RagDataLoader ragDataLoader;



	public RagService(ChatClient.Builder builder, EmbeddingModel embeddingModel, RagDataLoader ragDataLoader){
		this.chatClient = builder.build();
		this.embeddingModel = embeddingModel;
		this.ragDataLoader = ragDataLoader;
	}



	@Value("classpath:/prompts/prompt-template.st")
	private Resource promptResource;


	public String ragChat(String question, MultipartFile file) throws Exception {

		File tempFile = File.createTempFile("uploaded", ".pdf");
		file.transferTo(tempFile);
		Resource fileResource = new FileSystemResource(tempFile);

		SimpleVectorStore vectorStore = (SimpleVectorStore) ragDataLoader.vectorStore(embeddingModel,fileResource);
		List<Document> relevantDocuments = vectorStore.similaritySearch(question);

		List<String> context = relevantDocuments.stream()
			.map(Document::getText)
			.toList();



		PromptTemplate promptTemplate = new PromptTemplate(promptResource);
		Prompt prompt = promptTemplate.create(Map.of("context", context, "question", question));

		String response = chatClient
			.prompt(prompt)
			.call()
			.content();

		tempFile.delete();
		return response;
	}

	public String ragChat(String question) {
		String content = chatClient.prompt()
			.user(question)
			.call()
			.content();
		return content;
	}



}
