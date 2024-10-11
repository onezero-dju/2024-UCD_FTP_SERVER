package com.ucd.exampleftp.meeting.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.ucd.exampleftp.meeting.db.Participant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Field("channel_id")
    @NotNull(message = "채널 아이디를 미입력")
    private Long channelId;

    @Field("channel_name")
    @NotBlank(message = "채널이름은 null 이거나 공백일 수 없음")
    private String channelName;

    @Field("category_id")
    private Long categoryId;

    @Field("category_id")
    private String categoryName;


    @Field(value = "meeting_title")
    private String meetingTitle;

    @Size(min = 1, message = "참가자는 최소 1명 이상이어야 합니다.")
    private List<Participant> participants;

    private String agenda;

    @Field("recordings")
    private List<String> recordings;
}
