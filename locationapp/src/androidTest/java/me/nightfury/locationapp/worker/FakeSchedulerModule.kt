//package me.nightfury.locationapp.worker
//
//import android.content.Context
//import android.net.ConnectivityManager
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import dagger.hilt.testing.TestInstallIn
//import io.mockk.mockk
//import me.nightfury.locationapp.di.ServiceModule
//import me.nightfury.locationdomain.ServiceScheduler
//import javax.inject.Singleton
//
//@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [ServiceModule::class]
//)
//object FakeSchedulerModule {
//
//    @Provides
//    @Singleton
//    fun provideFakeServiceScheduler(): ServiceScheduler = mockk(relaxed = true)
//
//
//    @Provides
//    @Singleton
//    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
//        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    }
//}