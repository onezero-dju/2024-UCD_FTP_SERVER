package com.ucd.exampleftp.meeting.db;

import com.ucd.exampleftp.meeting.model.LlmResponse;
import jakarta.persistence.PrePersist;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "llm_summaries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LlmSummary {

    @Id
    private String id; // MongoDB의 고유 ID

    // LlmResponse의 필드들
    private Map<String, String> mentionedAgendas;
    private String blockSummary;

    // 추가적인 메타데이터
    private LocalDateTime createdAt;
    private String meetingId; // 관련 미팅 ID 연결

    // 생성 시간을 자동으로 설정하는 생성자
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // LlmResponse를 LlmSummary로 변환하는 정적 팩토리 메서드
    public static LlmSummary fromLlmResponse(LlmResponse response, String meetingId, String channelId) {
        return LlmSummary.builder()
                .mentionedAgendas(response.getMentioned_agendas())
                .blockSummary(response.getBlock_summary())
                .meetingId(meetingId)
                .build();
    }
}