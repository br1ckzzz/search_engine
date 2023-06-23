package com.example.web_search_engine.services.impl;

import com.example.web_search_engine.model.Page;
import com.example.web_search_engine.model.WebSite;
import com.example.web_search_engine.repositories.PageRepository;
import com.example.web_search_engine.repositories.SearchRepository;
import com.example.web_search_engine.services.PageService;
import com.example.web_search_engine.services.handlers.ReadSiteRecursive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    @Autowired
    public PageServiceImpl(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Override
    public synchronized void startCrawlSites(WebSite site, String userAgent, SearchRepository searchRepository) {
        ReadSiteRecursive readSite = new ReadSiteRecursive(site.getUrl(), site, userAgent);
        new ForkJoinPool().invoke(readSite);
        pageRepository.saveAll(ReadSiteRecursive.getPageList());
    }

    public Long getCountBySiteId(Long siteId) {
        return pageRepository.findPageCountBySiteId(siteId);
    }

    public Long getPagesCount() {
        return pageRepository.count();
    }

    public List<Page> getPagesBySiteId(Long siteId) {
        return pageRepository.findPagesBySiteId(siteId);
    }

    public Page getPageById(long pageId) {
        return pageRepository.getById(pageId);
    }

    public void deletePagesBySiteId(Long siteId) {
        pageRepository.deleteAllBySiteId(siteId);
    }

}
