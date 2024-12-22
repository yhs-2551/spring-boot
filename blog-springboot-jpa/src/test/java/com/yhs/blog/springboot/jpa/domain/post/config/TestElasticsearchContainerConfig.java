package com.yhs.blog.springboot.jpa.domain.post.config;

import java.io.IOException;
import java.time.Duration;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@TestConfiguration
@Testcontainers
@Profile("test")
@EnableElasticsearchRepositories(basePackageClasses = PostSearchRepository.class)
public class TestElasticsearchContainerConfig {

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.16.1"))
            .withExposedPorts(9200)
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withEnv("xpack.security.transport.ssl.enabled", "false")
            .withEnv("xpack.security.enrollment.enabled", "false")
            .withEnv("xpack.ml.enabled", "false")
            .withCreateContainerCmdModifier(cmd -> cmd
                    .withCmd("bash", "-c",
                            "elasticsearch-plugin install --batch analysis-nori && /usr/local/bin/docker-entrypoint.sh elasticsearch"))
            .waitingFor(Wait.forHttp("/")
                    .forPort(9200)
                    .withStartupTimeout(Duration.ofMinutes(5)));
    static {
        elasticsearchContainer.start();
        createPostsIndex();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient httpClient = RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(),
                        elasticsearchContainer.getMappedPort(9200), "http"))
                .build();

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new ElasticsearchClient(
                new RestClientTransport(
                        httpClient,
                        new JacksonJsonpMapper(objectMapper)));
    }

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris",
                () -> "http://" + elasticsearchContainer.getHost() + ":"
                        + elasticsearchContainer.getMappedPort(9200));

    }

    private static void createPostsIndex() {
        try {
            RestClient restClient = RestClient.builder(
                    new HttpHost(elasticsearchContainer.getHost(),
                            elasticsearchContainer.getMappedPort(9200), "http"))
                    .build();

            String indexSettings = """
                                        {
                        "settings": {
                            "analysis": {
                                "tokenizer": {
                                    "my_tokenizer": {
                                        "type": "ngram",
                                        "min_gram": 1,
                                        "max_gram": 2,
                                        "token_chars": ["letter", "digit"]
                                    }
                                },
                                "analyzer": {
                                    "my_analyzer": {
                                        "type": "custom",
                                        "tokenizer": "my_tokenizer"
                                    }
                                }
                            }
                        },
                        "mappings": {
                            "properties": {
                                "_class": {
                                    "type": "keyword",
                                    "index": false
                                },
                                "id": {
                                    "type": "keyword"
                                },
                                "userId": {
                                    "type": "keyword"
                                },
                                "categoryId": {
                                    "type": "keyword"
                                },
                                "title": {
                                    "type": "text",
                                    "fields": {
                                        "keyword": {
                                            "type": "keyword",
                                            "ignore_above": 256
                                        },
                                        "ngram": {
                                            "type": "text",
                                            "analyzer": "my_analyzer"
                                        }
                                    }
                                },
                                "content": {
                                    "type": "text",
                                    "fields": {
                                        "keyword": {
                                            "type": "keyword",
                                            "ignore_above": 256
                                        },
                                        "ngram": {
                                            "type": "text",
                                            "analyzer": "my_analyzer"
                                        }
                                    }
                                },
                                "username": {
                                    "type": "keyword"
                                },
                                "createdAt": {
                                    "type": "date"
                                },
                                "category": {
                                    "properties": {
                                        "id": {
                                            "type": "keyword"
                                        },
                                        "name": {
                                            "type": "keyword"
                                        }
                                    }
                                },
                                "featuredImage": {
                                    "properties": {
                                        "id": {
                                            "type": "keyword"
                                        },
                                        "fileName": {
                                            "type": "keyword"
                                        },
                                        "fileUrl": {
                                            "type": "keyword"
                                        },
                                        "fileType": {
                                            "type": "keyword"
                                        },
                                        "fileSize": {
                                            "type": "long"
                                        }
                                    }
                                }
                            }
                        }
                    }
                                        """;

            // 기존 인덱스 삭제
            Request deleteRequest = new Request("DELETE", "/posts");
            try {
                restClient.performRequest(deleteRequest);
            } catch (IOException e) {
                // 인덱스가 없는 경우 무시
            }

            // 새 인덱스 생성
            Request createRequest = new Request("PUT", "/posts");
            createRequest.setJsonEntity(indexSettings);
            restClient.performRequest(createRequest);

        } catch (IOException e) {
            throw new RuntimeException("Failed to create elasticsearch index", e);
        }
    }
}