package com.yhs.blog.springboot.jpa.config.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@RequiredArgsConstructor
@Log4j2
@EnableElasticsearchRepositories(basePackageClasses = PostSearchRepository.class)
@Profile("!test")
public class ElasticsearchConfig {

        // LocalDateTime을 올바르게 역직렬화 하기 위해 아래와 같은 설정 필요.
        @Bean
        public ElasticsearchClient elasticsearchClient() {
                RestClient httpClient = RestClient.builder(
                                HttpHost.create("http://localhost:9200")).build();

                ObjectMapper objectMapper = new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                return new ElasticsearchClient(
                                new RestClientTransport(
                                                httpClient,
                                                new JacksonJsonpMapper(objectMapper)));
        }
}
