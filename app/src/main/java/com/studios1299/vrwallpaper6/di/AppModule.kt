package com.studios1299.vrwallpaper6.di

import android.content.Context
import com.studios1299.vrwallpaper6.feature.chat.domain.ChatRepository
import com.studios1299.vrwallpaper6.feature.chat.data.FakeChatRepository
//import com.google.firebase.auth.FirebaseAuth


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
//    val firebaseAuth: FirebaseAuth
//    val firebaseFirestore: FirebaseAuth
//    val authRepository: AuthRepository
    val chatRepository: ChatRepository
//    val friendsRepository: AuthRepository
//    val galleryRepository: AuthRepository
}

class AppModuleImpl(
    private val appContext: Context
): AppModule {
//    override val authApi: AuthApi by lazy {
//        Retrofit.Builder()
//            .baseUrl("https://my-url.com")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create()
//    }
    override val chatRepository: ChatRepository by lazy {
        FakeChatRepository()
    }
}

class TestAppModuleImpl: AppModule {
    override val chatRepository: ChatRepository by lazy {
        TODO("Not yet implemented")
    }
}