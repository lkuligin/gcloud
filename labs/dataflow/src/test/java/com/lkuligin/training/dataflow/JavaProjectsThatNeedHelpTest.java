package com.lkuligin.training.dataflow;

import com.google.api.services.bigquery.model.TableRow;
import com.lkuligin.training.dataflow.JavaProjectsThatNeedHelp.PackagesUse;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Create.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JavaProjectsThatNeedHelpTest {
  @Rule
  public TestPipeline p = TestPipeline.create();

  @SuppressWarnings({"rawtypes", "unchecked"})
  static final KV<String, Double>[] EXPECTED = new KV[]{
      KV.of("org", Math.log(3)*Math.log(2)), KV.of("com", Math.log(2)*Math.log(1)), KV.of("com.test1", Math.log(1)*Math.log(1)), KV.of("org.test", Math.log(3)*Math.log(2))
  };

  @Test
  @Category(ValidatesRunner.class)
  public void PackagesUse() throws Exception {
    Values<TableRow> input = Create.of(Arrays.asList(
        new TableRow().set("content", "package org.test;\nimport com.test1;\n//TODO todo1;\n//TODO todo2;\n//FIXME fixme1"),
        new TableRow().set("content", "package com.test1;\nimport org.test;\n//TODO todo3;"),
        new TableRow().set("content", "package com.test2;\nimport org.test;\n//TODO todo4;")));
    PCollection<List<KV<String, Double>>> output = p
        .apply(input)
        .apply(new PackagesUse(5));

    PAssert.thatSingletonIterable(output).satisfies(new VerifySample(EXPECTED));

    p.run().waitUntilFinish();
  }

}
