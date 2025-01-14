package com.yhs.blog.springboot.jpa.config.batch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * AWS S3의 임시 파일을 정리하기 위한 Spring Batch 설정 클래스.
 * 이 클래스는 주기적으로 오래된 임시 파일을 확인하고 제거하는 배치 작업을 설정한다.
 */

/**
 * 임시 파일 정리를 위한 메인 배치 작업을 생성하고 설정한다.
 * RunIdIncrementer를 사용하여 고유한 작업 인스턴스를 보장.
 *
 * @return 임시 파일 정리를 위한 구성된 Job 인스턴스
 */

/**
 * 임시 파일 정리를 위한 단계를 구성한다.
 * 이 단계는 S3Objects를 10개 단위로 처리한다.
 *
 * @return 임시 파일 정리를 위한 구성된 Step 인스턴스
 */

/**
 * 임시 디렉토리에서 S3Objects를 읽는 ItemReader를 생성한다.
 * 대용량 디렉토리의 효율적인 처리를 위해 연속 토큰을 사용한 페이지네이션을 구현.
 *
 * @return S3 객체를 읽기 위한 ItemReader 인스턴스
 */

/**
 * S3Objects를 날짜에 따라 필터링하는 ItemProcessor를 생성.
 * 30일 이상 된 객체들이 삭제 대상으로 설정.
 *
 * @return 오래된 S3 객체를 필터링하기 위한 ItemProcessor 인스턴스
 */

/**
 * 필터링된 S3Objects를 삭제하는 ItemWriter를 생성.
 * S3 버킷에서 객체들의 실제 삭제를 수행. 
 *
 * @return S3 객체를 삭제하기 위한 ItemWriter 인스턴스
 */

@Configuration
@RequiredArgsConstructor
@Log4j2
public class BatchConfig {

    private final JobRepository jobRepository;
    private final S3Client s3Client;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private List<String> userBlogIds;


    @PostConstruct
    public void init() {
        userBlogIds = userRepository.findAll().stream().map(user -> user.getBlogId()).toList();
    }


    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Bean
    public Job cleanupTempFilesJob() {
        return new JobBuilder("cleanupTempFilesJob", jobRepository)
            .start(cleanupTempFilesStep())
            .incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    public Step cleanupTempFilesStep() {
        return new StepBuilder("cleanupTempFilesStep", jobRepository)
            .<S3Object, S3Object>chunk(10, transactionManager)
            .reader(s3TempFileReader())
            .processor(s3TempFileProcessor())
            .writer(s3TempFileWriter())
            .build();
    }

    @Bean
    public ItemReader<S3Object> s3TempFileReader() {
        // 익명 클래스로 ItemReader 구현
        return new ItemReader<S3Object>() {
            private ListObjectsV2Response currentResponse;
            private Iterator<S3Object> iterator;
            private String continuationToken;
            private int currentUserIndex = 0;

            @Override
            public S3Object read() {
                if (iterator == null || !iterator.hasNext()) { // 초기 iterator는 null
                    currentResponse = fetchNextBatch(); // 10개를 가져옴 위쪽에 chunkSize = 10
                    if (currentResponse == null)
                        return null;
                    iterator = currentResponse.contents().iterator();
                }
                return iterator.hasNext() ? iterator.next() : null; //10개 중 하나씩 읽어들임. 1개씩 processor로 전달
            }

            private ListObjectsV2Response fetchNextBatch() { // 다음 10개 batch를 가져옴
                try {

                    if (currentUserIndex >= userBlogIds.size()) {
                        return null;  // 모든 사용자 처리 완료
                    }

                    String currentUserBlogId = userBlogIds.get(currentUserIndex);


                    ListObjectsV2Request request = ListObjectsV2Request.builder()
                            .bucket(bucketName)
                            .prefix(currentUserBlogId + "/temp/") // 전체 스캔
                            .continuationToken(continuationToken) // continuationToken초기값은 null
                            .build();

                    ListObjectsV2Response response = s3Client.listObjectsV2(request);

                    log.debug("조회된 객체 수: {}", response.contents().size());

                    if (!response.hasContents() && continuationToken == null) {
                        currentUserIndex++;  // 다음 사용자로
                        return fetchNextBatch();
                    }

                    continuationToken = response.nextContinuationToken();
                    return response;
                } catch (Exception e) {
                    log.error("S3 파일 조회 실패", e);
                    return null;
                }
            }
        };
    }

    @Bean
    public ItemProcessor<S3Object, S3Object> s3TempFileProcessor() {
        return item -> {
            String key = item.key();

            log.info("s3TempFileProcessor로 넘어온 파일 {}", key); 

            // {userId}/temp/ 패턴 확인
            if (key.contains("/temp/")) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime fileDate = LocalDateTime.ofInstant(
                    item.lastModified(),
                    ZoneId.systemDefault()
                );
                
                if (ChronoUnit.DAYS.between(fileDate, now) >= 7) {
                    return item;
                }
            }
            return null;
        };
    }
    // 10개 모이션 writer에서 삭제
    @Bean
    public ItemWriter<S3Object> s3TempFileWriter() {
        return items -> {
            for (S3Object item : items) {
                log.info("삭제 시도 파일: {}", item.key());
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(item.key())
                        .build();
                s3Client.deleteObject(deleteRequest);

                log.info("삭제 완료 파일: {}", item.key());

            }
        };
    }

}
