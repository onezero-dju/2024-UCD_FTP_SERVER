package com.ucd.exampleftp.returnzero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


@Component
public class PostTranscribeSample {

    @Autowired
    GetTranscribeSample getTranscribeSample;

    public String postAudio(File file, int numOfPeople, String token) throws Exception {




        URL url = new URL("https://openapi.vito.ai/v1/transcribe");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("accept", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer "+ token);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=authsample");
        httpConn.setDoOutput(true);


        DataOutputStream outputStream;
        outputStream = new DataOutputStream(httpConn.getOutputStream());

        outputStream.writeBytes("--authsample\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + file.getName() +"\"\r\n");
        outputStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName()) + "\r\n");
        outputStream.writeBytes("Content-Transfer-Encoding: binary" + "\r\n");
        outputStream.writeBytes("\r\n");

        FileInputStream in =new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            outputStream.write(buffer,0,bytesRead);
            outputStream.writeBytes("\r\n");
            outputStream.writeBytes("--authsample\r\n");
        }
        outputStream.writeBytes("\r\n");
        outputStream.writeBytes("--authsample\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"config\"\r\n");
        outputStream.writeBytes("Content-Type: application/json\r\n");
        outputStream.writeBytes("\r\n");
        outputStream.writeBytes(
                "{\n" +
                        "   \"use_diarization\": true,\n" +
                        "   \"diarization\": {\"spk_count\": 2},\n" +
                        "   \"use_itn\": false,\n" +
                        "   \"use_disfluency_filter\": false,\n" +
                        "   \"use_profanity_filter\": false,\n" +
                        "   \"use_paragraph_splitter\": true,\n" +
                        "   \"paragraph_splitter\": {\"max\": 50}\n" +
                        "}"
        );
        outputStream.writeBytes("\r\n");
        outputStream.writeBytes("--authsample\r\n");
        outputStream.flush();
        outputStream.close();

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        s.close();
        System.out.println(response);

        return response;
    }
}