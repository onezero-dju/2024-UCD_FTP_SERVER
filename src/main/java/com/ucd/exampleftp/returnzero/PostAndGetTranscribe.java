package com.ucd.exampleftp.returnzero;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

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


        String fileName=file.getName();
        log.info(fileName);

        try {
            String transcribeIdResponse;
            String response;
            //json
            ObjectMapper objectMapper = new ObjectMapper();

            transcribeIdResponse = postTranscribeSample.postAudio(file, 2,token);

            JsonNode rootNode = objectMapper.readTree(transcribeIdResponse);
            JsonNode idNode= rootNode.path("id");
            String transcribeId= idNode.asText();

            log.info("transcribeId:"+transcribeId);


            int maxRetry = 5; // 최대 재시도 횟수
            int retryCount=0;
            boolean success = false;

            while(retryCount<maxRetry &&!success) {

                response = getTranscribeSample.getTranscribe(transcribeId, token);


                if (response.contains("not found")||response.contains("\"status\":\"transcribing\"")) {
                    log.info("H0004 code received, retrying...");
                    retryCount++;
                    Thread.sleep(2000);  // 2초 대기 후 재시도

                } else {
                    success = true;
                    log.info("response below");
                    log.info(response);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "done";



    }


}
