package com.ucd.exampleftp.STT.db;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Results {

    private List<Utterance> utterances;

    private boolean verified;
}
