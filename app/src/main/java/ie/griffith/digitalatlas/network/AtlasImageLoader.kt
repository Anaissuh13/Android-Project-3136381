package ie.griffith.digitalatlas.network

import android.content.Context
import coil.ImageLoader
import okhttp3.OkHttpClient

//one shared image loader for the app, set up to send our user agent,
//wikimedia rejects image requests from coil's default one with a 403
object AtlasImageLoader {

    //created the first time it's needed and then reused, so the image cache is shared too
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader =
        instance ?: ImageLoader.Builder(context.applicationContext)
            .okHttpClient {
                OkHttpClient.Builder()
                    //adding the user agent header to every image request
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", HttpHelper.USER_AGENT)
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            //fading the photo in instead of it popping in
            .crossfade(true)
            .build()
            .also { instance = it }
}