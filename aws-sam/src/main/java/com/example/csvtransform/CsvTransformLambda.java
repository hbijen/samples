package com.example.csvtransform;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
public class CsvTransformLambda implements RequestHandler<Object, Object> {
    private final S3Client s3;
    private String bucketName;
    private String s3IN;
    private String s3OUT;

    private String tmpInFolder;
    private String tmpOutFolder;


    public CsvTransformLambda() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3 = DependencyFactory.s3Client();
        
        bucketName = System.getenv("BUCKET_NAME");
        s3IN = System.getenv("IN_PREFIX");
        s3OUT = System.getenv("OUT_PREFIX");

        // create temporary folders if it does not exists
        tmpInFolder = createTmpFolder("input");
        tmpOutFolder = createTmpFolder("output");
    }

    public String createTmpFolder(Object subfolder) {
        File folder = new File("/tmp/" + subfolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }
    public Boolean deleteTmpFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file.delete();
        }
        //set true if it does not exist
        return true;
    }
    private List<S3Object> getObjects(String name) {
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(name)
                .build();
        ListObjectsResponse res = s3.listObjects(listObjects);
        return res.contents();
    }

    @Override
    public Object handleRequest(final Object input, final Context context) {

        log.info(String.format("%s Invoking function %s", context.getAwsRequestId(), context.getFunctionName()));
        List<S3Object> objects = getObjects(bucketName);
        
        // get the list of input csv files
        List<String> inputCsvKeys = objects.stream().filter((d) -> d.key().startsWith(s3IN)).map((d) -> d.key()).collect(Collectors.toList());

        //read from S3
        for (String inputKey : inputCsvKeys) {
            log.info("Processing input file " + inputKey);

            GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(inputKey).build();
            Path filepath = Paths.get(tmpInFolder, Paths.get(inputKey).getFileName().toString());
            s3.getObject(request, filepath);
        }

        //log.info("Processing input file " + inputKey);
        processFiles("/tmp/input");

        //write to S3
        List<Path> outFiles = Util.getCsvFiles("/tmp/output");
        for (Path file : outFiles) {
            PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(s3OUT + "/" + file.getFileName()).build();
            s3.putObject(request, file);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("file_count", inputCsvKeys.size());
        return result;
    }


    private void processFiles(String inFolder) {
        CsvProcessor.Builder csvProcessorBuilder = new CsvProcessor.Builder();
        csvProcessorBuilder.setInputDir(inFolder);
        csvProcessorBuilder.setOutputDir(Optional.of("/tmp/output"));

        List<Integer> indexes =  Arrays.asList("0,1".split(",")).stream().map((d) -> Integer.parseInt(d)).collect(Collectors.toList());
        csvProcessorBuilder.setColumns(indexes);
        csvProcessorBuilder.build().process();
    }
    
}
