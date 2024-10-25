//package com.studios1299.playwall.core.data.networking.legacy
//
//import com.google.firebase.crashlytics.crashlytics
//import com.google.firebase.crashlytics.ktx.crashlytics
//import com.google.firebase.ktx.Firebase
//import com.studios1299.playwall.core.domain.error_handling.DataError
//import com.studios1299.playwall.core.domain.error_handling.SmartResult
//import io.ktor.client.HttpClient
//import io.ktor.client.call.body
//import io.ktor.client.request.delete
//import io.ktor.client.request.get
//import io.ktor.client.request.parameter
//import io.ktor.client.request.post
//import io.ktor.client.request.setBody
//import io.ktor.client.request.url
//import io.ktor.client.statement.HttpResponse
//import io.ktor.util.network.UnresolvedAddressException
//import kotlinx.coroutines.CancellationException
//import kotlinx.serialization.SerializationException
//
//suspend inline fun <reified Response: Any> HttpClient.get(
//    route: String,
//    queryParameters: Map<String, Any?> = mapOf()
//): SmartResult<Response, DataError.Network> {
//    return safeCall {
//        get {
//            url(constructRoute(route))
//            queryParameters.forEach { (key, value) ->
//                parameter(key, value)
//            }
//        }
//    }
//}
//
//suspend inline fun <reified Request, reified Response: Any> HttpClient.post(
//    route: String,
//    body: Request
//): SmartResult<Response, DataError.Network> {
//    return safeCall {
//        post {
//            url(constructRoute(route))
//            setBody(body)
//        }
//    }
//}
//
//suspend inline fun <reified Response: Any> HttpClient.delete(
//    route: String,
//    queryParameters: Map<String, Any?> = mapOf()
//): SmartResult<Response, DataError.Network> {
//    return safeCall {
//        delete {
//            url(constructRoute(route))
//            queryParameters.forEach { (key, value) ->
//                parameter(key, value)
//            }
//        }
//    }
//}
//
//suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): SmartResult<T, DataError.Network> {
//    val response = try {
//        execute()
//    } catch(e: UnresolvedAddressException) {
////        Record exception in Crashlytics
//        Firebase.crashlytics.recordException(e)
//        e.printStackTrace()
//        return SmartResult.Error(DataError.Network.NO_INTERNET)
//    } catch (e: SerializationException) {
//        e.printStackTrace()
//        return SmartResult.Error(DataError.Network.SERIALIZATION)
//    } catch(e: Exception) {
//        if(e is CancellationException) throw e
//        e.printStackTrace()
//        return SmartResult.Error(DataError.Network.UNKNOWN)
//    }
//
//    return responseToResult(response)
//}
//
//suspend inline fun <reified T> responseToResult(response: HttpResponse): SmartResult<T, DataError.Network> {
//    return when(response.status.value) {
//        in 200..299 -> SmartResult.Success(response.body<T>())
//        401 -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
//        408 -> SmartResult.Error(DataError.Network.REQUEST_TIMEOUT)
//        409 -> SmartResult.Error(DataError.Network.CONFLICT)
//        413 -> SmartResult.Error(DataError.Network.PAYLOAD_TOO_LARGE)
//        429 -> SmartResult.Error(DataError.Network.TOO_MANY_REQUESTS)
//        in 500..599 -> SmartResult.Error(DataError.Network.SERVER_ERROR)
//        else -> SmartResult.Error(DataError.Network.UNKNOWN)
//    }
//}
//
//fun constructRoute(route: String): String {
//    val BASE_URL: String = ""
//    return when {
//        route.contains(BASE_URL) -> route
//        route.startsWith("/") -> BASE_URL + route
//        else -> BASE_URL + "/$route"
//    }
//}