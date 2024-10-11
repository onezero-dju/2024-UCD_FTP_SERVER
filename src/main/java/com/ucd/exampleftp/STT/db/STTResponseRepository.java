package com.ucd.exampleftp.STT.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface STTResponseRepository extends MongoRepository<STTResponse, String> {

    List<STTResponse> findAllByMeetingIdOrderByCountDesc(String meeting_id);
}
