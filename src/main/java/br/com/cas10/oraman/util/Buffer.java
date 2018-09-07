package br.com.cas10.oraman.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class Buffer<T> {

  private final EvictingQueue<T> buffer;

  public Buffer(int maxSize) {
    buffer = EvictingQueue.create(maxSize);
  }

  public synchronized void add(T o) {
    buffer.offer(checkNotNull(o));
  }

  public synchronized List<T> toList() {
    return ImmutableList.copyOf(buffer);
  }
}
