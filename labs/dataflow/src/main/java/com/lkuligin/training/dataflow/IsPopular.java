package com.lkuligin.training.dataflow;

import static com.lkuligin.training.dataflow.PackageParser.getPackages;

import java.util.List;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.Top;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class IsPopular {
    public interface IsPopularOptions extends PipelineOptions {
        @Description("Output prefix")
        @Default.String("/tmp/output.csv")
        String getOutputPrefix();
        void setOutputPrefix(String s);

        @Description("Input directory")
        @Default.String("src/main/java/com/lkuligin/training/dataflow/*.java")
        String getInputDir();
        void setInputDir(String s);

        @Description("Keyword to search")
        @Default.String("import")
        String getKeyword();
        void setKeyword(String s);

        @Description("Top packages to take")
        @Default.Integer(5)
        Integer getTopCount();
        void setTopCount(Integer i);

    }

    public static class LineStartsWithDoFn extends DoFn<String, String> {
        private String keyword;

        public LineStartsWithDoFn (String keyword) {
            this.keyword = keyword;
        }

        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            String line = c.element();
            if (line.startsWith(this.keyword)) {
                c.output(line);
            }
        }
    }

    public static class PackageUseDoFn extends DoFn<String, KV<String, Integer>> {
        private String keyword;

        public PackageUseDoFn(String keyword) {
            this.keyword = keyword;
        }

        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            List<String> packages = getPackages(c.element(), keyword);
            for (String p : packages) {
                c.output(KV.of(p, 1));
            }
        }
    }

    public static class PackageUse extends PTransform<PCollection<String>, PCollection<List<KV<String, Integer>>>> {
        String keyword;
        Integer topCount;

        public PackageUse(String keyword, Integer topCount) {
            this.keyword = keyword;
            this.topCount = topCount;
        }

        @Override
        public PCollection<List<KV<String, Integer>>> expand(PCollection<String> lines) {

            return lines.apply("GetImports", ParDo.of(new LineStartsWithDoFn(keyword)))
                    .apply("PackageUse", ParDo.of(new PackageUseDoFn(keyword)))
                    .apply(Sum.integersPerKey())
                    .apply("Top", Top.of(this.topCount, new KV.OrderByValue<>()));
        }
    }

    @SuppressWarnings("serial")
    public static void main(String[] args) {
        IsPopularOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(IsPopularOptions.class);
        Pipeline p = Pipeline.create(options);

        String outputPrefix = options.getOutputPrefix();

        p
                .apply("GetJava", TextIO.read().from(options.getInputDir()))
                .apply(new PackageUse(options.getKeyword(), options.getTopCount()))
                .apply("ToString", ParDo.of(new DoFn<List<KV<String, Integer>>, String>() {

                    @ProcessElement
                    public void processElement(ProcessContext c) throws Exception {
                        StringBuilder sb = new StringBuilder();
                        for (KV<String, Integer> kv : c.element()) {
                            if (sb.length()>0) sb.append('\n');
                            sb.append(kv.getKey() + "," + kv.getValue());
                        }
                        c.output(sb.toString());
                    }

                }))
                .apply(TextIO.write().to(outputPrefix).withoutSharding());

        p.run();
    }

}
