package com.ucd.exampleftp.ftp.MongoService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucd.exampleftp.meeting.model.LlmResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@Slf4j
public class MongoService {

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public MongoService(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }


    ///리턴제로에 ${returnzero.client.id}를 통해 계정 인증을 하고
    public String getRZtoken(String tokenType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is(tokenType));

        Document document = mongoTemplate.findOne(query, Document.class, "tokens");
        if (document != null) {
            return document.getString("token");
        }
        return null;
    }


    public String setRZtoken(String rawToken) {

        String[] editedToken = rawToken.split("\\{\"access_token\":\"|\",\"expire_at\"");


        Query query = new Query();
        query.addCriteria(Criteria.where("type").is("returnzero_token")); // "type" 필드를 통해 특정 토큰 문서를 식별

        String token = editedToken[1];//1번째 배열에는 토큰이 저장됨
        String expire_at = editedToken[2];//2번째 배열에는 토큰이 expire 되는 시간이 나옴


        Update update = new Update();

        update.set("token", token);
        update.set("type", "returnzero_token");
        update.set("expire_at", expire_at);
        update.set("updated_time", System.currentTimeMillis());

        mongoTemplate.upsert(query, update, "tokens");

        return editedToken[1];

    }


    public void saveLlmResponse(String meetingId, LlmResponse llmResponse) {
        // Document로 변환
        Document document = new Document();
        document.put("meetingId", meetingId);
        document.put("mentionedAgendas", llmResponse.getMentioned_agendas());
        document.put("blockSummary", llmResponse.getBlock_summary());
        document.put("createdAt", new Date());

        // MongoDB에 저장
        mongoTemplate.save(document, "llm_summaries");

        log.info("LLM Response saved for meeting: {}", meetingId + "llm요약 몽고디비 저장본: " + document.toJson());
    }
}







