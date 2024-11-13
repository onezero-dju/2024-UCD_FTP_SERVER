package com.ucd.exampleftp.ftp.returnzero;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucd.exampleftp.STT.db.STTResponse;
import com.ucd.exampleftp.STT.service.STTResponseService;
import com.ucd.exampleftp.meeting.service.MeetingService;
import com.ucd.exampleftp.meeting.service.MeetingSseService;
import com.ucd.exampleftp.util.config.rabbitMQ.RabbitMQSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Component
@Slf4j
public class PostAndGetTranscribe {

    @Autowired
    private GetTranscribeSample getTranscribeSample;

    @Autowired
    private PostTranscribeSample postTranscribeSample;

    @Autowired
    private MeetingSseService meetingSseService;

    @Autowired
    private MeetingService meetingService;



    @Autowired
    private STTResponseService sttResponseService;

    @Autowired
    private ObjectMapper objectMapper;

    public String postAndGetTranscribe(
            File file, String token, String meetingId, String participants, String count
    ) throws JsonProcessingException {

        String transcribeIdResponse;
        String response = "";

        String fileName = file.getName();
        log.info("postAndGetTranscribe에 들어온 파일 이름: {}", fileName);

        try {
            // 서버에 파일 전송
            transcribeIdResponse = postTranscribeSample.postAudio(file, 2, token);
            log.info("transcribeIdResponse: {}", transcribeIdResponse);

            JsonNode rootNode = objectMapper.readTree(transcribeIdResponse);
            JsonNode idNode = rootNode.path("id");
            String transcribeId = idNode.asText();
            log.info("transcribeId: {}\n\n", transcribeId);

            int maxRetry = 10; // 최대 재시도 횟수
            int retryCount = 0;
            boolean success = false;

            while (retryCount < maxRetry && !success) {
                response = getTranscribeSample.getTranscribe(transcribeId, token);

                if (response.contains("not found") || response.contains("\"status\":\"transcribing\"")) {
                    log.info("STT get Transcribe response: {}", response);
                    retryCount++;
                    Thread.sleep(1000);  // 1초 대기 후 재시도
                } else {
                    success = true;
                }
            }

            // 응답이 유효하다면 수행.
            if (isValidJson(response)) {
                STTResponse sttResponse = objectMapper.readValue(response, STTResponse.class);
                Mono<String> responseMono = sttResponseService.sendTestQuestionToLLMAsync();

                responseMono.subscribe(response1 -> {
                    // 응답 처리 로직
                    log.info("Received response from LLM API: {}", response1);
                }, error -> {
                    // 에러 처리 로직
                    log.error("Error occurred while sending request to LLM API", error);
                });



                sttResponseService.saveSTTResponse(sttResponse, meetingId, Integer.parseInt(count));

                //여기에서 RabbitMQ 활용해 sse controller단에 토픽을 보내기(나중에)
                //log.info(sttResponseService.sendTestQuestionToLLMAsync().toString());

                log.info("아젠다 출력"+meetingService.getAgenda(meetingId).toString());


                //RabbitMQ를 통해 STTResponse 전송
                meetingSseService.sendSTTResponse(meetingId, sttResponse);



            } else {
                log.error("Unexpected response format: {}", response);
                throw new IllegalStateException("Invalid response format: " + response);
            }

        } catch (JsonProcessingException e) {
            log.error("JSON processing error in postAndGetTranscribe", e);
            throw e;
        } catch (Exception e) {
            log.error("Error in postAndGetTranscribe", e);
            throw new RuntimeException(e);
        }

        return response;
    }


    private boolean isValidJson(String response) {
        try {
            objectMapper.readTree(response);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
