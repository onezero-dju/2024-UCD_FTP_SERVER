package com.ucd.exampleftp.meeting.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ucd.exampleftp.meeting.db.Participant;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;




@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)  // JSON 필드와 일치시키기 위해 추가
public class MeetingCreateRequest {

    @Field("category_id")
    private String categoryId;

    @Field(value = "meeting_title")
    private String meetingTitle;

    @Field(value = "participants")
    private List<Participant> participants;

    private List<String> agenda;

    @Field("recordings")
    private List<String> recordings;
}