package opt.bitstorage.finance.net

import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import opt.bitstorage.finance.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {

    companion object{
        const val BASE_URL = "https://opt.bitstorage.finance/wallet/"

        private var mInstance: ApiClient? = null
        private var token: String? = null

        fun getInstance(token: String = ""): ApiClient {
            if (mInstance == null) {
                mInstance = ApiClient()
            }
            ApiClient.token = token
            return mInstance!!
        }
    }

    private fun okHttpDispatcher() = Dispatcher().apply {
        // maxRequests = 1
    }

    private fun okHttpClient(): OkHttpClient {
        val httpClientBuilder = OkHttpClient
                .Builder()

        httpClientBuilder
                .writeTimeout(60L, TimeUnit.SECONDS)
                .readTimeout(60L, TimeUnit.SECONDS)
                .dispatcher(okHttpDispatcher())
                //.cookieJar(cookieJar)

                if (token!!.isNotEmpty()) {
                    httpClientBuilder.addInterceptor {
                        val request = it.request().newBuilder()
                                .addHeader("x-access-token", token!!)
                                .build()
                        try {
                            it.proceed(request)
                        } catch (e: java.lang.Exception) {
                            throw e
                        } finally {
                        }
                    }
                            .retryOnConnectionFailure(true)
                }

        if (BuildConfig.DEBUG) {
            httpClientBuilder.addInterceptor(HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY })
        }

        return httpClientBuilder
                .build()
    }

    private fun retrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient())
                .build()
    }

    fun getService(): ApiService{
        return retrofit().create(ApiService::class.java)
    }
}