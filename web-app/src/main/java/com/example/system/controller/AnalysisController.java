package com.example.system.controller;

import com.example.system.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    // Trang web
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // API gọi từ JS
    @PostMapping("/api/run")
    @ResponseBody
    public String run(
            @RequestParam("file")
            MultipartFile file
    ) {
        try {
            return analysisService
                    .uploadToPython(file);
        } catch (Exception e) {
            return "{\"error\":\""
                    + e.getMessage()
                    + "\"}";
        }
    }

    @PostMapping("/api/llm")
    @ResponseBody
    public String llm() {
        try {
            return analysisService.getLLMInsight();
        } catch (Exception e) {
            return "{\"llm_analysis\":\"" + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public String chat(@RequestBody String requestBody) {
        try {
            return analysisService.chat(requestBody);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/api/predict")
    @ResponseBody
    public String predict(@RequestBody java.util.Map<String, Object> body) {
        try {
            java.util.List<String> sequence =
                    (java.util.List<String>) body.get("sequence");

            return analysisService.predictNext(sequence);

        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}