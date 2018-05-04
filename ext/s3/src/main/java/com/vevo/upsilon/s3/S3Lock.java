package com.vevo.upsilon.s3;

import com.vevo.upsilon.lock.Lock;
import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.sync.RequestBody;
import software.amazon.awssdk.utils.IoUtils;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class S3Lock implements Lock {

    private static final String LOCKED_FILE_CONTENTS = "locked";

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final S3Client client;
    private final String key;
    private final String bucket;

    public S3Lock(S3Client client, String bucket, String key) {
        this.client = client;
        this.key = key;
        this.bucket = bucket;
    }

    public S3Lock(AwsCredentialsProvider credentialsProvider, String bucket, String key) {
        this(S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .build(), bucket, key);
    }

    @Override
    public boolean tryLock() {
        lock.readLock().lock();

        try {
            GetObjectRequest gor = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return client.getObject(gor, (response, inputStream) -> {
                String lockContent = IoUtils.toString(inputStream);

                //only if the file is present and has content "locked" will we return false
                //we're negating here because if it is indeed locked, we arent the ones that locked it so we're gonna fail locking
                return !lockContent.equals(LOCKED_FILE_CONTENTS);
            });
        } catch (S3Exception e) {
            //throw an actual exception only if its not a 404 for a missing lock
            if (e.getStatusCode() != 404) {
                throw e;
            }
        } finally {
            lock.readLock().unlock();
        }

        //if we're here, it means that the lock was not found

        //if another thread has the lock return nope
        if (!lock.writeLock().tryLock()) {
            return false;
        }

        //if there is no lock file, write one and return true!
        try {
            PutObjectRequest por = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            client.putObject(por, RequestBody.of(LOCKED_FILE_CONTENTS));

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unlock() {

        lock.writeLock().lock();

        try {
            DeleteObjectRequest dor = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            client.deleteObject(dor);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
