package com.ucd.exampleftp.ftp;

import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.ucd.exampleftp.MongoService.MongoService;
import com.ucd.exampleftp.returnzero.PostAndGetTranscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class FtpServerConfig {

    @Value("${ftp.server.port}")
    private int ftpPort; // FTP 서버 포트를 가져옴

    @Autowired
    PostAndGetTranscribe postAndGetTranscribe;

    private final MongoService mongoService;

    public FtpServerConfig(MongoService mongoService) {
        this.mongoService = mongoService;
    }


    public void startFtpServer(GridFsTemplate gridFsTemplate) throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory(); // FTP 서버 팩토리 생성
        ListenerFactory factory = new ListenerFactory(); // 리스너 팩토리 생성
        factory.setPort(ftpPort); // 포트 설정

        // 패시브 모드 설정
        DataConnectionConfigurationFactory dataConnConfigFactory = new DataConnectionConfigurationFactory();
        dataConnConfigFactory.setPassivePorts("21100-21110");
        factory.setDataConnectionConfiguration(dataConnConfigFactory.createDataConnectionConfiguration());

        serverFactory.addListener("default", factory.createListener()); // 리스너 추가

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor()); // 암호화 방식 설정

        UserManager um = userManagerFactory.createUserManager(); // 사용자 관리자 생성

        BaseUser user = new BaseUser();
        user.setName("ftpuser"); // 사용자 이름 설정
        user.setPassword("Str0ngP@ssw0rd!"); // 사용자 비밀번호 설정
        log.info("user password:"+user.getPassword());
        user.setHomeDirectory("ftp"); // 홈 디렉토리 설정
        user.setAuthorities(Collections.singletonList(new WritePermission())); // 쓰기 권한 부여

        userManagerFactory.createUserManager().save(user); // 사용자 저장
        serverFactory.setUserManager(userManagerFactory.createUserManager()); // 사용자 관리자 설정

        FtpServer server = serverFactory.createServer(); // FTP 서버 생성
        server.start(); // FTP 서버 시작

        new Thread(() -> monitorFtpDirectory(gridFsTemplate)).start(); // FTP 디렉토리 모니터링을 새로운 스레드로 시작
    }

    private void monitorFtpDirectory(GridFsTemplate gridFsTemplate) {
        File ftpDir = new File("ftp"); // FTP 디렉토리 객체 생성

        if (!ftpDir.exists()) {
            log.error("There is no ftp directory ***********************************");
            ftpDir.mkdirs(); // 디렉토리가 없으면 생성
        }

        while (true) {
            String token = mongoService.getTokenFromDB("returnzero_token");
            File[] files = ftpDir.listFiles(); // FTP 디렉토리 내 파일 목록 가져오기
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            saveFileToGridFS(file, gridFsTemplate); // 파일을 GridFS에 저장
                            postAndGetTranscribe.postAndGetTranscribe(file,token);
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
        try (FileInputStream inputStream = new FileInputStream(file)) {
            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(new org.bson.Document("type", "audio").append("upload_date", new Date())); // 업로드 옵션 설정

            gridFsTemplate.store(inputStream, file.getName(), options.getMetadata()); // GridFS에 파일 업로드

            System.out.println("File saved to GridFS and metadata saved to MongoDB: " + file.getName());
        }
    }




}