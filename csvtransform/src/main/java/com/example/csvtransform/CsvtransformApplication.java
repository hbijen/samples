package com.example.csvtransform;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

@SpringBootApplication
@CommandLine.Command(name = "csv-transform", mixinStandardHelpOptions = true)
@Slf4j
public class CsvtransformApplication implements Runnable {

    @CommandLine.Option(names = { "-i", "--input-dir" }, required = true, description = "Input directory")
    private String inputDir;
    
    @CommandLine.Option(names = { "-o", "--output-dir" }, required = false, description = "Output directory")
    private String outputDir;

    @CommandLine.Option(names = { "-c", "--columns" }, defaultValue = "0,1", required = false, description = "comma separated index of each field to be extracted, defaults to first two columns")
    private String columns;

	public static void main(String[] args) {
        try {
		    new CommandLine(new CsvtransformApplication()).execute(args);
        } catch (ParameterException ex) { // command line arguments could not be parsed
            System.err.println(ex.getMessage());
            ex.getCommandLine().usage(System.err);
        }
	}

    @Override
    public void run() {
        CsvProcessor.Builder csvProcessorBuilder = new CsvProcessor.Builder();

        csvProcessorBuilder.setInputDir(inputDir);
        csvProcessorBuilder.setOutputDir(Optional.ofNullable(outputDir));

        List<Integer> indexes =  Arrays.asList(columns.split(",")).stream().map((d) -> Integer.parseInt(d)).collect(Collectors.toList());
        csvProcessorBuilder.setColumns(indexes);

        csvProcessorBuilder.build().process();

    }

}
