//package com.ucd.exampleftp.meeting.listener;
//
//import com.ucd.exampleftp.STT.db.STTResponse;
//import com.ucd.exampleftp.meeting.service.MeetingSseService;
//import com.ucd.exampleftp.util.config.rabbitMQ.RabbitMQConfig;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class STTResponseListener {
//
//    @Autowired
//    private MeetingSseService meetingSseService;
//
//    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
//    public void receiveSTTResponse(STTResponse sttResponse) {
//        log.info("Received STTResponse from RabbitMQ: {}", sttResponse);
//
//        // meetingId를 사용하여 해당 회의에 연결된 클라이언트에게 메시지 전송
//        String meetingId = sttResponse.getMeetingId();
//
//        if (meetingId != null && !meetingId.isEmpty()) {
//            meetingSseService.sendSTTResponse(meetingId, sttResponse);
//        } else {
//            log.warn("Received STTResponse with null or empty meetingId: {}", sttResponse);
//        }
//    }
//}
