package com.ucd.exampleftp.meeting.service;

import com.ucd.exampleftp.meeting.db.Meeting;
import com.ucd.exampleftp.meeting.model.MeetingDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
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



}
