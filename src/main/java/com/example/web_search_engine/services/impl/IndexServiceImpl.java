package com.example.web_search_engine.services.impl;

import com.example.web_search_engine.config.ConfigProperty;
import com.example.web_search_engine.model.*;
import com.example.web_search_engine.repositories.FieldRepository;
import com.example.web_search_engine.repositories.IndexRepository;
import com.example.web_search_engine.repositories.SearchRepository;
import com.example.web_search_engine.response.ResponseService;
import com.example.web_search_engine.response.impl.ErrorResponse;
import com.example.web_search_engine.response.impl.Response;
import com.example.web_search_engine.services.IndexService;
import com.example.web_search_engine.handlers.IndexHandler;
import com.example.web_search_engine.handlers.RunIndexing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class IndexServiceImpl implements IndexService {

    private final IndexRepository indexRepository;
    private final LemmaServiceImpl lemmaService;
    private final PageServiceImpl pageService;
    private final ThreadPoolExecutor executor;
    private final IndexHandler indexHandler;
    private final ConfigProperty config;
    private final SearchRepository searchRepository;
    private final FieldRepository fieldRepository;

    @Autowired
    public IndexServiceImpl(IndexRepository indexRepository,
                            LemmaServiceImpl lemmaService,
                            PageServiceImpl pageService, IndexHandler indexHandler,
                            ConfigProperty config, SearchRepository searchRepository, FieldRepository fieldRepository) {
        this.indexRepository = indexRepository;
        this.lemmaService = lemmaService;
        this.pageService = pageService;
        this.config = config;
        this.indexHandler = indexHandler;
        this.searchRepository = searchRepository;
        this.fieldRepository = fieldRepository;
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Override
    public void indexingSite(WebSite site) {

        pageService.startCrawlSites(site, config.getUserAgent(), searchRepository);
        List<Field> fields = putAndGetFields();
        List<Page> pages = pageService.getPagesBySiteId(site.getId());
        lemmaService.createLemmas(pages);
        indexHandler.createIndexes(fields, pages, site);
        site.setStatusTime(LocalDateTime.now());
        site.setStatus(site.getStatus().equals(Status.FAILED) ? Status.FAILED : Status.INDEXED);
        searchRepository.save(site);
    }

    @Override
    public ResponseService indexingAll() {

        if (isIndexing()) {
            ErrorResponse response = new ErrorResponse();
            response.setResult(false);
            response.setError("Индексация уже запущена");
            return response;
        }

        List<WebSite> sites = searchRepository.findAll();
        sites.forEach(site -> clearDataBaseBySite(site.getId()));
        sites.forEach(site -> searchRepository.deleteById(site.getId()));
        sites.clear();

        config.getSites().forEach(s -> {
            WebSite site = new WebSite();
            site.setName(s.getName());
            site.setUrl(s.getUrl());
            site.setStatus(Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            searchRepository.save(site);
        });

        searchRepository.findAll().forEach(site -> executor.submit(new RunIndexing(site, this)));
        Response response = new Response();
        response.setResult(true);
        return response;
    }

    public boolean isIndexing() {
        return executor.getActiveCount() > 0;
    }

    @Override
    public ResponseService indexingOne(String url) {

        WebSite webSite = searchRepository.findByUrl(url);
        if (webSite != null) {
            clearDataBaseBySite(webSite.getId());
            executor.submit(new RunIndexing(webSite, this));
            Response response = new Response();
            response.setResult(true);
            return response;
        }
        WebSite site = findSiteFromProperty(url);
        if (site == null) {
            ErrorResponse response = new ErrorResponse();
            response.setResult(false);
            response.setError("Данный сайт находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
            return response;
        }
        searchRepository.save(site);
        executor.submit(new RunIndexing(site, this));
        Response response = new Response();
        response.setResult(true);
        return response;
    }

    public WebSite findSiteFromProperty(String url) {

        Optional<ConfigProperty.Site> siteFromProperty = config.getSites()
                .stream().filter(s -> s.getUrl().equals(url)).findFirst();
        if (siteFromProperty.isPresent()) {
            WebSite site = new WebSite();
            site.setUrl(siteFromProperty.get().getUrl());
            site.setName(siteFromProperty.get().getName());
            site.setStatus(Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            return site;
        } else {
            return null;
        }
    }

    @Override
    public ResponseService stopIndexing() {

        if (!isIndexing()) {
            ErrorResponse response = new ErrorResponse();
            response.setResult(false);
            response.setError("Индексация не запущена");
            return response;
        }

        executor.shutdownNow();

        if (!isAlive()) {
            searchRepository.findAll().forEach(site -> {
                site.setStatus(Status.FAILED);
                searchRepository.save(site);
            });
            Response response = new Response();
            response.setResult(true);
            return response;
        } else {
            ErrorResponse response = new ErrorResponse();
            response.setResult(false);
            response.setError("Ошибка остановки индексации");
            return response;
        }
    }

    public void deleteIndexesByPage(Long pageId) {
        indexRepository.deleteAllBySiteId(pageId);
    }

    public void clearDataBaseBySite(Long siteId) {
        pageService.getPagesBySiteId(siteId).forEach(page -> deleteIndexesByPage(page.getId()));
        lemmaService.deleteLemmasBySiteId(siteId);
        pageService.deletePagesBySiteId(siteId);
    }

    public boolean isAlive() {

        boolean isAlive = false;
        try {
            isAlive = executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            ie.fillInStackTrace();
        }
        return isAlive;
    }

    public Long countIndexes() {
        return indexRepository.count();
    }

    public List<Index> getIndexesByLemmaId(long lemmaId) {
        return indexRepository.findIndexByLemmaId(lemmaId);
    }

    public List<Field> putAndGetFields() {

        List<Field> fields = fieldRepository.findAll();

        if (fields.isEmpty()) {

            Field fieldTitle = new Field();
            Field fieldBody = new Field();

            fieldTitle.setName("title");
            fieldTitle.setSelector("title");
            fieldTitle.setWeight(1.0F);

            fieldBody.setName("body");
            fieldBody.setSelector("body");
            fieldBody.setWeight(0.8F);

            fields.add(fieldRepository.saveAndFlush(fieldTitle));
            fields.add(fieldRepository.saveAndFlush(fieldBody));
        }
        return new ArrayList<>(fields);
    }

}
