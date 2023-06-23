package com.example.web_search_engine.repositories;

import com.example.web_search_engine.model.WebSite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchRepository extends JpaRepository<WebSite, Long> {
    WebSite findByUrl(String url);
}
