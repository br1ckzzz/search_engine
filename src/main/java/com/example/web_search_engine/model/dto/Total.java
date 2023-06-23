package com.example.web_search_engine.model.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
public class Total {
    private long sites;
    private long pages;
    private long lemmas;
    private boolean isIndexing;
}
