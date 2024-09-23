package com.ucd.exampleftp.STT.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface STTResponseRepository extends MongoRepository<STTResponse, String> {
}
