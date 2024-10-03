package com.ucd.exampleftp.meeting.service;


import com.ucd.exampleftp.meeting.db.Meeting;
import com.ucd.exampleftp.meeting.db.MeetingRepository;
import com.ucd.exampleftp.meeting.model.MeetingCreateRequest;
import com.ucd.exampleftp.meeting.model.MeetingDTO;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingConverter meetingConverter;

    public MeetingService(MeetingRepository meetingRepository, MeetingConverter meetingConverter) {
        this.meetingRepository = meetingRepository;
        this.meetingConverter = meetingConverter;
    }


    // 미팅 저장
    public MeetingDTO saveMeeting(MeetingCreateRequest meetingCreateRequest) {

        Meeting meeting = Meeting.builder()
                .meetingTitle(meetingCreateRequest.getMeetingTitle())
                .categoryName(meetingCreateRequest.getCategoryName())
                .categoryId(meetingCreateRequest.getCategoryId())
                .channelName(meetingCreateRequest.getChannelName())
                .channelId(meetingCreateRequest.getChannelId())
                .recordings(meetingCreateRequest.getRecordings())
                .agenda(meetingCreateRequest.getAgenda())
                .participants(meetingCreateRequest.getParticipants())
                .createdAt(LocalDate.now())
                .editedAt(LocalDate.now())
                .build();

        meetingRepository.save(meeting);  // MongoDB에 저장

        return meetingConverter.meetingConverterToDTO(meeting);
    }


    public MeetingDTO viewMeeting(String meeting_id) {

        ObjectId objectId = new ObjectId(meeting_id);
        Optional<Meeting> meeting = meetingRepository.findById(objectId);

        if (meeting.isPresent()) {
            log.info("meeting id:" + meeting.get());
            Meeting meetingEntity = meeting.get();
            return meetingConverter.meetingConverterToDTO(meetingEntity);
        } else {
            return null;
        }
    }

    public List<MeetingDTO> viewByCategoryId(String category_id) {
        List<Meeting> meetingList = meetingRepository.findAllByCategoryIdOrderByEditedAt(category_id);

        List<MeetingDTO> meetingDTOList = meetingConverter.meetingListConverterToDTOList(meetingList);

        return meetingDTOList;
    }

    ///아젠다가 있는지 확인하는 서비스. ftp서버로 음성데이터를 받을 때 사용함.
    public boolean isAgendaExist(String meeting_id) {


        ObjectId objectId = new ObjectId(meeting_id);
        Optional<Meeting> meeting = meetingRepository.findById(objectId);

        if (meeting.get().getAgenda().isEmpty()||meeting.get().getAgenda()==null) {

            return false;

        } else {
            return true;
        }
    }



}
