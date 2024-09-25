import android.util.Log
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import com.studios1299.playwall.core.data.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.time.Duration.Companion.seconds


object S3Handler {
    private const val BUCKET_NAME = "playwall-dev"
    private const val REGION = "eu-north-1"
    private const val FOLDER_NAME = "avatars/"
    private const val LOG_TAG = "S3Handler"

    private val s3Client = S3Client {
        region = REGION
        credentialsProvider = StaticCredentialsProvider(
            aws.smithy.kotlin.runtime.auth.awscredentials.Credentials(
                Credentials.accessKey,
                Credentials.secretKey
            )
        )
    }

    suspend fun uploadToS3(file: File): String? {
        return try {
            val avatarId = "$FOLDER_NAME${UUID.randomUUID()}.jpg"
            Log.d(LOG_TAG, "Starting upload to S3 with key: $avatarId")

            val request = PutObjectRequest {
                bucket = BUCKET_NAME
                key = avatarId
                body = ByteStream.fromFile(file)
            }

            s3Client.putObject(request)
            Log.d(LOG_TAG, "File uploaded successfully with key: $avatarId")

            avatarId // Return S3 key (avatarId)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to upload file to S3: ${e.message}", e)
            null // Return null on failure
        }
    }

    suspend fun loadFromS3(avatarId: String): String? {

        val getObjectRequest = GetObjectRequest {
            bucket = BUCKET_NAME
            key = avatarId
        }

        return try {
            // Presign the GetObjectRequest to create a presigned URL
            val presignedRequest = s3Client.presignGetObject(getObjectRequest, 3600.seconds)

            val presignedUrl = presignedRequest.url.toString()
            Log.d("S3Handler", "Generated presigned URL: $presignedUrl")
            presignedUrl
        } catch (e: Exception) {
            Log.e("S3Handler", "Failed to generate presigned URL: ${e.message}", e)
            null
        }
    }
}

