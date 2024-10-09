package com.ucd.exampleftp.meeting.service;

import com.ucd.exampleftp.meeting.db.Meeting;
import com.ucd.exampleftp.meeting.model.MeetingDTO;
import com.ucd.exampleftp.meeting.model.MeetingsByChannelDTO;
import com.ucd.exampleftp.meeting.model.MeetingsByChannelDTOList;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class MeetingConverter {

    public MeetingDTO meetingConverterToDTO(Meeting meeting){

        MeetingDTO meetingDTO = MeetingDTO.builder()
                .id(meeting.getId().toString())
                .meetingTitle(meeting.getMeetingTitle())
                .channelId(meeting.getChannelId())
                .channelName(meeting.getChannelName())
                .categoryId(meeting.getCategoryId())
                .categoryName(meeting.getCategoryName())
                .createdAt(meeting.getCreatedAt())
                .editedAt(meeting.getEditedAt())
                .agenda(meeting.getAgenda())
                .participants(meeting.getParticipants())
                .build();

        return meetingDTO;
    }

    public List<MeetingDTO> meetingListConverterToDTOList(List<Meeting> meetings){

        return meetings.stream().map(this::meetingConverterToDTO).collect(Collectors.toList());


    }

    // Meeting -> MeetingsByChannelDTO로 변환
    private MeetingsByChannelDTO meetingToMeetingsByChannelDTO(Meeting meeting) {
        return MeetingsByChannelDTO.builder()
                .meetingId(meeting.getId().toString())  // ObjectId를 문자열로 변환
                .meetingTitle(meeting.getMeetingTitle())
                .createdAt(meeting.getCreatedAt())
                .editedAt(meeting.getEditedAt())
                .participants(meeting.getParticipants())
                .agenda(meeting.getAgenda())
                .recordings(meeting.getRecordings())
                .build();
    }

    // List<Meeting>을 MeetingsByChannelDTOList로 변환
    public List<MeetingsByChannelDTOList> convertByChannelAndSorting(List<Meeting> meetings) {
        // 먼저 categoryId를 기준으로 그룹화한 다음 그룹별로 MeetingsByChannelDTOList를 생성
        Map<Long, List<Meeting>> groupedByCategory = meetings.stream()
                .collect(Collectors.groupingBy(Meeting::getCategoryId));

        // 그룹화된 결과를 MeetingsByChannelDTOList로 변환
        return groupedByCategory.entrySet().stream()
                .map(entry -> MeetingsByChannelDTOList.builder()
                        .Category_id(entry.getKey())
                        .Category_name(entry.getValue().get(0).getCategoryName())  // 첫 번째 회의의 카테고리 이름 사용
                        .meetingDTOList(entry.getValue().stream()
                                .sorted(Comparator.comparing(Meeting::getEditedAt))  // editedAt 기준 정렬
                                .map(this::meetingToMeetingsByChannelDTO)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }


}
