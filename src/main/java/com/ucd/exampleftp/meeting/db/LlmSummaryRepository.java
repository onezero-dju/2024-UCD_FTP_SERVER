package com.ucd.exampleftp.meeting.db;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LlmSummaryRepository extends ReactiveMongoRepository<LlmSummary, String> {
    // 추가적인 커스텀 쿼리 메서드 필요시 여기에 작성
    Mono<LlmSummary> findByMeetingId(String meetingId);
}
