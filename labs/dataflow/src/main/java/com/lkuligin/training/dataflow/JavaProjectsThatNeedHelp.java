package com.lkuligin.training.dataflow;


import static com.lkuligin.training.dataflow.PackageParser.countCallsForHelp;
import static com.lkuligin.training.dataflow.PackageParser.parseImportStatement;
import static com.lkuligin.training.dataflow.PackageParser.parsePackageStatement;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.Top;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.transforms.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class JavaProjectsThatNeedHelp {

  public interface JavaProjectsThatNeedHelpOptions extends PipelineOptions {

    @Description("Output prefix")
    @Default.String("/tmp/output.csv")
    String getOutputPrefix();

    void setOutputPrefix(String s);

    @Description("Top packages to take")
    @Default.Integer(1000)
    Integer getTopCount();

    void setTopCount(Integer i);
  }

  public static class SplitRowToLines extends DoFn<TableRow, String[]> {

    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      {
        TableRow row = c.element();
        String content = (String) row.get("content");
        if (content != null) {
          String[] lines = content.split("\n");
          if (lines.length > 0) {
            c.output(lines);
          }
        }
      }
    }
  }

  private static class PackagesNeedHelp extends DoFn<String[], KV<String, Integer>> {

    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      String[] lines = c.element();
      String[] packages = parsePackageStatement(lines);
      int numHelpNeeded = countCallsForHelp(lines);
      if (numHelpNeeded > 0) {
        for (String packageName : packages) {
          c.output(KV.of(packageName, numHelpNeeded));
        }
      }
    }
  }

  public static class PackagesUse extends PTransform<PCollection<TableRow>, PCollection<List<KV<String, Double>>>> {
    private int topAmount;

    public PackagesUse(int topAmount) {
      this.topAmount = topAmount;
    }

    @Override
    public PCollection<List<KV<String, Double>>> expand(PCollection<TableRow> input) {
      PCollection<String[]> javaContent = input.apply("ToLines",
          ParDo.of(new SplitRowToLines()));

      PCollectionView<Map<String, Integer>> packagesThatNeedHelp = javaContent
          .apply("NeedsHelp", ParDo.of(new PackagesNeedHelp()))
          .apply("SumPackagesNeededHelp", Sum.integersPerKey())
          .apply("ToView", View.asMap());

      return javaContent
          .apply("IsPopular", ParDo.of(new DoFn<String[], KV<String, Integer>>() {
            @ProcessElement
            public void processElement(ProcessContext c) throws Exception {
              String[] lines = c.element();
              String[] packages = parseImportStatement(lines);
              for (String packageName : packages) {
                c.output(KV.of(packageName, 1)); // used once
              }
            }
          }))
          .apply("SumPackagesUsed", Sum.integersPerKey())
          .apply("CompositeScore",
              ParDo.of(new DoFn<KV<String, Integer>, KV<String, Double>>() {
                @ProcessElement
                public void processElement(ProcessContext c) throws Exception {
                  String packageName = c.element().getKey();
                  int numTimesUsed = c.element().getValue();
                  Integer numHelpNeeded = c.sideInput(packagesThatNeedHelp).get(packageName);
                  if (numHelpNeeded != null) c.output(KV.of(packageName, Math.log(numTimesUsed) * Math.log(numHelpNeeded)));
                }
              }).withSideInputs(packagesThatNeedHelp))
          .apply("Top_" + topAmount, Top.of(topAmount, new KV.OrderByValue<>()));
    }
  }


  @SuppressWarnings("serial")
  public static void main(String[] args) {
    JavaProjectsThatNeedHelpOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
        .as(JavaProjectsThatNeedHelpOptions.class);
    Pipeline p = Pipeline.create(options);

    String javaQuery = "SELECT content FROM [fh-bigquery:github_extracts.contents_java_2016]";
    p
        .apply("GetJava", BigQueryIO.read().fromQuery(javaQuery)) //
        .apply(new PackagesUse(options.getTopCount()))
        .apply("ToString", ParDo.of(new DoFn<List<KV<String, Double>>, String>() {

          @ProcessElement
          public void processElement(ProcessContext c) throws Exception {
            List<KV<String, Double>> sorted = new ArrayList<>(c.element());
            Collections.sort(sorted, new KV.OrderByValue<>());
            Collections.reverse(sorted);

            StringBuffer sb = new StringBuffer();
            for (KV<String, Double> kv : c.element()) {
              sb.append(kv.getKey() + "," + kv.getValue() + '\n');
            }
            c.output(sb.toString());
          }

        }))
        .apply(TextIO.write().to(options.getOutputPrefix()).withoutSharding());

    p.run();
  }

}
