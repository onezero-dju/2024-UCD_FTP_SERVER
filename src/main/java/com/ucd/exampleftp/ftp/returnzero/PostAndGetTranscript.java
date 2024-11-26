package com.ucd.exampleftp.ftp.returnzero;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucd.exampleftp.STT.db.STTResponse;
import com.ucd.exampleftp.STT.db.Utterance;
import com.ucd.exampleftp.STT.service.STTResponseService;
import com.ucd.exampleftp.ftp.MongoService.MongoService;
import com.ucd.exampleftp.meeting.model.LlmResponse;
import com.ucd.exampleftp.meeting.model.SummaryRequest;
import com.ucd.exampleftp.meeting.service.MeetingConverter;
import com.ucd.exampleftp.meeting.service.MeetingService;
import com.ucd.exampleftp.meeting.service.MeetingSseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

@Component
@Slf4j
public class PostAndGetTranscript {

    @Autowired
    private GetTranscribeSample getTranscribeSample;

    @Autowired
    private PostTranscribeSample postTranscribeSample;

    @Autowired
    private MeetingSseService meetingSseService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MongoService mongoService;

    @Autowired
    private STTResponseService sttResponseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeetingConverter meetingConverter;


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


                StringBuilder allMessagesBuilder = new StringBuilder();

                List<Utterance> utterances = sttResponse.getResults().getUtterances();
                for (Utterance utterance : utterances) {
                    allMessagesBuilder.append(utterance.getMsg()).append(" ");
                }
                sttResponseService.saveSTTResponse(sttResponse, meetingId, Integer.parseInt(count));


                String summary=allMessagesBuilder.toString().trim();


                // ObjectMapper 인스턴스가 없다면 생성 또는 주입
                ObjectMapper objectMapper = new ObjectMapper();

                SummaryRequest summaryRequest = new SummaryRequest();
                summaryRequest.setAgendas(meetingService.getAgenda(meetingId));
                summaryRequest.setTranscript(summary);






                Mono<LlmResponse> llmResponseMono = sttResponseService.sendTextToLLMAsync(summaryRequest)
                        .map(this.meetingConverter::convertStringToLlmResponse);




                llmResponseMono.subscribe(response1 -> {
                    // 응답 처리 로직
                    log.info("Received response from LLM API: {}", response1);
                    meetingSseService.streamLLMResponse(meetingId, response1);
                    mongoService.saveLlmResponse(meetingId, response1);
                }, error -> {
                    // 에러 처리 로직
                    log.error("Error occurred while sending request to LLM API", error);
                });








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
