package com.ucd.exampleftp.meeting.model;


import com.ucd.exampleftp.meeting.db.Participant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
public class MeetingDTO {

    // ObjectId를 문자열로 반환하기 위한 필드
    private String id;  // 여기에 ObjectId의 문자열 버전이 들어갈 예정

    @Field("meeting_title")
    private String meetingTitle;

    @Field("category_id")
    private String categoryId;

    @Field("created_at")
    private Date createdAt;

    @Field("edited_at")
    private Date editedAt;

    private List<Participant> participants;
    private List<String> agenda;
    private List<String> recordings;
}
