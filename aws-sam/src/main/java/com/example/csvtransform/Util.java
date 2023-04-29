package com.example.csvtransform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {
    
    public static List<Path> getCsvFiles(String dir) {
        List<Path> files = new ArrayList<>();
		try {
			files = Files.list(Paths.get(dir))
								.filter(path -> path.toString().endsWith(".csv"))
								.sorted().collect(Collectors.toList());
		} catch (IOException e) {
			log.error("unable to read dir " + dir, e);
		}
        return files;
    }
}
