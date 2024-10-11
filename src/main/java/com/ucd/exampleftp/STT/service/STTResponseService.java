package com.ucd.exampleftp.STT.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucd.exampleftp.STT.db.STTResponse;
import com.ucd.exampleftp.STT.db.STTResponseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class STTResponseService {

    @Autowired
    private STTResponseRepository sttResponseRepository;
    @Autowired
    private ObjectMapper objectMapper;

    final private MongoTemplate mongoTemplate;

    public STTResponseService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public STTResponse saveSTTResponse(String jsonResponse, String meetingId, int count) {
        try {


            // JSON 문자열을 STTResponse 객체로 매핑
            STTResponse sttResponse = objectMapper.readValue(jsonResponse, STTResponse.class);
            sttResponse.setMeetingId(meetingId);
            sttResponse.setCount(count);
            // MongoDB에 저장
            return sttResponseRepository.save(sttResponse);
        } catch (Exception e) {
            log.error("Failed to map and save STTResponse", e);
            throw new RuntimeException("Failed to map and save STTResponse", e);
        }
    }

    public List<STTResponse> getSTTResponse(
            String meeting_id
    ){
        return sttResponseRepository.findAllByMeetingIdOrderByCountDesc(meeting_id);


    }



}

