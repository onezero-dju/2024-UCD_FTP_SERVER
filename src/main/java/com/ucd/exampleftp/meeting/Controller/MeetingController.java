package com.ucd.exampleftp.meeting.Controller;

import com.ucd.exampleftp.meeting.model.AddAgendaRequest;
import com.ucd.exampleftp.meeting.model.MeetingCreateRequest;
import com.ucd.exampleftp.meeting.model.MeetingDTO;
import com.ucd.exampleftp.meeting.model.MeetingsByChannelDTOList;
import com.ucd.exampleftp.meeting.service.MeetingService;
import com.ucd.exampleftp.util.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/meetings")
public class MeetingController extends GlobalExceptionHandler {


    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping(value = "create")
    public ResponseEntity<MeetingDTO> createMeeting(
            @Valid
            @RequestBody MeetingCreateRequest meetingCreateRequest
    ) {
        // 미팅을 저장하고 생성된 미팅 ID를 반환
        MeetingDTO meetingDTO = meetingService.saveMeeting(meetingCreateRequest);

        // 생성된 리소스의 URI를 생성
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // 현재 요청 URI 가져오기
                .path("/{id}") // 생성된 리소스의 ID를 경로에 추가
                .buildAndExpand(meetingDTO.getId()) // ID로 경로 대체
                .toUri();

        // 201 Created 상태 코드와 Location 헤더 반환
        return ResponseEntity.created(location).body(meetingDTO);
    }


    @GetMapping(value = "/{meeting_id}/view")
    public MeetingDTO viewMeeting(
            @PathVariable("meeting_id")
            String meeting_id
    ){
        return meetingService.viewMeeting(meeting_id);

    }

    @GetMapping(value = "/{meeting_id}/check_agenda")
    public boolean checkAgenda(
            @PathVariable("meeting_id")
            String meeting_id
    ){

        log.info("is agendaexist?"+meetingService.isAgendaExist(meeting_id));

        return meetingService.isAgendaExist(meeting_id);

    }

    @PostMapping(value = "/{meeting_id}/add_agenda")
    public boolean addAgenda(
            @PathVariable("meeting_id")
            String meeting_id,

            @RequestBody
            AddAgendaRequest addAgendaRequest
    ){



        return meetingService.addAgenda(meeting_id,addAgendaRequest);

    }



    @GetMapping("/by_channel/{channel_id}")
    public List<MeetingsByChannelDTOList> getMeetingsByChannelId(
            @PathVariable(value = "channel_id")
            String channel_id

    ){
        return meetingService.viewByChannelId(channel_id);

    }



    @GetMapping("/{meeting_id}/delete")
    public boolean deleteMeetings(
            @PathVariable(value = "meeting_id")
            String meeting_id
    ){

        try {

            meetingService.deleteMeetings(meeting_id);
            return true;

        }catch (Exception exceptione){
            throw new IllegalStateException("delete meeting is not working in controller");
        }

    }


    @GetMapping(value = "/{meeting_id}/add_participant")
    public boolean addParticipant(){


return false;
    }







}
