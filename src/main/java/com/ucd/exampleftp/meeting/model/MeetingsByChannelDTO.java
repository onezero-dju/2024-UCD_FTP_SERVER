package com.ucd.exampleftp.meeting.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ucd.exampleftp.meeting.db.Participant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Builder
@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MeetingsByChannelDTO {

    private String meetingId;  // 여기에 ObjectId의 문자열 버전이 들어갈 예정

    @Field(value = "meeting_title")
    private String meetingTitle;

    @Field(value = "created_at")
    private LocalDateTime createdAt;

    @Field(value = "edited_at")
    private LocalDateTime editedAt;
    private String agenda;

    private List<Participant> participants;


    private List<String> recordings;


}
