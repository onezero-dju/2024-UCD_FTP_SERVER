package com.ucd.exampleftp.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucd.exampleftp.STT.db.STTResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MeetingSseService {
    // 연결된 SseEmitter 목록 저장 (회의 ID 기준)
    private static final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    // 클라이언트를 목록에 추가
    public void addEmitter(String meetingId, SseEmitter emitter){
        emitters.computeIfAbsent(meetingId, key -> Collections.synchronizedList(new ArrayList<>())).add(emitter);

        // 연결 종료 시 emitter 제거
        emitter.onCompletion(() -> removeEmitter(meetingId, emitter));
        emitter.onTimeout(() -> removeEmitter(meetingId, emitter));
        emitter.onError((e) -> removeEmitter(meetingId, emitter));
    }

    // 클라이언트를 목록에서 제거
    private void removeEmitter(String meetingId, SseEmitter emitter) {
        List<SseEmitter> emitterList = emitters.get(meetingId);
        if (emitterList != null) {
            emitterList.remove(emitter);
        }
    }

    // STTResponse 객체를 클라이언트에게 전송
    public void sendSTTResponse(String meetingId, STTResponse sttResponse){
        // JSON 변환
        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(sttResponse);
        } catch (JsonProcessingException e){
            throw new RuntimeException("JSON 변환 실패", e);
        }

        // 연결된 모든 클라이언트에게 전송
        List<SseEmitter> emitterList = emitters.getOrDefault(meetingId, Collections.emptyList());
        for (SseEmitter emitter : new ArrayList<>(emitterList)) { // ConcurrentModificationException 방지를 위해 복사본 사용
            try {
                emitter.send(SseEmitter.event().name("sttResponse").data(jsonData));
            } catch (IOException e){
                emitterList.remove(emitter);
            }
        }
    }
}
