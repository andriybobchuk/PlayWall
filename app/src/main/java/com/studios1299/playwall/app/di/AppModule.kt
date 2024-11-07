package com.studios1299.playwall.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.studios1299.playwall.auth.data.EmailPatternValidator
import com.studios1299.playwall.auth.data.AuthRepositoryImpl
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.domain.PatternValidator
import com.studios1299.playwall.core.data.FirebaseCoreRepositoryImpl
import com.studios1299.playwall.core.data.local.dao.ChatDao
import com.studios1299.playwall.core.data.local.database.AppDatabase.Companion.getDatabase
import com.studios1299.playwall.core.domain.CoreRepository

/**
 * # Adding Dependencies
 * To add new dependencies to your project, follow these steps:
 *
 * 1. **Define the Dependency:**
 *    - Add a new property to the `AppModule` interface representing the dependency you want to provide.
 *
 *      ```kotlin
 *      interface AppModule {
 *          val chatRepository: ChatRepository
 *          val userRepository: UserRepository // Example of a new dependency
 *      }
 *      ```
 *
 * 2. **Implement the Dependency:**
 *    - Implement the property in the `AppModuleImpl` class, providing the actual instance of the dependency.
 *
 *      ```kotlin
 *      class AppModuleImpl(
 *          private val appContext: Context
 *      ) : AppModule {
 *          override val chatRepository: ChatRepository by lazy {
 *              FakeChatRepository()
 *          }
 *          override val userRepository: UserRepository by lazy {
 *              RealUserRepository(appContext) // Example of an implemented dependency
 *          }
 *      }
 *      ```
 *
 * 3. **Usage in ViewModel:**
 *    - Inject the dependency into your ViewModel by accessing it from `MyApp.appModule`.
 *
 *      ```kotlin
 *      class ChatViewModel(
 *          private val chatRepository: ChatRepository,
 *          private val userRepository: UserRepository // Using the new dependency
 *      ) : ViewModel()
 *      ```
 *
 * 4. **Provide the ViewModel:**
 *    - Create your ViewModel with the necessary dependencies using custom ``viewModelFactory``.
 *
 *      ```kotlin
 *      MessengerScreen(
 *          viewModel = viewModel<ChatViewModel>(
 *              factory = viewModelFactory {
 *                  ChatViewModel(
 *                      MyApp.appModule.chatRepository,
 *                      MyApp.appModule.userRepository // Passing the new dependency
 *                  )
 *              }
 *          ),
 *          onBackClick = {},
 *          modifier = Modifier.padding(innerPadding)
 *      )
 *      ```
 *  ## Important!
 *  I made AppModule an interface to be able to later define TestAppModule and use it for testing!
 */
interface AppModule {
    val coreRepository: CoreRepository
    val authRepository: AuthRepository
    val emailPatternValidator: PatternValidator
    val firebaseAuth: FirebaseAuth
    val crashlytics: FirebaseCrashlytics
    val firebaseMessaging: FirebaseMessaging
    val context: Context
    val chatDao: ChatDao
}

class AppModuleImpl(
    private val appContext: Context
): AppModule {

    override val coreRepository: CoreRepository by lazy {
        FirebaseCoreRepositoryImpl(
            firebaseAuth = firebaseAuth,
            exploreDao = getDatabase(appContext).exploreWallpaperDao(),
            friendsDao = getDatabase(appContext).friendDao(),
            chatDao = chatDao,
            userDao = getDatabase(appContext).userDao()
        )
    }
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            firebaseAuth = firebaseAuth,
            firebaseMessaging = firebaseMessaging,
        )
    }
    override val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    override val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }
    override val firebaseMessaging: FirebaseMessaging by lazy {
        FirebaseMessaging.getInstance()
    }
    override val context: Context
        get() = appContext

    override val chatDao: ChatDao by lazy {
        getDatabase(appContext).chatDao()
    }

    override val emailPatternValidator: PatternValidator by lazy {
        EmailPatternValidator
    }
}