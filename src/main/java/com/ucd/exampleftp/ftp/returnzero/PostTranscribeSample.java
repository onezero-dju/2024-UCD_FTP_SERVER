package com.ucd.exampleftp.ftp.returnzero;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Component
@Slf4j
public class PostTranscribeSample {

    @Autowired
    GetTranscribeSample getTranscribeSample;

    public String postAudio(File file, int numOfPeople, String token) throws Exception {

        log.info("auth token: {}", token);

        String boundary = "authsample";
        String LINE_FEED = "\r\n";

        URL url = new URL("https://openapi.vito.ai/v1/transcribe");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Accept", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer " + token);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (DataOutputStream outputStream = new DataOutputStream(httpConn.getOutputStream())) {

            // 파일 파트 작성
            outputStream.writeBytes("--" + boundary + LINE_FEED);
            String sanitizedFileName = sanitizeFileName(file.getName());
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + sanitizedFileName + "\"" + LINE_FEED);
            String contentType = URLConnection.guessContentTypeFromName(sanitizedFileName);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            outputStream.writeBytes("Content-Type: " + contentType + LINE_FEED);
            outputStream.writeBytes(LINE_FEED);

            // 파일 데이터 스트리밍 (청크 단위)
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.writeBytes(LINE_FEED);
            }

            // 다음 파트 시작
            outputStream.writeBytes("--" + boundary + LINE_FEED);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"config\"" + LINE_FEED);
            outputStream.writeBytes("Content-Type: application/json" + LINE_FEED);
            outputStream.writeBytes(LINE_FEED);
            outputStream.writeBytes(
                    "{\n" +
                            "   \"use_diarization\": true,\n" +
                            "   \"diarization\": {\"spk_count\": " + numOfPeople + "},\n" +
                            "   \"use_itn\": false,\n" +
                            "   \"use_disfluency_filter\": false,\n" +
                            "   \"use_profanity_filter\": false,\n" +
                            "   \"use_paragraph_splitter\": true,\n" +
                            "   \"paragraph_splitter\": {\"max\": 50}\n" +
                            "}"
            );
            outputStream.writeBytes(LINE_FEED);

            // 최종 경계 작성
            outputStream.writeBytes("--" + boundary + "--" + LINE_FEED);
            outputStream.flush();
        } catch (IOException e) {
            log.error("Error while sending POST request", e);
            throw e;
        }

        // 응답 처리
        int responseCode = httpConn.getResponseCode();
        InputStream responseStream = responseCode / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        String response;
        try (Scanner s = new Scanner(responseStream, StandardCharsets.UTF_8.name())) {
            s.useDelimiter("\\A");
            response = s.hasNext() ? s.next() : "";
        }

        log.info("Response: {}", response);
        return response;
    }

    /**
     * 파일 이름을 정제하여 MIME 헤더 오류를 방지합니다.
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }
}
