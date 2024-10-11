package com.ucd.exampleftp.meeting.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MeetingsByChannelDTOList {

    Long CategoryId;
    String CategoryName;
    List<MeetingsByChannelDTO> meetingDTOList;

}
