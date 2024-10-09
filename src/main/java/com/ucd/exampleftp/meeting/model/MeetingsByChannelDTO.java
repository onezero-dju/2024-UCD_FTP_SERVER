package com.ucd.exampleftp.meeting.model;

import com.ucd.exampleftp.meeting.db.Participant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;
@Builder
@Getter
@Setter
@ToString

public class MeetingsByChannelDTO {

    private String meetingId;  // 여기에 ObjectId의 문자열 버전이 들어갈 예정

    @Field(value = "meeting_title")
    private String meetingTitle;

    @Field(value = "created_at")
    private LocalDate createdAt;

    @Field(value = "edited_at")
    private LocalDate editedAt;

    private List<Participant> participants;

    private List<String> agenda;

    private List<String> recordings;


}
