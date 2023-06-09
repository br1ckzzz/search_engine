package com.example.web_search_engine.handlers;

import com.example.web_search_engine.model.WebSite;
import com.example.web_search_engine.services.impl.IndexServiceImpl;

public class RunIndexing extends Thread {

    private final IndexServiceImpl indexService;
    private final WebSite site;

    public RunIndexing(WebSite site, IndexServiceImpl indexService) {
        this.site = site;
        this.indexService = indexService;

    }

    @Override
    public void run() {

        try {
            Thread.sleep(1000);
            indexService.indexingSite(site);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
