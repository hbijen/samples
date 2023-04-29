package com.example.csvtransform;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvProcessor {

    private String inputDir;
	private Optional<String> outputDir;
	private Boolean skipHeader;
	private List<Integer> columns;

	private CsvProcessor() {
    }
	private CsvProcessor(Builder builder) {
		this.inputDir = builder.getInputDir();
		this.outputDir = builder.getOutputDir();
		this.columns = builder.getColumns();
	}

    public void process() {

		log.info("processing input folder " + inputDir);
		log.info("write to output folder " + outputDir);
		log.info("skipHeader " + skipHeader);

		List<Path> files = Util.getCsvFiles(inputDir);
		log.info("files.size() :  " + files.size());
		BufferedWriter writer = null;
		for (Path file : files) {
			log.info("processing file: " + file.getFileName() + " =====");

			try {
				if ( outputDir.isPresent() ) {
					writer = new BufferedWriter( new FileWriter(outputDir.get() + "/" + file.getFileName().toString()));
				}

				CSVFormat format = CSVFormat.DEFAULT;
				try (
					Reader reader = Files.newBufferedReader(file);
					CSVParser parser = new CSVParser(reader, format)
					) {
					Optional<String> row;
					for (CSVRecord record : parser) {
						row = columns.stream().map((i)-> record.get(i)).reduce((s1, s2) -> s1 + "," + s2);

						if (writer != null) {
							writer.write(row.get());
							writer.newLine();
						} else {
							log.info(row.get());
						}
					}
				} catch(Exception ex) {
					log.error("Error processing file: " + file.getFileName(), ex);	
				}

			} catch (IOException e) {
				log.error("Error opening file: " + file.getFileName(), e);
				e.printStackTrace();
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						// do nothing
					}
				}
			}

		}
	
    }
    
	@Data
	public static final class Builder {

		private String inputDir;
		private Optional<String> outputDir;
		private List<Integer> columns;

		public Builder() {
		}

		public CsvProcessor build() {
			return new CsvProcessor(this);
		}
	}    
}
