package com.ucd.exampleftp.ftp;

import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.ucd.exampleftp.ftp.MongoService.MongoService;
import com.ucd.exampleftp.ftp.returnzero.PostAndGetTranscript;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class FtpServerConfig {

    @Value("${ftp.server.port}")
    private int ftpPort; // FTP 서버 포트를 가져옴

    @Autowired
    PostAndGetTranscript postAndGetTranscript;

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
            log.info("There is no ftp directory. Ftp file will be crated automatically");
            ftpDir.mkdirs(); // 디렉토리가 없으면 생성
        }

        while (true) {
            String token = mongoService.getRZtoken("returnzero_token");



            File[] files = ftpDir.listFiles(); // FTP 디렉토리 내 파일 목록 가져오기
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            ///첫번째 meeting_id
                            ///두번째 참여자 수
                            ///세번째 파일 카운트
                            List<String> fileInfo= saveFileToGridFS(file, gridFsTemplate); // 파일을 GridFS에 저장
                            String response_stt= postAndGetTranscript
                                    .postAndGetTranscribe(file,token,fileInfo.get(0),fileInfo.get(1),fileInfo.get(2));

                            log.info("\n\n"+"response is here:"+response_stt+"\n\n");

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

    private List<String> saveFileToGridFS(File file, GridFsTemplate gridFsTemplate) throws IOException {

        log.info(file.getName());

        List<String> fileInfos = new ArrayList<>(List.of(file.getName().split("%")));



        for(String str:fileInfos){
            ///첫번째 meeting_id
            ///두번째 참여자 수
            ///세번째 파일 카운트
            log.info("str"+str);
        }

        fileInfos.set(2, fileInfos.get(2).split(".wav")[0]);





        try (FileInputStream inputStream = new FileInputStream(file)) {
            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(new org.bson.Document("type", "audio").append("upload_date", new Date())); // 업로드 옵션 설정

            gridFsTemplate.store(inputStream, fileInfos.get(0), options.getMetadata()); // GridFS에 파일 업로드

            System.out.println("File saved to GridFS and metadata saved to MongoDB: " + file.getName());
        }

        return fileInfos;
    }




}
