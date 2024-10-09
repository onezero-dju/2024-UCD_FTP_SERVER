package com.ucd.exampleftp.meeting.db;


import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends MongoRepository<Meeting,ObjectId> {

    List<Meeting> findAllByCategoryIdOrderByEditedAt(Long id);

    List<Meeting> findAllByChannelIdOrderByCategoryIdAscEditedAtAsc(Long channelId);


    // agenda가 null이 아니고 비어 있지 않은 경우 존재 여부 확인

}

