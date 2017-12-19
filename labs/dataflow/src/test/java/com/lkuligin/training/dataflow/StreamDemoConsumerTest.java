package com.lkuligin.training.dataflow;


import com.google.api.services.bigquery.model.TableRow;
import com.lkuligin.training.dataflow.StreamDemoConsumer.StreamWordCount;
import org.apache.beam.sdk.coders.StringDelegateCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TimestampedValue;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StreamDemoConsumerTest {

  private Instant baseTime = new Instant(0);
  private long windowDuration = 10;
  private long windowPeriod = 5;

  private TimestampedValue<String> event(String value, Duration baseTimeOffset) {
    return TimestampedValue.of(value, baseTime.plus(baseTimeOffset));
  }

  @Rule
  public TestPipeline p = TestPipeline.create();

  @Test
  @Category(ValidatesRunner.class)
  public void testStreamDemoConsumerArriveOnTime() throws Exception

  {
    TestStream<String> createWords = TestStream
        .create(StringUtf8Coder.of())
        .advanceWatermarkTo(baseTime)
        .addElements(event("line1 test1 word1", Duration.standardSeconds(3)),
            event("line2 test2 word2", Duration.standardSeconds(5)),
            event("line3 test3 word3 word4", Duration.standardSeconds(11))
        )
        //close the window
        .advanceWatermarkToInfinity();

    PCollection<Integer> output = p.apply(createWords)
        .apply(new StreamWordCount(windowDuration, windowPeriod));

    PAssert.that(output).inOnTimePane(new IntervalWindow(baseTime, Duration.standardSeconds(windowDuration))).containsInAnyOrder(6);
    PAssert.that(output).inOnTimePane(new IntervalWindow(baseTime.plus(Duration.standardSeconds(5)), Duration.standardSeconds(windowDuration))).containsInAnyOrder(7);

    p.run().waitUntilFinish();
  }

  @Test
  @Category(ValidatesRunner.class)
  //arives late but still within the window
  public void testStreamDemoConsumerArriveLate1() throws Exception

  {
    TestStream<String> createWords = TestStream
        .create(StringUtf8Coder.of())
        .advanceWatermarkTo(baseTime)
        .addElements(event("line1 test1 word1", Duration.standardSeconds(3)),
            event("line2 test2 word2", Duration.standardSeconds(5))
        )
        .advanceWatermarkTo(baseTime.plus(Duration.standardSeconds(13)))
        .addElements(event("line3 test3 word3 word4", Duration.standardSeconds(11)))
        //close the window
        .advanceWatermarkToInfinity();

    PCollection<Integer> output = p.apply(createWords)
        .apply(new StreamWordCount(windowDuration, windowPeriod));

    PAssert.that(output).inOnTimePane(new IntervalWindow(baseTime, Duration.standardSeconds(windowDuration))).containsInAnyOrder(6);
    PAssert.that(output).inOnTimePane(new IntervalWindow(baseTime.plus(Duration.standardSeconds(5)), Duration.standardSeconds(windowDuration))).containsInAnyOrder(7);

    p.run().waitUntilFinish();
  }
}
