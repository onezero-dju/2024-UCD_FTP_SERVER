package com.ucd.exampleftp.STT.db;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor  // 기본 생성자 추가
@AllArgsConstructor // 모든 필드를 포함한 생성자 추가
@Getter
public class Results {

    private List<Utterance> utterances;

    private boolean verified;
}
