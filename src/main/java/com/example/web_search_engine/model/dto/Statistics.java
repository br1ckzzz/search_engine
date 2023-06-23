package com.example.web_search_engine.model.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
public class Statistics {

    private Total total;

    private List<Detailed> detailed;
}
