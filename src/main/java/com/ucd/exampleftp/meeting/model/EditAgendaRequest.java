package com.ucd.exampleftp.meeting.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)  // JSON 필드와 일치시키기 위해 추가
public class EditAgendaRequest {

    String agenda;

}