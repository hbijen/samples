
package com.example.csvtransform;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * The module containing all dependencies required by the {@link CsvTransformLambda}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of S3Client
     */
    public static S3Client s3Client() {
        return S3Client.builder()
                       .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                       .httpClientBuilder(UrlConnectionHttpClient.builder())
                       .build();
    }
}
