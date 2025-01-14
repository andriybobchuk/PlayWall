package com.studios1299.playwall.core.data.s3

import android.util.Log
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import com.studios1299.playwall.app.Credentials
import java.io.File
import java.net.URI
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


object S3Handler {
    private const val BUCKET_NAME = "playwall-dev"
    private const val REGION = "eu-north-1"
    private const val LOG_TAG = "S3Handler"

    enum class Folder(val path: String) {
        AVATARS("avatars/"),
        WALLPAPERS("wallpapers/");

        override fun toString(): String {
            return path
        }
    }

//    private val s3Client = S3Client {
//        region = REGION
//        credentialsProvider = StaticCredentialsProvider(
//            aws.smithy.kotlin.runtime.auth.awscredentials.Credentials(
//                Credentials.accessKey,
//                Credentials.secretKey
//            )
//        )
//    }

//    @Deprecated(
//        message = "Due to safety concerns, access to AWS key has only backend now",
//        replaceWith = ReplaceWith("uploadWallpaper"),
//        level = DeprecationLevel.ERROR
//    )
//    suspend fun uploadToS3(file: File, folder: Folder): String? {
//        return try {
//            val filename = "$folder${UUID.randomUUID()}.jpg"
//            Log.d(LOG_TAG, "Starting upload to S3 with key: $filename")
//
//            val request = PutObjectRequest {
//                bucket = BUCKET_NAME
//                key = filename
//                body = ByteStream.fromFile(file)
//            }
//
//            s3Client.putObject(request)
//            Log.d(LOG_TAG, "File uploaded successfully with key: $filename")
//
//            filename // Return S3 key (avatarId)
//        } catch (e: Exception) {
//            Log.e(LOG_TAG, "Failed to upload file to S3: ${e.message}", e)
//            null // Return null on failure
//        }
//    }

    /**
     * Accepts path as is saved in database and returns downloadable link to S3
     * @param filename path from database in format "avatars/2a68a3ae-54d3-4890-b902-1a19426a"
     * @return downloadable link to S3 like "https://playwall-dev.s3.eu-north-1.amazonaws.com/avatars/2a68a3ae-54d3-4890-"
     */
//    @Deprecated(
//        message = "Due to safety concerns, access to AWS key has only backend now",
//        replaceWith = ReplaceWith("getPresignedUrl"),
//        level = DeprecationLevel.ERROR
//    )
//    suspend fun pathToDownloadableLink(filename: String): String? {
//        val getObjectRequest = GetObjectRequest {
//            bucket = BUCKET_NAME
//            key = filename
//        }
//        return try {
//            val presignedRequest = s3Client.presignGetObject(getObjectRequest, 12.hours)
//            val presignedUrl = presignedRequest.url.toString()
//            Log.d("com.studios1299.playwall.core.data.s3.S3Handler", "Generated presigned URL: $presignedUrl")
//            presignedUrl
//        } catch (e: Exception) {
//            Log.e("S3Handler", "Failed to generate presigned URL: ${e.message}", e)
//            null
//        }
//    }

    /**
     * Accepts a downloadable link to S3 and returns the path to store in the database.
     * @param presignedUrl Downloadable link to S3 in format "https://playwall-dev.s3.eu-north-1.amazonaws.com/avatars/2a68a3ae-54d3-4890-b902-1a19426a"
     * @return path to store in database in format "avatars/2a68a3ae-54d3-4890-b902-1a19426a"
     */
    fun downloadableLinkToPath(presignedUrl: String): String? {
        return try {
            val uri = URI(presignedUrl)
            val bucketPath = uri.path

            if (bucketPath.startsWith("/")) {
                bucketPath.substring(1)
            } else {
                bucketPath
            }
        } catch (e: Exception) {
            Log.e("S3Handler", "Failed to extract path from URL: ${e.message}", e)
            null
        }
    }
}

