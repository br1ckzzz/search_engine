package com.example.web_search_engine.model.dto;

import lombok.*;

@AllArgsConstructor
@Data
public class SearchData {

    private String site;

    private String siteName;

    private String uri;

    private String title;

    private String snippet;

    private Float relevance;
}
