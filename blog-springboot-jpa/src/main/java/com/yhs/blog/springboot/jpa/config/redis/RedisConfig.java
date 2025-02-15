package com.yhs.blog.springboot.jpa.config.redis;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; 
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPublicProfileResponse;
 
@Configuration
// @ConfigurationProperties("spring.data.redis")  private String host;  private int port; 와 같이 사용할 수 있지만 일단 기존 방식 사용(ConfigurationProperties방식이 더 선호되는 듯 함)
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, Boolean> redisTemplate() {
        RedisTemplate<String, Boolean> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, UserPublicProfileResponse> userPublicProfileRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UserPublicProfileResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<UserPublicProfileResponse> serializer = new Jackson2JsonRedisSerializer<>(
                objectMapper,
                UserPublicProfileResponse.class);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, List<CategoryWithChildrenResponse>> userCategoriesRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, List<CategoryWithChildrenResponse>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // CollectionType 사용하여 List 타입 지정
        CollectionType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, CategoryWithChildrenResponse.class);

        Jackson2JsonRedisSerializer<List<CategoryWithChildrenResponse>> serializer = new Jackson2JsonRedisSerializer<>(
                objectMapper,
                listType);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    // @Bean
    // public RedisTemplate<String, UserPrivateProfileResponse>
    // userPrivateProfileRedisTemplate(
    // RedisConnectionFactory connectionFactory) {
    // RedisTemplate<String, UserPrivateProfileResponse> redisTemplate = new
    // RedisTemplate<>();
    // redisTemplate.setConnectionFactory(connectionFactory);

    // ObjectMapper objectMapper = new ObjectMapper();
    // objectMapper.registerModule(new JavaTimeModule());

    // Jackson2JsonRedisSerializer<UserPrivateProfileResponse> serializer = new
    // Jackson2JsonRedisSerializer<>(
    // objectMapper,
    // UserPrivateProfileResponse.class);

    // redisTemplate.setKeySerializer(new StringRedisSerializer());
    // redisTemplate.setValueSerializer(serializer);

    // redisTemplate.afterPropertiesSet();
    // return redisTemplate;
    // }

    // 2. String, Object 용 RedisTemplate (일반 객체)
    // @Bean
    // public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory
    // connectionFactory) {
    // RedisTemplate<String, Object> template = new RedisTemplate<>();
    // template.setConnectionFactory(connectionFactory);

    // // key: String 형식으로 직렬화
    // template.setKeySerializer(new StringRedisSerializer());

    // // value: JSON 형식으로 직렬화 (모든 객체 타입 지원)
    // template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

    // // Hash 작업을 위한 직렬화 설정
    // template.setHashKeySerializer(new StringRedisSerializer());
    // template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    // return template;
    // }

    // 3. String, User 전용 RedisTemplate
    // @Bean
    // public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory
    // connectionFactory) {
    // RedisTemplate<String, User> redisTemplate = new RedisTemplate<>();
    // redisTemplate.setConnectionFactory(connectionFactory);

    // ObjectMapper objectMapper = new ObjectMapper();
    // objectMapper.registerModule(new JavaTimeModule());
    // // 1711031400000같은 밀리초 단위 비활성화, ISO8601 형식의 문자열(2024-03-21T14:30:00)로 변환
    // // json 내부에서 ISO 형식 사용
    // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // // @JsonIgnore 적용
    // objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());

    // // setObjectMapper는 deprecated되어서 이렇게 생성자 방식을 사용해야함.
    // Jackson2JsonRedisSerializer<User> serializer = new
    // Jackson2JsonRedisSerializer<>(objectMapper, User.class);

    // // key: String 형식으로 직렬화
    // redisTemplate.setKeySerializer(new StringRedisSerializer());

    // // value: User 객체 전용 JSON 직렬화
    // redisTemplate.setValueSerializer(serializer);

    // // Hash 작업을 위한 직렬화 설정, opsForHash() 사용 시 두번째 및 세번째 파라미터 직렬화. 첫번째 파라미터는 위에서
    // // 설정한
    // // String 형식 직렬화 사용
    // redisTemplate.setHashKeySerializer(new StringRedisSerializer());
    // redisTemplate.setHashValueSerializer(serializer);

    // // 모든 설정이 완료된 후 초기화
    // // 설정 유효성 검사
    // // 필수 속성 확인
    // redisTemplate.afterPropertiesSet();
    // return redisTemplate;
    // }
}
