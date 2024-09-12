package com.ucd.exampleftp;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.ucd.exampleftp.ftp.FtpServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication // Spring Boot 애플리케이션임을 나타냄
@EnableMongoRepositories // MongoDB 리포지토리 활성화
@Slf4j
@EnableScheduling
public class ExampleFtpApplication {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri; // MongoDB URI를 가져옴

    @Autowired
    private FtpServerConfig ftpServerConfig;

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(ExampleFtpApplication.class, args);


    }

    @Bean
    public CommandLineRunner run(GridFsTemplate gridFsTemplate) {
        return args -> {
            // FTP 서버 시작
            ftpServerConfig.startFtpServer(gridFsTemplate);

        };
    }

}

