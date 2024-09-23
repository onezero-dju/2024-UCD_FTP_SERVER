package com.ucd.exampleftp.STT.db;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
public class Utterance {

    @Field("start_at") // MongoDB 필드명과 매핑
    private int startAt;

    private int duration;

    private int spk;

    @Field("spk_type")
    private String spkType;

    private String msg;
}
