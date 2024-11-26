package com.ucd.exampleftp.STT.db;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor  // 기본 생성자 추가
@AllArgsConstructor // 모든 필드를 포함한 생성자 추가
@Getter
public class Utterance {

    @Field("start_at") // MongoDB 필드명과 매핑
    private int startAt;

    private int duration;

    private int spk;

    @Field("spk_type")
    private String spkType;

    private String msg;
}
