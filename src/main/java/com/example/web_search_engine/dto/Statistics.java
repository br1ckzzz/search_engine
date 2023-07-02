package com.example.web_search_engine.dto;

import lombok.Data;

import java.util.List;

@Data
public class Statistics {

    private Total total;

    private List<Detailed> detailed;
}
