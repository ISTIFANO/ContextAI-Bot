package com.chatboot.chatboot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.nio.file.Files;

@Service
public class WhisperService {

    private final String apiKey;
    private final RestTemplate restTemplate;

    public WhisperService(@Value("${whisper.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    public String transcribe(File audioFile) throws Exception {
        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(audioBytes){
            @Override
            public String getFilename(){
                return audioFile.getName();
            }
        });
        body.add("model", "whisper-1");

        HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = "https://api.openai.com/v1/audio/transcriptions";

        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        String responseBody = response.getBody();
        int index = responseBody.indexOf("\"text\":\"");
        if(index != -1){
            int start = index + 8;
            int end = responseBody.indexOf("\"", start);
            if(end != -1){
                return responseBody.substring(start, end);
            }
        }
        return "ممكن الصوت ما تفهمش";
    }
}
