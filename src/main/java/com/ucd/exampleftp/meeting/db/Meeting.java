    package com.ucd.exampleftp.meeting.db;

    import com.fasterxml.jackson.databind.PropertyNamingStrategies;
    import com.fasterxml.jackson.databind.annotation.JsonNaming;
    import lombok.*;
    import org.bson.types.ObjectId;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.mapping.Document;
    import org.springframework.data.mongodb.core.mapping.Field;
    import jakarta.validation.constraints.NotBlank;

    import java.sql.Timestamp;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;



    @Document(collection = "Meeting")
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public class Meeting {

        @Id
        private ObjectId id;  // MongoDB에서 자동 생성되는 ID

        @Field("channel_id")
        private Long channelId;

        @Field("channel_name")
        private String channelName;

        @Field("category_id")
        private Long categoryId;

        @Field("category_name")
        private String categoryName;

        @Field(value = "meeting_title")
        @NotBlank
        private String meetingTitle;

        @Field(value = "created_at")
        private LocalDateTime createdAt;

        @Field(value = "edited_at")
        private LocalDateTime editedAt;

        @Builder.Default
        private List<Participant> participants = new ArrayList<>();  // 빈 리스트로 기본값 설정

        @Builder.Default
        private List<String> agenda = new ArrayList<>();  // 빈 리스트로 기본값 설정

        @Field("recordings")
        @Builder.Default
        private List<String> recordings = new ArrayList<>();  // 빈 리스트로 기본값 설정

        // 기본 생성자 필요 없음 (Lombok이 자동 생성)
    }
