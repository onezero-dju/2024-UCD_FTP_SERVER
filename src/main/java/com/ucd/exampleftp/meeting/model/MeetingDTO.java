package com.ucd.exampleftp.meeting.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ucd.exampleftp.meeting.db.Participant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MeetingDTO {

    // ObjectId를 문자열로 반환하기 위한 필드
    private String id;  // 여기에 ObjectId의 문자열 버전이 들어갈 예정

    @Field("channel_id")
    private Long channelId;

    @Field("channel_name")
    private String channelName;

    @Field("category_id")
    private Long categoryId;

    @Field("category_name")
    private String categoryName;

    @Field(value = "meeting_title")
    private String meetingTitle;

    @Field(value = "created_at")
    private LocalDateTime createdAt;

    @Field(value = "edited_at")
    private LocalDateTime editedAt;

    private List<Participant> participants;

    private String agenda;

    @Field("recordings")
    private List<String> recordings;

}
