package com.khue.ktorsslclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.khue.ktorsslclient.ui.theme.KtorSslClientTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.conscrypt.Conscrypt
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Serializable
data class Fruit(
    val id: String,
    val name: String,
)

@Serializable
data class Data(
    val id: String,
    val items: List<Fruit>
)

@Serializable
data class ResponseData(
    val data: Data
)

val crt = "-----BEGIN CERTIFICATE-----\n" +
        "MIID2TCCAsGgAwIBAgIUY34cAQzIs2ZFtMC9H8xkHkypw40wDQYJKoZIhvcNAQEL\n" +
        "BQAwfDELMAkGA1UEBhMCVk4xDDAKBgNVBAgMA0hDTTEMMAoGA1UEBwwDSENNMQsw\n" +
        "CQYDVQQKDAJTUzELMAkGA1UECwwCc2ExDzANBgNVBAMMBmVwYXBlcjEmMCQGCSqG\n" +
        "SIb3DQEJARYXMDkwMzIwMDFraHVuZ0BnbWFpbC5jb20wHhcNMjQwNDE1MTczNTIw\n" +
        "WhcNMjkwNDE0MTczNTIwWjB8MQswCQYDVQQGEwJWTjEMMAoGA1UECAwDSENNMQww\n" +
        "CgYDVQQHDANIQ00xCzAJBgNVBAoMAlNTMQswCQYDVQQLDAJzYTEPMA0GA1UEAwwG\n" +
        "ZXBhcGVyMSYwJAYJKoZIhvcNAQkBFhcwOTAzMjAwMWtodW5nQGdtYWlsLmNvbTCC\n" +
        "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJbVni9kt04YUxtUO1u+cbbh\n" +
        "/mYwbA9vVnHZRqp8bUCiTHR8OkVHo/ShiagUt2XfGyUVLUtCwoFdIoMRb5PBxY1X\n" +
        "1zshZI5zKWU6CrgF4fb2xWjvm0tqLcrviffMT51vnohUGuUO6rMsZHPbToXpOY3R\n" +
        "Hwh/7Z+NeeRPq8J6tMQM3Yq1y1SyyBlTogj6EWXPuud8M8BcvSNYQH26lQTsOJxo\n" +
        "4unISol1UOumFEylAUXZWPt8PYvyOXEcK3fQfsA8HHI/wmxB5EieEi8s2/fAAuam\n" +
        "6vuLERZcGeakcsfkwOkqFeGxzzyaC0Xt/NDxuTAyLMMNTr9R4dbvebcuKB9Ud+0C\n" +
        "AwEAAaNTMFEwHQYDVR0OBBYEFCryj4bm1Y3/8/CblVEls+mbHJXcMB8GA1UdIwQY\n" +
        "MBaAFCryj4bm1Y3/8/CblVEls+mbHJXcMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZI\n" +
        "hvcNAQELBQADggEBAF4b+1PwU42fWBFo+ACnWVd0HQq7V92kX44LCKdjaZM71XIt\n" +
        "vcptTg1aEolyVSH2j4Ni+3uxiOzXVxY2c75o0C1w/N5Y3jRBzqnMTiw6l7AUzbdw\n" +
        "eM6veos4UCw5l1ALhquuLzCT7acGYD0iWk6QRQpn5cGuTQlAAFNNMGzm2l3pxvCW\n" +
        "UWSM2uE665fhqDQET0lLs/FGmGK3V9DSIWo7LB/cr4cp0tFFyOk6Qm3yu+bm4jLF\n" +
        "kIJG9rhcxvl28Y20ZPZadsWbKLlVv413su8ThmnekM8yXDPxp8Eh504r7miamGf7\n" +
        "dF0faRatl0DlAITKv0NMjVe5/QRKnZXLB8MpLmU=\n" +
        "-----END CERTIFICATE-----"


class MainActivity : ComponentActivity() {

    fun getKeyStore(): KeyStore {
//        val keyStoreFile = FileInputStream(File(applicationContext.filesDir, "keystore.jks"))
//        val keyStorePassword = "123456".toCharArray()
//        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//        keyStore.load(keyStoreFile, keyStorePassword)
        val certificateFactory = CertificateFactory.getInstance("X509")
        val epaperCA = certificateFactory.generateCertificate(ByteArrayInputStream(crt.toByteArray()))
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("khue", epaperCA)
        return keyStore
    }

    fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    fun getSslContext(): SSLContext? {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, getTrustManagerFactory()?.trustManagers, null)
        return sslContext
    }

    fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }

    suspend fun getFruitFromApi(): ResponseData {
        val client = HttpClient(CIO) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            install(ContentNegotiation) {
                json(json)
            }
            engine {
//                sslManager = { httpsURLConnection ->
//                    httpsURLConnection.sslSocketFactory = getSslContext()?.socketFactory
//                    httpsURLConnection.hostnameVerifier = HostnameVerifier { hostname, session ->  true}
//                }
                https {
                    trustManager = getTrustManager()
                }
            }
        }

        return client.get("https://10.1.141.187:8002/fruits").body()
    }

    companion object {
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
        CoroutineScope(Dispatchers.IO).launch {
            findFruit()
        }
        setContent {
            KtorSslClientTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
    }

    private suspend fun findFruit() {
        val fruits = getFruitFromApi()
        Log.d("KtorSslClient", fruits.toString())
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KtorSslClientTheme {
        Greeting("Android")
    }
}