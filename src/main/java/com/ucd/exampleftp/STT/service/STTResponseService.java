package com.ucd.exampleftp.STT.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ucd.exampleftp.STT.db.STTResponse;
import com.ucd.exampleftp.STT.db.STTResponseRepository;
import com.ucd.exampleftp.meeting.model.SummaryRequest;
import io.netty.channel.ChannelOption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class STTResponseService {

    private final STTResponseRepository sttResponseRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Autowired
    public STTResponseService(WebClient.Builder webClientBuilder,
                              @Value("${llm.server.baseUri}") String LlmServerUri,
                              STTResponseRepository sttResponseRepository,
                              ObjectMapper objectMapper) {
        this.sttResponseRepository = sttResponseRepository;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(LlmServerUri)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 연결 타임아웃 설정 (10초)
                        .responseTimeout(Duration.ofSeconds(30)) // 응답 타임아웃 설정 (30초)
                ))
                .build();
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
     * @return API 응답 Mono<String>
     */
    public Mono<String> sendTextToLLMAsync(SummaryRequest summaryRequest) {

        try {
            String jsonString = objectMapper.writeValueAsString(summaryRequest);
            log.info("summaryRequest: " + jsonString);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 오류", e);
        }

        try {

            log.info("요약할 글: ", summaryRequest);

            return this.webClient.post()

                    .uri("/nlp/in-mtg")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(summaryRequest)
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
//    public Mono<String> sendTestQuestionToLLMAsync() {
//        String url = "http://34.47.100.212:80/nlp/in-mtg";
//        String question = "다음 글을 3 문장으로 요약해줘. 집중과 몰입을 강조할 때, 초보적인 독자가 가지기 쉬운 오류는 그것이 빠른 속도에서 얻어지는 것이라고 착각한다는 사실입니다. 결론적으로 말해 집중이나 몰입은 속도와는 무관한 ‘정신의 고조상태’를 말할 뿐입니다. 오히려 여러분이 분명히 아셔야 될 것은 좋은 섹스 전체에서 ‘느림과 여유의 미학’이 차지하는 비중이 무려 70~80%에 이른다는 점입니다. 스피노자는 이성(理性)이 절대 위치에 있는 철학 전통에서 인간을 이해하는 데 무엇보다 감성(感性)이 중요한 키워드임을 주지시켰던 혁명적인 철학자입니다. 그는 공포와 예속이 익숙한 시대에 항상 긍정과 자유의 철학을 이야기함으로써 살해 위협도 받고, 동료들로부터 따돌림당하기도 했습니다. 유대교 교리에 어긋나는 언행으로 유대 교회에 의해 파문당했던 그는 렌즈 가공일을 하다 생긴 폐질환으로 숨지고 맙니다. 스피노자는 언제 어떤 상황에 처해서도 항상 느리고 여유 있는 자세로 삶을 영위한 걸로 유명합니다. 그 저명한 대가가 평생 소장한 책은 100권이 채 되지 않았던 것입니다. 그런데 스피노자보다도 몇 배나 더 ‘느림의 미학’을 추구한 이가 있으니, ‘슬로 리딩(Slow reading)’의 창시자로 유명한 하시모토 다케시입니다. 나다고등학교 교장으로 재직하던 시절, 그는 학생들에게 놀이를 통해 배움에 대한 흥미와 즐거움을 주고자 슬로 리딩법을 고안해 냅니다. 고교 3년 동안 학생들은 선정된 오직 한 권의 책만 가지고서 다양하게 읽고 생각하고 쓰며 토론해 나갑니다. 그 황당하리라 여겨졌던 수업의 결과는 의외로 도쿄대와 교토대의 합격률 1위라는 찬란한 결과로 나타나 세상을 경악시키지요. 졸업생의 무려 58%가 도쿄대에 합격하고, 특히 낙타가 바늘구멍 들어가기보다 어렵다는 100명 정원의 도쿄대 의대 합격생을 한 해에만 16명을 배출하는 기염을 토합니다.";
//
//        try {
//            // 1. JSON 페이로드 생성 using ObjectMapper
//            ObjectNode jsonBody = objectMapper.createObjectNode();
//            jsonBody.put("question", question);
//
//            // 2. WebClient를 사용하여 비동기 POST 요청 전송
//            return webClient.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(jsonBody)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .doOnNext(response -> {
//                        // 성공 시 로깅
//                        log.info("Answer endpoint response: {}", response);
//                    })
//                    .doOnError(error -> {
//                        // 에러 시 로깅
//                        log.error("Error sending POST request to /answer endpoint", error);
//                    });
//
//        } catch (Exception e) {
//            log.error("Error constructing test question", e);
//            return Mono.error(new RuntimeException("Error constructing test question", e));
//        }
//    }
}
