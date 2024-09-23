package com.ucd.exampleftp.meeting.Controller;

import com.ucd.exampleftp.meeting.db.Meeting;
import com.ucd.exampleftp.meeting.model.MeetingCreateRequest;
import com.ucd.exampleftp.meeting.model.MeetingDTO;
import com.ucd.exampleftp.meeting.model.Test;
import com.ucd.exampleftp.meeting.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping(value = "/api/meeting")
public class MeetingController {


    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping(value = "create")
    public String createMeeting(

            @RequestBody
            MeetingCreateRequest meetingCreateRequest
    ){

        Meeting meeting = Meeting.builder()
                .meetingTitle(meetingCreateRequest.getMeetingTitle())
                .categoryId(meetingCreateRequest.getCategoryId())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .editedAt(Timestamp.valueOf(LocalDateTime.now()))
                .agenda(meetingCreateRequest.getAgenda())
                .participants(meetingCreateRequest.getParticipants())
                .recordings(meetingCreateRequest.getRecordings())
                .build();


        return meetingService.saveMeeting(meeting);

    }


    @GetMapping(value = "/view/{id}")
    public MeetingDTO viewMeeting(
            @PathVariable("id")
            String id
    ){
        return meetingService.viewMeeting(id);

    }

    @GetMapping(value = "/check_agenda/{meeting_id}")
    public boolean viewAllMeeting(
            @PathVariable("meeting_id")
            String meeting_id
    ){

        log.info("is agendaexist?"+meetingService.isAgendaExist(meeting_id));

        return meetingService.isAgendaExist(meeting_id);

    }

    @PostMapping("/test")
    public String test(
            @RequestBody
            Test test_context
    ){

        return test_context.toString();

    }


}
