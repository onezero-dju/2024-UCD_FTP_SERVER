package com.ucd.exampleftp.meeting.model;


import com.ucd.exampleftp.meeting.db.Participant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
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
    private LocalDate createdAt;

    @Field(value = "edited_at")
    private LocalDate editedAt;

    private List<Participant> participants;

    private List<String> agenda;

    @Field("recordings")
    private List<String> recordings;

}
