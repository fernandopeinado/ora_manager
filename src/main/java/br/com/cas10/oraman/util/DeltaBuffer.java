package br.com.cas10.oraman.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.function.BiFunction;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;

public class DeltaBuffer<T, U> {

  private final EvictingQueue<U> buffer;
  private final BiFunction<T, T, U> deltaFunction;
  private T last;

  public DeltaBuffer(int maxSize, BiFunction<T, T, U> deltaFunction) {
    this.buffer = EvictingQueue.create(maxSize);
    this.deltaFunction = deltaFunction;
  }

  public synchronized void add(T o) {
    checkNotNull(o);
    if (last != null) {
      buffer.offer(deltaFunction.apply(last, o));
    }
    last = o;
  }

  public synchronized List<U> toList() {
    return ImmutableList.copyOf(buffer);
  }
}
