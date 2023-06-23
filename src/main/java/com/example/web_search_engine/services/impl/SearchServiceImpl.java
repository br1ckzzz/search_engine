package com.example.web_search_engine.services.impl;

import com.example.web_search_engine.model.WebSite;
import com.example.web_search_engine.model.dto.SearchData;
import com.example.web_search_engine.repositories.SearchRepository;
import com.example.web_search_engine.response.ResponseService;
import com.example.web_search_engine.response.impl.ErrorResponse;
import com.example.web_search_engine.response.impl.SearchResponse;
import com.example.web_search_engine.services.SearchService;
import com.example.web_search_engine.services.handlers.SearchHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SearchServiceImpl implements SearchService {

    private final SearchHandler searchHandler;
    private final SearchRepository searchRepository;

    @Autowired
    public SearchServiceImpl(SearchHandler searchHandler, SearchRepository searchRepository) {
        this.searchHandler = searchHandler;
        this.searchRepository = searchRepository;
    }

    @Override
    public ResponseService searchContent(String query, String url, int offset, int limit) {

        if (query.isEmpty()) {
            ErrorResponse response = new ErrorResponse();
            response.setResult(false);
            response.setError("Введен пустой запрос");
            return response;
        }
        WebSite webSite = searchRepository.findByUrl(url);
        List<SearchData> searches = searchHandler.searchData(webSite, query, offset, limit);

        if (searches.isEmpty()) {
            ErrorResponse response = new ErrorResponse();
            response.setResult(false);
            response.setError("Не найдено: " + query.toUpperCase(Locale.ROOT));
            return response;
        }
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(searchHandler.getSearchCount());
        response.setData(searches);
        return response;
    }
}
