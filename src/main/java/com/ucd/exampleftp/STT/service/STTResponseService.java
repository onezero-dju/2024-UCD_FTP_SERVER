package com.ucd.exampleftp.STT.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ucd.exampleftp.STT.db.STTResponse;
import com.ucd.exampleftp.STT.db.STTResponseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class STTResponseService {

    @Autowired
    private STTResponseRepository sttResponseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    final private WebClient webClient;

    @Autowired
    public STTResponseService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * STTResponse 객체를 MongoDB에 저장하는 메서드
     *
     * @param sttResponse STTResponse 객체
     * @param meetingId   미팅 ID
     * @param count       카운트
     * @return 저장된 STTResponse 객체
     */
    @Transactional
    public STTResponse saveSTTResponse(STTResponse sttResponse, String meetingId, int count) {
        try {
            sttResponse.setMeetingId(meetingId);
            sttResponse.setCount(count);
            // MongoDB에 저장
            return sttResponseRepository.save(sttResponse);
        } catch (Exception e) {
            log.error("Failed to map and save STTResponse", e);
            throw new RuntimeException("Failed to map and save STTResponse", e);
        }
    }

    /**
     * 특정 미팅 ID에 해당하는 STTResponse 객체들을 가져오는 메서드
     *
     * @param meeting_id 미팅 ID
     * @return STTResponse 객체 리스트
     */
    public List<STTResponse> getSTTResponse(String meeting_id) {
        return sttResponseRepository.findAllByMeetingIdOrderByCountDesc(meeting_id);
    }

    /**
     * STTResponse 객체를 받아 LLM API로 질문을 전송하는 비동기 메서드
     *
     * @param sttResponse STTResponse 객체
     * @return API 응답 Mono<String>
     */
    public Mono<String> sendTextToLLMAsync(STTResponse sttResponse) {
        if (sttResponse == null) {
            log.error("STTResponse is null");
            return Mono.error(new IllegalArgumentException("STTResponse cannot be null"));
        }
        if (sttResponse.getResults() == null) {
            log.error("STTResponse.results is null");
            return Mono.error(new IllegalStateException("STTResponse.results cannot be null"));
        }

        try {
            // 1. STTResponse에서 msg 필드 추출 및 결합
            String question = sttResponse.getResults().getUtterances()
                    .stream()
                    .map(utterance -> utterance.getMsg())
                    .collect(Collectors.joining(" ")); // 공백으로 결합

            log.info("Constructed question: {}", question);

            // 2. JSON 페이로드 생성 using ObjectMapper
            ObjectNode jsonBody = objectMapper.createObjectNode();
            jsonBody.put("question", question);

            // 3. WebClient를 사용하여 비동기 POST 요청 전송
            return webClient.post()
                    .uri("/answer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> {
                        // 성공 시 로깅
                        log.info("Answer endpoint response: {}", response);
                    })
                    .doOnError(error -> {
                        // 에러 시 로깅
                        log.error("Error sending POST request to /answer endpoint", error);
                    });

        } catch (Exception e) {
            log.error("Error constructing question from STTResponse", e);
            return Mono.error(new RuntimeException("Error constructing question from STTResponse", e));
        }
    }

    /**
     * 테스트를 위한 고정된 질문을 /answer 엔드포인트로 전송하는 비동기 메서드
     *
     * @return API 응답 Mono<String>
     */
    public Mono<String> sendTestQuestionToLLMAsync() {
        String url = "/answer";
        String question = "미분을 짧게 설명해";

        try {
            // 1. JSON 페이로드 생성 using ObjectMapper
            ObjectNode jsonBody = objectMapper.createObjectNode();
            jsonBody.put("question", question);

            // 2. WebClient를 사용하여 비동기 POST 요청 전송
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> {
                        // 성공 시 로깅
                        log.info("Answer endpoint response: {}", response);
                    })
                    .doOnError(error -> {
                        // 에러 시 로깅
                        log.error("Error sending POST request to /answer endpoint", error);
                    });

        } catch (Exception e) {
            log.error("Error constructing test question", e);
            return Mono.error(new RuntimeException("Error constructing test question", e));
        }
    }
}
