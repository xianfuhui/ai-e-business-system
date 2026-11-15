package com.example.system.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;

@Service
public class AnalysisService {
    @Value("${python.api.base-url}")
    private String baseUrl;

    /** Folder shared with the Python service (must match its DATASET_DIR). */
    @Value("${dataset.dir:../data}")
    private String datasetDir;

    public String getLLMInsight() throws Exception {

        String url = baseUrl + "/api/llm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }

    public String chat(String message) throws Exception {

        String url = baseUrl + "/api/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("message", message);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }

    public String predictNext(java.util.List<String> sequence) throws Exception {

        String url = baseUrl + "/api/predict";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("sequence", sequence);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }

    public String uploadToPython(
        MultipartFile file
    ) throws Exception {

        // Keep only the bare file name so an upload cannot escape the dataset folder.
        String filename = Paths.get(file.getOriginalFilename()).getFileName().toString();

        Path folder = Paths.get(datasetDir).toAbsolutePath().normalize();
        Files.createDirectories(folder);
        file.transferTo(folder.resolve(filename));

        String url =
                baseUrl + "/process";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        Map<String, String> body =
                new HashMap<>();

        body.put("filename", filename);

        HttpEntity<Map<String, String>>
                request =
                new HttpEntity<>(body, headers);

        RestTemplate restTemplate =
                new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url,
                        request,
                        String.class
                );

        return response.getBody();
    }
}