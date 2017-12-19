package com.lkuligin.training.dataflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;

public class VerifySample<T> implements SerializableFunction<Iterable<KV<String, T>>, Void> {

  private KV<String, T>[] expected;

  public VerifySample(KV<String, T>[] expected) {
    this.expected = expected;
  }

  @Override
  public Void apply(Iterable<KV<String, T>> actualIter) {
    //TODO take into account that the iterable might consist not from unique values only
    List<KV<String, T>> actual = new ArrayList<>();
    for (KV<String, T> el : actualIter) {
      actual.add(el);
    }
    assertEquals(this.expected.length, actual.size());
    for (KV<String, T> el : actual) {
      boolean matchFound = false;
      int i = 0;
      for (; i < this.expected.length; i++) {
        if (el.equals(this.expected[i])) {
          matchFound = true;
          break;
        }
      }
      assertTrue("Invalid input, element expected but not found " + el.toString(), matchFound);
    }
    return null;
  }

}
