package com.ucd.exampleftp.ftp.returnzero;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;


///Returnzero

@Component
@Slf4j
public class PostAndGetTranscribe {
    @Autowired
    private GetTranscribeSample getTranscribeSample;

    @Autowired
    private PostTranscribeSample postTranscribeSample;

    public String postAndGetTranscribe(
            File file, String token
    ){

        String transcribeIdResponse;
        String response = "";

        String fileName=file.getName();
        log.info("postAndGetTranscribe에 들어온 파일 이름"+fileName);

        try {
            //json
            ObjectMapper objectMapper = new ObjectMapper();

            //ReturnZero에 파일 전송
            transcribeIdResponse = postTranscribeSample.postAudio(file, 2,token);

            log.info("transcribeIdResponse:"+transcribeIdResponse);

            JsonNode rootNode = objectMapper.readTree(transcribeIdResponse);
            JsonNode idNode= rootNode.path("id");



            String transcribeId= idNode.asText();

            log.info("transcribeId:"+transcribeId+"여기서는 id만 잘려서 나와야 함.");


            int maxRetry = 10; // 최대 재시도 횟수
            int retryCount=0;
            boolean success = false;

            while(retryCount<maxRetry &&!success) {

                response = getTranscribeSample.getTranscribe(transcribeId, token);


                if (response.contains("not found")||response.contains("\"status\":\"transcribing\"")) {
                    log.info("STT get Transcribe response:"+ response);

                    retryCount++;
                    Thread.sleep(1000);  // 2초 대기 후 재시도

                } else {
                    success = true;

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return response;



    }


}