package com.lkuligin.training.dataflow;

import com.google.common.io.Files;
import com.lkuligin.training.dataflow.IsPopular.IsPopularOptions;
import com.lkuligin.training.dataflow.IsPopular.LineStartsWithDoFn;
import com.lkuligin.training.dataflow.IsPopular.PackageUse;
import com.lkuligin.training.dataflow.IsPopular.PackageUseDoFn;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(JUnit4.class)
public class IsPopularTest {
    @Test
    public void testLineStartsWithDoFn() throws Exception {
        DoFnTester<String, String> lineStartsWithDoFn =
                DoFnTester.of(new LineStartsWithDoFn("import"));

        Assert.assertThat(lineStartsWithDoFn.processBundle("import java.io.File;"),
                CoreMatchers.hasItems("import java.io.File;"));
        Assert.assertThat(lineStartsWithDoFn.processBundle(" "),
                CoreMatchers.<String>hasItems());
        Assert.assertThat(lineStartsWithDoFn.processBundle("java.io.File;"),
                CoreMatchers.<String>hasItems());
        Assert.assertThat(lineStartsWithDoFn.processBundle("//import java.io.File;"),
                CoreMatchers.<String>hasItems());
    }

    @Test
    public void testPackageUseDoFn() throws Exception {
        DoFnTester<String, KV<String, Integer>> packageUseDoFn =
                DoFnTester.of(new PackageUseDoFn("import"));

        Assert.assertThat(packageUseDoFn.processBundle("import java.io.File;"),
                CoreMatchers.hasItems(KV.of("java", 1), KV.of("java.io", 1), KV.of("java.io.File", 1)));
        Assert.assertThat(packageUseDoFn.processBundle("import java.io.File; import java.util.List;"),
                CoreMatchers.hasItems(KV.of("java", 1), KV.of("java.io", 1), KV.of("java.io.File", 1)));
        Assert.assertThat(packageUseDoFn.processBundle(" "),
                CoreMatchers.<KV<String, Integer>>hasItems());
    }

    @Rule
    public TestPipeline p = TestPipeline.create();

    @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();

    static final String[] LINES_ARRAY = new String[] {
            "import java.util.List;", "public class Example {", "}",
            "", "import org.apache.beam.sdk.values.KV;", "import org.apache.beam.sdk.values.PCollection;"};
    static final List<String> LINES = Arrays.asList(LINES_ARRAY);

    @SuppressWarnings({"rawtypes", "unchecked"})
    static final KV<String, Integer>[] EXPECTED = new KV[] {
            KV.of("org.apache.beam.sdk", 2), KV.of("org", 2), KV.of("org.apache.beam.sdk.values",  2),
            KV.of("org.apache", 2), KV.of("org.apache.beam", 2),
            KV.of("java", 1), KV.of("java.util", 1), KV.of("java.util.List", 1),
            KV.of("org.apache.beam.sdk.values.PCollection", 1), KV.of("org.apache.beam.sdk.values.KV", 1)
    };

    private static class VerifySample implements SerializableFunction<Iterable<KV<String, Integer>>, Void> {
        private KV<String, Integer>[] expected;

        private VerifySample(KV<String, Integer>[] expected) {
            this.expected = expected;
        }

        @Override
        public Void apply(Iterable<KV<String, Integer>> actualIter) {
            //TODO take into account that the iterable might consist not from unique values only
            List<KV<String, Integer>> actual = new ArrayList<>();
            for (KV<String, Integer> el : actualIter) {
                actual.add(el);
            }
            assertEquals(this.expected.length, actual.size());
            for (KV<String, Integer> el: actual) {
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

    @Test
    @Category(ValidatesRunner.class)
    public void testPackageUser() throws Exception {
        PCollection<String> input = p.apply(Create.of(LINES).withCoder(StringUtf8Coder.of()));

        PCollection<List<KV<String, Integer>>> output = input.apply(new PackageUse("import", 10));

        //top.Of returns a PCollection<List<T>> with a single element, so we need a hack to compare lists of KV<>s
        PAssert.thatSingletonIterable(output).satisfies(new VerifySample(EXPECTED));
        p.run().waitUntilFinish();
    }

    @Test
    public void testGrep() throws Exception {
        File inputFile = tmpFolder.newFile();
        File outputFile = tmpFolder.newFile();

        StringBuilder lines1 = new StringBuilder();

        for (String line : LINES) {
            if (lines1.length() > 0) {
                lines1.append("\n");
            }
            lines1.append(line);
        }

        Files.write(lines1.toString(), inputFile, StandardCharsets.UTF_8);

        IsPopularOptions options = TestPipeline.testingPipelineOptions().as(IsPopularOptions.class);
        options.setInputDir(inputFile.getAbsolutePath());
        options.setOutputPrefix(outputFile.getAbsolutePath());
        options.setKeyword("import");
        options.setTopCount(10);
        IsPopular.main(TestPipeline.convertToArgs(options));

        Assert.assertTrue("Search works properly",
                Files.readLines(outputFile, StandardCharsets.UTF_8).size() == 10);

    }

}
