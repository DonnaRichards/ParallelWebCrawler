package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    if (klass.getMethods().length==0){
      throw new IllegalArgumentException("Your class has no methods, cannot be used by Profiler");
    }
    Objects.requireNonNull(klass);
    InvocationHandler handler = new ProfilingMethodInterceptor(clock, state, delegate);
    T proxy = (T) Proxy.newProxyInstance(klass.getClassLoader(),
            new Class[]{klass},
            handler);
    return proxy;
  }

  @Override
  public void writeData(Path path) {
    Objects.requireNonNull(path);
    try (Writer writer = Files.newBufferedWriter(path)){
      writeData(writer);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
