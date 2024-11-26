package com.ucd.exampleftp.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucd.exampleftp.STT.db.STTResponse;
import com.ucd.exampleftp.meeting.model.LlmResponse;
import com.ucd.exampleftp.meeting.model.SummaryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MeetingSseService {
    private static final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public MeetingSseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void addEmitter(String meetingId, SseEmitter emitter) {
        emitters.computeIfAbsent(meetingId, key -> Collections.synchronizedList(new ArrayList<>())).add(emitter);
        log.info("Emitter added for meetingId {}: {}", meetingId, emitter);

        emitter.onCompletion(() -> removeEmitter(meetingId, emitter));
        emitter.onTimeout(() -> removeEmitter(meetingId, emitter));
        emitter.onError((e) -> removeEmitter(meetingId, emitter));
    }

    private void removeEmitter(String meetingId, SseEmitter emitter) {
        List<SseEmitter> emitterList = emitters.get(meetingId);
        if (emitterList != null) {
            emitterList.remove(emitter);
            log.info("Emitter removed for meetingId {}", meetingId);
        }
    }


    public void streamLLMResponse(String meetingId, LlmResponse llmResponseMono) {

        try {
            String SseData = objectMapper.writeValueAsString(llmResponseMono);

            sendEventToEmitters(meetingId, "llmResponse", SseData);
        } catch (JsonProcessingException e) {
            log.error("LLM 응답 JSON 변환 실패", e);
            sendErrorEvent(meetingId, "LLM 응답 처리 중 오류가 발생했습니다.");
        }
    }

    private void sendEventToEmitters(String meetingId, String eventName, String jsonData) {
        List<SseEmitter> emitterList = emitters.getOrDefault(meetingId, Collections.emptyList());
        log.info("Sending {} to {} emitters for meetingId {}", eventName, emitterList.size(), meetingId);

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitterList) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(jsonData));
                log.debug("{} event sent successfully", eventName);
            } catch (IOException e) {
                log.warn("Failed to send {} event, removing emitter", eventName, e);
                deadEmitters.add(emitter);
            }
        }

        // 실패한 이미터들 제거
        deadEmitters.forEach(emitter -> removeEmitter(meetingId, emitter));
    }

    private void sendErrorEvent(String meetingId, String errorMessage) {
        try {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", errorMessage);
            errorResponse.put("timestamp", System.currentTimeMillis());

            String jsonError = objectMapper.writeValueAsString(errorResponse);
            sendEventToEmitters(meetingId, "llmError", jsonError);
        } catch (JsonProcessingException e) {
            log.error("Error JSON 변환 실패", e);
        }
    }
}