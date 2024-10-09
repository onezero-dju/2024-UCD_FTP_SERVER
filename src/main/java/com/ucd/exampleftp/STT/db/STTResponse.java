package com.ucd.exampleftp.STT.db;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@Getter
@Setter
@Document(collection = "stt_responses") // MongoDB 컬렉션 이름 지정
public class STTResponse {

    @Id
    private String id; // MongoDB의 _id 필드와 매핑됩니다.

    private String status;

    private Results results;

    private int count;

    @Field(value = "meeting_id")
    private String meetingId;
}
