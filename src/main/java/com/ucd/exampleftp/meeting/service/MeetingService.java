package com.ucd.exampleftp.meeting.service;


import com.mongodb.client.result.UpdateResult;
import com.ucd.exampleftp.meeting.db.Meeting;
import com.ucd.exampleftp.meeting.db.MeetingRepository;
import com.ucd.exampleftp.meeting.db.Participant;
import com.ucd.exampleftp.meeting.model.*;
import com.ucd.exampleftp.util.config.jwt.CustomUserDetails;
import com.ucd.exampleftp.util.config.jwt.CustomUserDetailsController;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import javax.swing.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingConverter meetingConverter;

    private final MongoTemplate mongoTemplate;

    private final CustomUserDetailsController customUserDetailsController;

    public MeetingService(MeetingRepository meetingRepository, MeetingConverter meetingConverter, MongoTemplate mongoTemplate, CustomUserDetailsController customUserDetailsController) {
        this.meetingRepository = meetingRepository;
        this.meetingConverter = meetingConverter;
        this.mongoTemplate = mongoTemplate;
        this.customUserDetailsController = customUserDetailsController;
    }



    // 미팅 저장
    public MeetingDTO createMeeting(MeetingCreateRequest meetingCreateRequest) {


        List<Participant> participants = new ArrayList<>();
        Participant participant= Participant.builder()
                .userId(customUserDetailsController.getCurrentUserId())
                .userName(customUserDetailsController.getCurrentUserName())
                .build();

        participants.add(
                participant
        );


        Meeting meeting = Meeting.builder()
                .meetingTitle(meetingCreateRequest.getMeetingTitle())
                .categoryName(meetingCreateRequest.getCategoryName())
                .categoryId(meetingCreateRequest.getCategoryId())
                .channelName(meetingCreateRequest.getChannelName())
                .channelId(meetingCreateRequest.getChannelId())
                .recordings(meetingCreateRequest.getRecordings())
                .agenda(meetingCreateRequest.getAgenda())
                .participants(participants)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .editedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
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
            throw new IllegalStateException("열람할 미팅이 없습니다.");
        }
    }

    public List<MeetingDTO> viewByCategoryId(String category_id) {
        List<Meeting> meetingList = meetingRepository.findAllByCategoryIdOrderByEditedAt(Long.valueOf(category_id));

        List<MeetingDTO> meetingDTOList = meetingConverter.meetingListConverterToDTOList(meetingList);

        return meetingDTOList;
    }




    public List<MeetingsByChannelDTOList> viewByChannelId(String channel_id) {

        List<Meeting> meetingList = meetingRepository.findAllByChannelIdOrderByCategoryIdAscEditedAtAsc(Long.valueOf(channel_id));

         return meetingConverter.convertByChannelAndSorting(meetingList);

    }




    ///아젠다가 있는지 확인하는 서비스. ftp서버로 음성데이터를 받을 때 사용함.
    public boolean isAgendaExist(String meeting_id) {


        ObjectId objectId = new ObjectId(meeting_id);

        try{
            Optional<Meeting> meeting = meetingRepository.findById(objectId);
            if (meeting.get().getAgenda().isEmpty()||meeting.get().getAgenda()==null) {

                return false;

            } else {
                return true;
            }

        }
        catch (Exception e){
            return false;
        }

    }



    public boolean addAgenda(String meetingId, AddAgendaRequest newAgendaItem) {

        UpdateResult result = null;
        
        // ObjectId로 변환하여 조회
        Query query = new Query(Criteria.where("_id").is(new ObjectId(meetingId)));

        for(String agenda : newAgendaItem.getAgenda()){


            // agenda 필드에 항목 추가
            Update update = new Update().push("agenda", agenda);

            // 업데이트 실행
            result = mongoTemplate.updateFirst(query, update, Meeting.class);

            

        }
        // 수정된 문서가 있으면 true 반환
        return result.getModifiedCount() > 0;


    }

    public boolean editAgenda(String meetingId, EditAgendaRequest editAgendaRequest) {
        // Meeting ID로 Meeting 문서를 찾기 위한 쿼리 생성
        Query query = new Query(Criteria.where("id").is(meetingId));

        // Update 객체를 생성하여 agenda 필드 설정
        Update update = new Update().set("agenda", editAgendaRequest.getAgenda());

        // updateFirst 메서드를 사용하여 첫 번째로 일치하는 문서에 업데이트 적용
        UpdateResult result = mongoTemplate.updateFirst(query, update, Meeting.class);

        // 업데이트된 문서 수가 1 이상이면 성공
        return result.getModifiedCount() > 0;
    }


    public boolean deleteMeetings(
            String meeting_id
    ){

        try {

            Query query = new Query(Criteria.where("id").is(meeting_id));
            mongoTemplate.remove(query, Meeting.class);

        }catch (Exception e){

        }

        return false;
    }

    public boolean addParticipant(String meeting_id){


        Query query = new Query(
                Criteria.where("id").is(meeting_id)
                        .and("participants.user_id").ne(customUserDetailsController.getCurrentUserId())
        );

        Participant participant= Participant.builder()
                .userName(customUserDetailsController.getCurrentUserName())
                .userId(customUserDetailsController.getCurrentUserId())
                .build();

        // Update 객체를 생성하여 participants 리스트에 새로운 Participant 추가
        Update update = new Update().push("participants", participant);

        // updateFirst 메서드를 사용하여 조건에 맞는 첫 번째 문서에 업데이트 적용
        UpdateResult result = mongoTemplate.updateFirst(query, update, Meeting.class);

        // 업데이트된 문서 수가 1 이상이면 성공
        return result.getModifiedCount() > 0;
    }



}
