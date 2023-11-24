package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

    private final CrawlerConfiguration config;

    private WebCrawlerMain(CrawlerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }

    @Inject
    private WebCrawler crawler;

    @Inject
    private Profiler profiler;

    private void run() throws Exception {
        Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

        CrawlResult result = crawler.crawl(config.getStartPages());
        CrawlResultWriter resultWriter = new CrawlResultWriter(result);
        /*
        Separate section of code to write output to System.out in a single block if both
        result and profile data are to be displayed on console.
        To avoid System.out resource being closed prematurely and
        not showing output of profiler information.
         */
        if (config.getResultPath().isEmpty() && config.getProfileOutputPath().isEmpty()) {
            try (Writer writer = new OutputStreamWriter(System.out)) {
                resultWriter.write(writer);
                // writing a new line to end here as noticed when both result and profiler output
                // written to console, there was no new line between them
                writer.write(String.format("%n"));
                profiler.writeData(writer);
                writer.flush();
            }
        }
        else {
            if (config.getResultPath().isEmpty()) {
                try (Writer writer = new OutputStreamWriter(System.out)) {
                    resultWriter.write(writer);
                    writer.flush();
                }
            } else {
                try (Writer writer = new FileWriter(this.config.getResultPath(), false)) {
                    resultWriter.write(writer);
                    writer.flush();
                }
            }
            if (config.getProfileOutputPath().isEmpty()) {
                try (Writer writer = new OutputStreamWriter(System.out)) {
                    profiler.writeData(writer);
                    writer.flush();
                }
            } else {
                try (Writer writer = new FileWriter(this.config.getProfileOutputPath(), true)) {
                    profiler.writeData(writer);
                    writer.flush();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: WebCrawlerMain [starting-url]");
            return;
        }

        CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
        new WebCrawlerMain(config).run();
    }
}