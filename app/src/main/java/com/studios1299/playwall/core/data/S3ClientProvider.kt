//package com.studios1299.playwall.core.data
//
////import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
////import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
////import software.amazon.awssdk.regions.Region
////import software.amazon.awssdk.services.s3.S3Client
//
//object S3ClientProvider {
//
//    private val awsCredentials = AwsBasicCredentials.create(
//        , // Access Key
//         // Secret Access Key
//    )
//
//    val s3Client: S3Client = S3Client.builder()
//        .region(Region.EU_NORTH_1)
//        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//        .build()
//}
