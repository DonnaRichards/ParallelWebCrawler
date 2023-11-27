package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final ProfilingState state;
  private final Object delegate;

  ProfilingMethodInterceptor(Clock clock, ProfilingState state, Object delegate) {
    this.clock = Objects.requireNonNull(clock);
    this.state = state;
    this.delegate = delegate;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
    if (method.getAnnotation(Profiled.class) != null) {
      if ("equals".equals(method.getName()) && Object.class.equals(method.getDeclaringClass())) {
        // Special handling for Object#equals(Object)
        return delegate.equals(args[0]);
      }
      else {
        Instant startMethodTime = clock.instant();
        try {
          return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } finally {
            Instant finishMethodTime = clock.instant();
            state.record(delegate.getClass(), method,
                    Duration.between(startMethodTime, finishMethodTime));
        }
      }
    }
    else {
      return method.invoke(delegate, args);
    }
  }
}
