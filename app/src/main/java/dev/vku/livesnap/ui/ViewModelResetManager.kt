package dev.vku.livesnap.ui

import dev.vku.livesnap.ui.screen.auth.login.LoginViewModel
import dev.vku.livesnap.ui.screen.auth.register.RegistrationViewModel
import dev.vku.livesnap.ui.screen.chat.ChatListViewModel
import dev.vku.livesnap.ui.screen.chat.ChatViewModel
import dev.vku.livesnap.ui.screen.home.CaptureViewModel
import dev.vku.livesnap.ui.screen.home.FeedViewModel
import dev.vku.livesnap.ui.screen.home.FriendModalViewModel
import dev.vku.livesnap.ui.screen.home.UploadSnapViewModel
import dev.vku.livesnap.ui.screen.profile.UserProfileViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewModelResetManager @Inject constructor() {
    fun resetAllViewModels(
        loginViewModel: LoginViewModel? = null,
        registrationViewModel: RegistrationViewModel? = null,
        chatListViewModel: ChatListViewModel? = null,
        chatViewModel: ChatViewModel? = null,
        captureViewModel: CaptureViewModel? = null,
        feedViewModel: FeedViewModel? = null,
        friendModalViewModel: FriendModalViewModel? = null,
        uploadSnapViewModel: UploadSnapViewModel? = null,
        userProfileViewModel: UserProfileViewModel? = null
    ) {
        // Reset auth related ViewModels
        loginViewModel?.resetState()
        
        registrationViewModel?.resetState()

        // Reset capture related ViewModels
        captureViewModel?.resetState()

        // Reset chat related ViewModels
        chatListViewModel?.resetState()
        chatViewModel?.resetState()

        // Reset home related ViewModels
        feedViewModel?.resetState()
        friendModalViewModel?.resetState()
        uploadSnapViewModel?.resetState()

        // Reset profile ViewModel
        userProfileViewModel?.resetState()
    }
} 