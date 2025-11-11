package com.swp.project.config;

import io.pinecone.clients.Index;
import io.pinecone.configs.PineconeConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PineconeConfig {

    @Value("${spring.ai.vectorstore.pinecone.apiKey}")
    private String pineconeApiKey;
    @Value("${spring.ai.vectorstore.pinecone.index-name}")
    private String pineconeIndexName;
    @Value("${pinecone.index-host}")
    private String pineconeIndexHost;

    @Bean
    public Index pineconeIndex() {
        io.pinecone.configs.PineconeConfig config = new io.pinecone.configs.PineconeConfig(pineconeApiKey);
        config.setHost(pineconeIndexHost);
        return new Index(config, new PineconeConnection(config), pineconeIndexName);
    }
}
