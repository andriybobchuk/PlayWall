package com.studios1299.playwall.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Creates a custom `ViewModelProvider.Factory` for initializing ViewModels with dependencies.
 *
 * Usage:
 * ```
 * MessengerScreen(
 *       viewModel = viewModel<ChatViewModel>(
 *           factory = viewModelFactory {
 *               ChatViewModel(MyApp.appModule.chatRepository)
 *           }
 *       ),
 *       onBackClick = {},
 *       modifier = Modifier.padding(innerPadding)
 * )
 * ```
 *
 * @param initializer A lambda function that returns an instance of the ViewModel.
 * @return A ViewModelProvider.Factory instance.
 */
fun <VM: ViewModel> viewModelFactory(initializer: () -> VM): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return initializer() as T
        }
    }
}