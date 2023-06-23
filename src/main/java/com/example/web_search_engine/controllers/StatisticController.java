package com.example.web_search_engine.controllers;

import com.example.web_search_engine.response.ResponseService;
import com.example.web_search_engine.services.impl.StatisticServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class StatisticController {

    private final StatisticServiceImpl statisticService;

    @GetMapping("/statistics")
    public ResponseEntity<Object> statistics() {
        ResponseService response = statisticService.getStatistics();
        return ResponseEntity.ok(response);
    }
}
