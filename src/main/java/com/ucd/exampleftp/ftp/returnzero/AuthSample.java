///
///
/// 이 클래스에서 수행하는 작업은 주로 리턴제로 API에서 사용자의 인증을 수행하고 토큰을 받아 데이터베이스에 저장하는 것입니다.
/// 이 클래스가 속한 returnzero 디렉터리에 위치한 다른 클래스들과는 상호작용하느
///








package com.ucd.exampleftp.ftp.returnzero;

import com.ucd.exampleftp.ftp.MongoService.MongoService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Getter
@Setter
@Component
@Slf4j
public class AuthSample {

    @Value("${returnzero.client.id}")
    private String clientID;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private GetTranscribeSample getTranscribeSample;

    @Autowired
    private PostTranscribeSample postTranscribeSample;

    @Autowired
    private MongoService mongoService;

    //6시간 단위로 리턴제리 토큰을 받아오는 스케줄 클래스.
    @Scheduled(fixedRate = 21600000)
    public void fetchTokenAndStoreInDB() throws IOException{

        String token=GetAccessToken();

        /// [1]배열에 엑세스 토큰, [2]배열에 expire_at을 나눠 넣는다.

        mongoService.setRZtoken(token);



    }

    public String GetAccessToken() throws IOException {
        URL url = new URL("https://openapi.vito.ai/v1/authenticate");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("accept", "application/json");
        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpConn.setDoOutput(true);


        byte[] out = clientID.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = httpConn.getOutputStream();
        stream.write(out);

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        s.close();

        return response;

    }
}