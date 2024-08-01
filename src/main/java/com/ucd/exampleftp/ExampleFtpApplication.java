package com.ucd.exampleftp;

import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@SpringBootApplication // Spring Boot 애플리케이션임을 나타냄
@EnableMongoRepositories // MongoDB 리포지토리 활성화
public class ExampleFtpApplication {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri; // MongoDB URI를 가져옴

    @Value("${ftp.server.port}")
    private int ftpPort; // FTP 서버 포트를 가져옴

    public static void main(String[] args) {
        SpringApplication.run(ExampleFtpApplication.class, args); // Spring Boot 애플리케이션 시작
    }

    @Bean
    public CommandLineRunner run(GridFsTemplate gridFsTemplate) {
        return args -> {
            startFtpServer(gridFsTemplate); // 애플리케이션 시작 시 FTP 서버를 시작
        };
    }

    private void startFtpServer(GridFsTemplate gridFsTemplate) throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory(); // FTP 서버 팩토리 생성
        ListenerFactory factory = new ListenerFactory(); // 리스너 팩토리 생성
        factory.setPort(ftpPort); // 포트 설정

        // 타임아웃 설정 추가
        factory.setIdleTimeout(300); // 초 단위 설정 (여기서는 60초)

        serverFactory.addListener("default", factory.createListener()); // 리스너 추가

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor()); // 암호화 방식 설정
        UserManager um = userManagerFactory.createUserManager(); // 사용자 관리자 생성

        BaseUser user = new BaseUser();
        user.setName("ftpuser"); // 사용자 이름 설정
        user.setPassword("Str0ngP@ssw0rd!"); // 사용자 비밀번호 설정
        user.setHomeDirectory("ftp"); // 홈 디렉토리 설정

        user.setAuthorities(Collections.singletonList(new WritePermission())); // 쓰기 권한 부여
        um.save(user); // 사용자 저장

        serverFactory.setUserManager(um); // 사용자 관리자 설정
        FtpServer server = serverFactory.createServer(); // FTP 서버 생성
        server.start(); // FTP 서버 시작

        new Thread(() -> monitorFtpDirectory(gridFsTemplate)).start(); // FTP 디렉토리 모니터링을 새로운 스레드로 시작
    }

    private void monitorFtpDirectory(GridFsTemplate gridFsTemplate) {
        File ftpDir = new File("ftp"); // FTP 디렉토리 객체 생성
        if (!ftpDir.exists()) {
            ftpDir.mkdirs(); // 디렉토리가 없으면 생성
        }

        while (true) {
            File[] files = ftpDir.listFiles(); // FTP 디렉토리 내 파일 목록 가져오기
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            saveFileToGridFS(file, gridFsTemplate); // 파일을 GridFS에 저장
                            file.delete();  // 파일을 MongoDB에 저장 후 삭제
                        } catch (IOException e) {
                            e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
                        }
                    }
                }
            }
            try {
                Thread.sleep(5000); // 5초 대기 후 다시 체크
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 발생 시 현재 스레드 상태를 인터럽트로 설정
            }
        }
    }

    private void saveFileToGridFS(File file, GridFsTemplate gridFsTemplate) throws IOException {
        MongoClient mongoClient = MongoClients.create(mongoUri); // MongoDB 클라이언트 생성
        MongoDatabase database = mongoClient.getDatabase("your_database_name"); // 데이터베이스 선택
        GridFSBucket gridFSBucket = GridFSBuckets.create(database); // GridFS 버킷 생성

        try (FileInputStream inputStream = new FileInputStream(file)) {
            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(new Document("type", "audio").append("upload_date", new Date())); // 업로드 옵션 설정

            gridFSBucket.uploadFromStream(file.getName(), inputStream, options); // GridFS에 파일 업로드

            Document doc = new Document("filename", file.getName())
                    .append("uploadDate", new Date())
                    .append("path", file.getAbsolutePath());
            database.getCollection("files").insertOne(doc); // 파일 메타데이터를 MongoDB에 저장

            System.out.println("File saved to GridFS and metadata saved to MongoDB: " + file.getName());
        } finally {
            mongoClient.close(); // MongoDB 클라이언트 닫기
        }
    }
}
