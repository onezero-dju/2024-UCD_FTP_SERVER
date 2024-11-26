package com.ucd.exampleftp.meeting.Controller;

import com.ucd.exampleftp.meeting.service.MeetingSseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/meetings")
@Slf4j
public class MeetingSseController {

    @Autowired
    private MeetingSseService meetingSseService;


    @GetMapping(value = "/{meetingId}/live-updates", produces = "text/event-stream")
    public SseEmitter streamMeetingEvents(@PathVariable String meetingId){
        // SseEmitter 객체 생성 (타임아웃 설정)
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
      log.info("meetingId--"+meetingId+"--");

        // 연결된 클라이언트를 목록에 저장
        meetingSseService.addEmitter(meetingId, emitter);

        return emitter;
    }
}
