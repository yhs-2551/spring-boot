package com.yhs.blog.springboot.jpa.config.batch.scheduler;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Component
@RestController
public class BatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job cleanupTempFilesJob;

    // @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시, 초(0) 분(0) 시(1) 일(*) 월(*) 요일(?)
    // public void runCleanupJob() {
    // log.info("[BatchScheduler] runCleanupJob() 메서드 시작");
    // executeJob();
    // }

    // @PostConstruct
    // public void initializeCleanup() {
    // log.info("[BatchScheduler] initializeCleanup() 메서드 시작");
    // executeJob();
    // }

    // 테스트용 수동 실행
    @PostMapping("/api/admin/batch/cleanup")
    public void manualCleanup() {
        log.info("[BatchScheduler] manualCleanup() 메서드 시작");
        executeJob();
    }

    private void executeJob() {
        log.info("[BatchScheduler] executeJob() 메서드 시작");

        try {
            JobParameters params = new JobParametersBuilder()
                    .addDate("date", new Date()) // Job 실행마다 고유한 파라미터 생성
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(cleanupTempFilesJob, params);
            log.debug("[BatchScheduler] executeJob() 메서드 Job 실행 결과: {}", execution.getStatus());
        } catch (Exception e) {
            log.error("[BatchScheduler] executeJob() 메서드 - 임시 파일 정리 작업 실패 에러", e.getMessage());
        }
    }

}
