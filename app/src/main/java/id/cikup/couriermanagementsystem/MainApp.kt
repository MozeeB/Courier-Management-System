package id.cikup.couriermanagementsystem

import androidx.multidex.MultiDexApplication
import com.orhanobut.hawk.Hawk
import id.cikup.couriermanagementsystem.di.myAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApp : MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()

        Hawk.init(this).build()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MainApp)
            modules(myAppModule)
        }
    }


}