package com.ucd.exampleftp.meeting.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)  // JSON 필드와 일치시키기 위해 추가
public class SummaryRequest {


    private Map<String, String> agendas;
    private String transcript;


    //*
    //
    //   "agendas": {
    //    "1": "RAG 파이프라인 구축 설계",
    //    "2": "의료 사각지대 대책",
    //    "3": "클라우드 모니터링 시스템 구현",
    //    "4": "다음 달 조직 운영 방안"
    //  },
    //  "transcript": "저희 의원실에서 조사를 해 보니까 비수급 빈곤층의 보건사회연구원에서는 추계치로 400만 명이라고 하는데 기초생활수급제도가 10년이나 됐는데 아직도 추계치를 내고 있는 이 상황이 참 마음이 아프고 한심하다는 생각이 듭니다. 그 추계치 400만 명의 인용에서 기초생활보장제도라는 10년 된 헌 집을 계속 고치고 고치고 고쳐서 쓰자 하는 방법인데 저는 헌 집 고치다가는 재정이 바닥나니까 차라리 설계를 다시 해서 새 집을 개축하자, 새로 짓자 이런 주장입니다. 그래서 보니까 비수급 빈곤층 통계를 내니까 저희 방에서 아무리 아무리 복지부에 부탁하고 여기저기에다 부탁해도 안 해 줘서 저희 보좌진들과 더불어 3개월 동안 통계를 냈습니다. 통계청 자료와 의료보험 자료와 기초수급제도 자료를 넣어서 통계를 돌려 보니까 617만 명이라는 사각지대가 나왔습니다. 이 사각지대 617만 명에 대한 대책을 세워야 된다고 생각을 합니다. 그 다음에 OECD에 대한 여러 가지 통계라든지 사회복지 통계라든지 여러 가지가 있겠지만 617만 명에 대한 사각지대가 존재하는 이것을 어떻게 풀 것인가, 그러면 통합급여로 갈 것인가, 개별급여로 주거, 의료, 생계급여를 따로따로 할 것인가에 대한 중요한 과제가 있습니다. 그리고 두 번째 과제로는 아동복지 예산 비율을 보시면…… 참 이 방송을 보시는 분들 마음이 어떨까 생각합니다. 아동의 숫자는 9688만 명이고 노인 숫자는 5537만 명인데 키로 보나 살아온 연륜으로 보나 필요한 욕구로 보나 참 차이가 많습니다."
    //}'
    //
    //
}
