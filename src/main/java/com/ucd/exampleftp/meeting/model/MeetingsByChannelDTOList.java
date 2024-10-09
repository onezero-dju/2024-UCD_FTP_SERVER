package com.ucd.exampleftp.meeting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@Setter
@ToString

public class MeetingsByChannelDTOList {

    Long Category_id;
    String Category_name;
    List<MeetingsByChannelDTO> meetingDTOList;

}
