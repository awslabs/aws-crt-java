package com.example.s3;

import java.io.*;
import java.util.concurrent.*;
import com.amazonaws.s3.model.*;
import com.amazonaws.s3.*;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.auth.credentials.*;
import software.amazon.awssdk.crt.io.*;

public class PutGet {
    public static void main(String[] args) {
        final String USAGE = "\n"
                + "Upload file to S3, then download it.\n\n"
                + "Usage:\n"
                + "    PutGet <bucket> <key> <filepath> <region> [throughput] [part-size]\n\n"
                + "Where:\n"
                + "    bucket: S3 bucket name.\n\n" + "    key: S3 key name.\n\n"
                + "    filepath: File to upload. Then downloaded with '.download' suffix.\n\n"
                + "    region: Signing region. ex: us-east-1\n\n"
                + "    throughput: Target throughput in gigabits per second (default 25.0)\n\n"
                + "    part-size: Size in bytes of each multipart upload (default 8MB)\n\n";
        if (args.length < 4) {
            System.out.print(USAGE);
            System.exit(1);
        }

        String bucket = args[0];
        String key = args[1];
        String putFilepath = args[2];
        String getFilepath = putFilepath + ".download";
        String signingRegion = args[3];

        long partSizeBytes = 8 * 1024 * 1024; // 8MB
        if (args.length > 4)
            partSizeBytes = Long.parseLong(args[4]);

        double targetThroughputGbps = 25.0;
        if (args.length > 5)
            targetThroughputGbps = Double.parseDouble(args[5]);

        Log.initLoggingToStderr(Log.LogLevel.Error);

        // try-with-resources to ensure these resources get closed
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(0 /* cpuGroup */, 0 /* numThreads 0=default */);
                HostResolver hostResolver = new HostResolver(eventLoopGroup, 256 /* maxEntries */);
                ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup, hostResolver);

                // default provider searches for AWS credentials in places like
                // ~/.aws/credentials and env vars AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY
                CredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                        .withClientBootstrap(clientBootstrap).build();

                S3NativeClient s3Client = new S3NativeClient(signingRegion, clientBootstrap, credentialsProvider,
                        partSizeBytes, targetThroughputGbps);) {

            System.out.println("Uploading...");
            File putFile = new File(putFilepath);
            PutObjectRequest putRequest = PutObjectRequest.builder().bucket(bucket).key(key)
                    .contentLength(putFile.length()).build();
            PutDataSupplier putDataSupplier = new PutGet.PutDataSupplier(putFile);
            CompletableFuture<PutObjectOutput> putFuture = s3Client.putObject(putRequest, putDataSupplier);
            try {
                putFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Upload complete");

            System.out.println("Downloading...");
            GetObjectRequest getRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
            GetDataConsumer getDataConsumer = new PutGet.GetDataConsumer(getFilepath);
            CompletableFuture<GetObjectOutput> getFuture = s3Client.getObject(getRequest, getDataConsumer);
            try {
                getFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Download complete");
        }
    }

    // Supplies the file body to upload for PUT
    static class PutDataSupplier implements RequestDataSupplier {
        FileInputStream stream;

        public PutDataSupplier(File file) {
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean getRequestBytes(byte[] buffer) {
            int progress = 0;
            while (progress < buffer.length) {
                try {
                    int result = stream.read(buffer, progress, buffer.length - progress);
                    if (result == -1)
                        return true;
                    progress += result;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }

        public void onFinished() {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Receives the downloaded file body from GET
    static class GetDataConsumer implements ResponseDataConsumer {
        FileOutputStream stream;

        public GetDataConsumer(String filepath) {
            try {
                stream = new FileOutputStream(filepath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void onResponseData(byte[] bodyBytesIn) {
            try {
                stream.write(bodyBytesIn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void onFinished() {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
