package com.ucd.exampleftp.meeting.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.swing.text.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)  // Snake Case를 사용하여 user_id로 매핑
public class Participant {

    @Field("user_id")  // MongoDB에 저장할 때 필드명을 snake_case로 지정
    private Long userId;

    @Field("user_name")  // MongoDB에 저장할 때 필드명을 snake_case로 지정
    private String userName;

}

