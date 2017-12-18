package com.lkuligin.training.dataflow;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

public class Grep {

    public static class GrepLineForInputFn extends DoFn<String, String> {
        private String searchTerm;

        public GrepLineForInputFn(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            String line = c.element();
            if (line.contains(this.searchTerm)) {
                c.output(line);
            }
        }
    }

    public interface GrepOptions extends PipelineOptions {
        @Description("Path to the input files")
        @Default.String("src/main/java/com/lkuligin/training/dataflow/*.java")
        String getInputDir();

        void setInputDir(String value);

        @Description("Output prefix")
        @Default.String("/tmp/output.txt")
        String getOutputPrefix();

        void setOutputPrefix(String value);

        @Description("Search term")
        @Default.String("import")
        String getSearchTerm();

        void setSearchTerm(String value);
    }


    @SuppressWarnings("serial")
    public static void main(String[] args) {
        GrepOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(GrepOptions.class);
        Pipeline p = Pipeline.create(options);

        p
                .apply("GetJava", TextIO.read().from(options.getInputDir()))
                .apply("Grep", ParDo.of(new GrepLineForInputFn(options.getSearchTerm())))
                .apply(TextIO.write()
                        .to(options.getOutputPrefix())//.withSuffix(".txt")
                        .withoutSharding());

        p.run();
    }
}
