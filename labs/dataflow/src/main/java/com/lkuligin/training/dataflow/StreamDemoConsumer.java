package com.lkuligin.training.dataflow;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import com.google.api.services.bigquery.model.TableRow;
import java.time.Instant;

public class StreamDemoConsumer {

  @VisibleForTesting
  private interface StreamDemoConsumerOptions extends DataflowPipelineOptions {

    @Description("Output BigQuery table <project_id>:<dataset_id>.<table_id>")
    @Default.String("test-project-188517:demos.streamdemo")
    String getOutput();

    void setOutput(String s);

    @Description("Input topic")
    @Default.String("projects/test-project-188517/topics/streamdemo")
    String getInput();

    void setInput(String s);
  }

  @VisibleForTesting
  static class StreamWordCount extends PTransform<PCollection<String>, PCollection<Integer>> {
    private long windowDuration;
    private long windowPeriod;

    @VisibleForTesting
    public StreamWordCount(long windowDuration, long windowPeriod)  {
      this.windowDuration = windowDuration;
      this.windowPeriod = windowPeriod;
    }

    @Override
    public PCollection<Integer> expand(PCollection<String> input) {
      return input
          .apply("window",
              Window.into(SlidingWindows
                  .of(Duration.standardSeconds(this.windowDuration))
                  .every(Duration.standardSeconds(this.windowPeriod))))
          .apply("WordsPerLine", ParDo.of(new DoFn<String, Integer>() {
            @ProcessElement
            public void processElement(ProcessContext c) throws Exception {
              String line = c.element();
              c.output(line.split(" ").length);
            }
          }))
          .apply("WordsInTimeWindow", Sum.integersGlobally().withoutDefaults())
          ;
    }
  }


  @SuppressWarnings("serial")
  public static void main(String[] args) {
    StreamDemoConsumerOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
        .as(StreamDemoConsumerOptions.class);
    options.setStreaming(true);
    Pipeline p = Pipeline.create(options);

    String topic = options.getInput();
    String output = options.getOutput();

    // Build the table schema for the output table.
    List<TableFieldSchema> fields = new ArrayList<>();
    fields.add(new TableFieldSchema().setName("timestamp").setType("TIMESTAMP"));
    fields.add(new TableFieldSchema().setName("num_words").setType("INTEGER"));
    TableSchema schema = new TableSchema().setFields(fields);

    p
        .apply("GetMessages", PubsubIO.readStrings().fromTopic(topic)) //
        .apply(new StreamWordCount(120, 30))
        .apply("ToBQRow", ParDo.of(new DoFn<Integer, TableRow>() {
          @ProcessElement
          public void processElement(ProcessContext c) throws Exception {
            TableRow row = new TableRow();
            row.set("timestamp", Instant.now().toString());
            row.set("num_words", c.element());
            c.output(row);
          }
        }))
        .apply(BigQueryIO.writeTableRows().to(output).withSchema(schema)
            .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
            .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED));

    p.run();
  }

}
