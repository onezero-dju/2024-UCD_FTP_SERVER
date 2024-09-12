package com.ucd.exampleftp.ftp.MongoService;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class MongoService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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



    public String setRZtoken(String rawToken){

        String[] editedToken = rawToken.split("\\{\"access_token\":\"|\",\"expire_at\"");



        Query query = new Query();
        query.addCriteria(Criteria.where("type").is("returnzero_token")); // "type" 필드를 통해 특정 토큰 문서를 식별

        String token=editedToken[1];//1번째 배열에는 토큰이 저장됨
        String expire_at=editedToken[2];//2번째 배열에는 토큰이 expire 되는 시간이 나옴


        Update update=new Update();

        update.set("token",token);
        update.set("type","returnzero_token");
        update.set("expire_at",expire_at);
        update.set("updated_time",System.currentTimeMillis());

        mongoTemplate.upsert(query,update,"tokens");

        return editedToken[1];

    }






}
