package net.dataprotect.qualitycodeproject.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.util.List;

@Component
public class RagDataLoader {
whwh
	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(1800000);
		factory.setReadTimeout(1800000);
		return new RestTemplate(factory);
	}

	public VectorStore vectorStore(EmbeddingModel embeddingModel, Resource fileFromUser) throws Exception {
		String fileHash = generateFileHash(fileFromUser.getFile());
		Path storePath = Path.of("src", "main", "resources", "store");
		Files.createDirectories(storePath);

		String fileStore = storePath.resolve(fileHash + "-store.json").toString();
		File file = new File(fileStore);

		SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

		if (file.exists()) {
			vectorStore.load(file);
			return vectorStore;
		}

		PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(fileFromUser);
		List<Document> documents = pagePdfDocumentReader.get();

		if (documents == null || documents.isEmpty()) {
			throw new IllegalArgumentException("Le document PDF ne contient pas de texte lisible ou est vide");
		}

		TextSplitter textSplitter = new TokenTextSplitter();
		List<Document> chunks = textSplitter.split(documents);

		if (chunks.isEmpty()) {
			throw new IllegalArgumentException("Aucun contenu valide n'a pu être extrait après découpage");
		}

		vectorStore.accept(chunks);
		vectorStore.save(file);
		return vectorStore;
	}

	public String generateFileHash(File file) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] fileBytes = Files.readAllBytes(file.toPath());
		byte[] hashBytes = digest.digest(fileBytes);

		StringBuilder sb = new StringBuilder();
		for (byte b : hashBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}