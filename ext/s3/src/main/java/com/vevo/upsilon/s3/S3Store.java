package com.vevo.upsilon.s3;

import com.vevo.upsilon.store.Store;
import com.vevo.upsilon.store.Version;
import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.sync.RequestBody;
import software.amazon.awssdk.utils.IoUtils;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class S3Store implements Store {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final S3Client client;
    private final String key;
    private final String bucket;

    public S3Store(S3Client client, String bucket, String key) {
        this.client = client;
        this.key = key;
        this.bucket = bucket;
    }

    public S3Store(AwsCredentialsProvider credentialsProvider, String bucket, String key) {
        this(S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .build(), bucket, key);
    }

    @Override
    public Optional<Version> getVersion() {
        lock.readLock().lock();
        try {
            GetObjectRequest gor = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            Version version = client.getObject(gor, (response, inputStream) -> Version.from(IoUtils.toString(inputStream)));

            return Optional.ofNullable(version);
        } catch (NoSuchKeyException e) {
            //if no version file, indicate it hasnt been set
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setVersion(Version version) {
        lock.writeLock().lock();

        try {
            PutObjectRequest por = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            client.putObject(por, RequestBody.of(version.getId()));
        } finally {
            lock.writeLock().unlock();
        }
    }
}
