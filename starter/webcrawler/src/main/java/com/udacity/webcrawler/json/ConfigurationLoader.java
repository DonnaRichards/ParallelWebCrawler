package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    // TODO: Fill in this method.
    Reader reader = null;
    try {
      reader = new BufferedReader(new FileReader(path.toString()));
      CrawlerConfiguration crawlerConfiguration =  read(reader);
      reader.close();
      return crawlerConfiguration;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    // return new CrawlerConfiguration.Builder().build();
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(AUTO_CLOSE_SOURCE);
    try {
      return mapper.readValue(reader, CrawlerConfiguration.Builder.class).build();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
