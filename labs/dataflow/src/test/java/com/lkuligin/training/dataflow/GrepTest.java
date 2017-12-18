package com.lkuligin.training.dataflow;

import com.google.common.io.Files;
import com.lkuligin.training.dataflow.Grep.GrepLineForInputFn;
import com.lkuligin.training.dataflow.Grep.GrepOptions;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.nio.charset.StandardCharsets;

@RunWith(JUnit4.class)
public class GrepTest {
    @Test
    public void testGrepLineForInputFn() throws Exception {
        DoFnTester<String, String> grepLineForInputFn =
                DoFnTester.of(new GrepLineForInputFn("test"));

        Assert.assertThat(grepLineForInputFn.processBundle("line1 test grep"),
                CoreMatchers.hasItems("line1 test grep"));
        Assert.assertThat(grepLineForInputFn.processBundle(" "),
                CoreMatchers.<String>hasItems());
        Assert.assertThat(grepLineForInputFn.processBundle("line1 test1 grep"),
                CoreMatchers.hasItems("line1 test1 grep"));
        Assert.assertThat(grepLineForInputFn.processBundle("line1 tell grep"),
                CoreMatchers.<String>hasItems());
    }

    @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void testGrep() throws Exception {
        File inputFile = tmpFolder.newFile();
        File outputFile = tmpFolder.newFile();
        Files.write(
                "line1 test1 import\nline2 test2\nline2 impor test\nlin4 import test4",
                inputFile,
                StandardCharsets.UTF_8);
        GrepOptions options = TestPipeline.testingPipelineOptions().as(GrepOptions.class);
        options.setInputDir(inputFile.getAbsolutePath());
        options.setOutputPrefix(outputFile.getAbsolutePath());
        options.setSearchTerm("import");
        Grep.main(TestPipeline.convertToArgs(options));
        Assert.assertTrue("Grep works properly",
                Files.readLines(outputFile, StandardCharsets.UTF_8).size() == 2);

    }


}
