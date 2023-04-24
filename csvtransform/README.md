# Getting Started

A lambda function in java using AWS SAM.


## Pre-requisite
Install following tools
- [Docker][1]
- [aws cli][2]
- [sam][3]

## Setting up environment using SAM
After required tools are installed and aws cli configured, following commands. 
Re-run both `sam build` and `sam deploy` for any changes in the code or `template.yaml` file
The `--guided` option will save selected options in the `samconfig.toml` file
```sh
# perform a build and create a docker image locally
sam build

# perform a guided setup and reinitialize 
# replace 'main' with the configured profile name
sam deploy --guided --config-env dev --profile main

# if `samconfig.toml` already exists then the option `--guided` can be removed
sam deploy --config-env dev --profile main
```

## configure bucket name
`template.yaml` contains following environment variables 
- BUCKET_NAME, the name of the s3 bucket
- IN_PREFIX, the folder name within the bucket that contains the input csv files
- OUT_PREFIX, the folder name within the bucket that contains the transformed csv files, one  corresponding to each input file

## test the lambda function from command line
```sh
# see logs of the lambda function
aws lambda invoke --profile main --function-name csv-transform out --log-type Tail \
--query 'LogResult' --output text |  base64 -d
```

## Run from command line locally

```sh
# create the jar
./mvnw package

# To get command line help 
java -jar ./target/csvtransform-0.0.1-SNAPSHOT.jar 

# <input directory> contains the source csv file and result will be written to the <output directory>
java -jar ./target/csvtransform-0.0.1-SNAPSHOT.jar -i <input directory> -o <output directory>
```


[1]: <https://docs.docker.com/desktop/>

[2]: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/prerequisites.html

[3]: <https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html>