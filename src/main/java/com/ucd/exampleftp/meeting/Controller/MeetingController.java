package com.ucd.exampleftp.meeting.Controller;

import com.ucd.exampleftp.meeting.model.MeetingCreateRequest;
import com.ucd.exampleftp.meeting.model.MeetingDTO;
import com.ucd.exampleftp.meeting.model.Test;
import com.ucd.exampleftp.meeting.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping(value = "/api/meeting")
public class MeetingController {


    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping(value = "create")
    public ResponseEntity<MeetingDTO> createMeeting(
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


    @GetMapping(value = "/view/{meeting_id}")
    public MeetingDTO viewMeeting(
            @PathVariable("meeting_id")
            String meeting_id
    ){
        return meetingService.viewMeeting(meeting_id);

    }

    @GetMapping(value = "/check_agenda/{meeting_id}")
    public boolean checkAgenda(
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
