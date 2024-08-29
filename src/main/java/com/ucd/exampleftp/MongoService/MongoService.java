package com.ucd.exampleftp.MongoService;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class MongoService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public String getTokenFromDB(String tokenType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is(tokenType));

        Document document = mongoTemplate.findOne(query, Document.class, "tokens");
        if (document != null) {
            return document.getString("token");
        }
        return null;
    }


}
