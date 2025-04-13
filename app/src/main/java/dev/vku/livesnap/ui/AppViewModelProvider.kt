///*
// * Copyright (C) 2023 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package dev.vku.livesnap.ui
//
//import android.app.Application
//import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
//import androidx.lifecycle.viewmodel.CreationExtras
//import androidx.lifecycle.viewmodel.initializer
//import androidx.lifecycle.viewmodel.viewModelFactory
//import dev.vku.livesnap.LiveSnapApplication
//import dev.vku.livesnap.ui.screen.auth.register.RegistrationViewModel
//
///**
// * Provides Factory to create instance of ViewModel for the entire Inventory app
// */
//object AppViewModelProvider {
//    val Factory = viewModelFactory {
//        // Initializer for ItemEditViewModel
//        initializer {
//            RegistrationViewModel(
//                usersRepository = inventoryApplication().container.usersRepository
//            )
//        }
////        // Initializer for ItemEntryViewModel
////        initializer {
////            ItemEntryViewModel(inventoryApplication().container.itemsRepository)
////        }
////
////        // Initializer for ItemDetailsViewModel
////        initializer {
////            ItemDetailsViewModel(
////                this.createSavedStateHandle()
////            )
////        }
////
////        // Initializer for HomeViewModel
////        initializer {
////            HomeViewModel()
////        }
//    }
//}
//
///**
// * Extension function to queries for [Application] object and returns an instance of
// * [InventoryApplication].
// */
//fun CreationExtras.inventoryApplication(): LiveSnapApplication =
//    (this[AndroidViewModelFactory.APPLICATION_KEY] as LiveSnapApplication)
