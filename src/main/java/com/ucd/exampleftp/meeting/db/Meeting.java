package com.ucd.exampleftp.meeting.db;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jdk.jfr.Category;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@Document(collection = "Meeting")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Meeting {

    @Id
    private ObjectId id;  // MongoDB에서 자동 생성되는 ID

    @Field("category_id")
    private String categoryId;

    @Field(value = "meeting_title")
    private String meetingTitle;

    @Field(value = "created_at")
    private Date createdAt;

    @Field(value = "edited_at")
    private Date editedAt;

    private List<Participant> participants;

    private List<String> agenda;

    @Field("recordings")
    private List<String> recordings;



}


