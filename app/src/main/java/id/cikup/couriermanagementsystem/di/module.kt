package id.cikup.couriermanagementsystem.di

import id.cikup.couriermanagementsystem.data.service.MapsService
import id.cikup.couriermanagementsystem.ui.dashboard.DashboardVM
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }
    single {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit.create(MapsService::class.java)
    }
}

val appModule = module {

}

val dataModule = module {

}

val viewModelModule = module {
    viewModel {
        DashboardVM(get())
    }
}

val myAppModule = listOf(networkModule, appModule, dataModule, viewModelModule)