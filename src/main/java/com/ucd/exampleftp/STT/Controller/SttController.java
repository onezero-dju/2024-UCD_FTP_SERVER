package com.ucd.exampleftp.STT.Controller;

import com.ucd.exampleftp.STT.db.STTResponse;
import com.ucd.exampleftp.STT.service.STTResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/stt")
public class SttController {

final private STTResponseService sttResponseService;

    public SttController(STTResponseService sttResponseService) {
        this.sttResponseService = sttResponseService;
    }


    @GetMapping(value = "/meetings/{meeting_id}/get_texts")
    public List<STTResponse> getSTTResponse(
            @PathVariable(value = "meeting_id")
            String meeting_id
    ){
        return sttResponseService.getSTTResponse(meeting_id);

    }



}
