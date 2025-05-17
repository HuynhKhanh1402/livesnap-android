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
    var loginViewModel: LoginViewModel? = null
    var registrationViewModel: RegistrationViewModel? = null
    var chatListViewModel: ChatListViewModel? = null
    var chatViewModel: ChatViewModel? = null
    var captureViewModel: CaptureViewModel? = null
    var feedViewModel: FeedViewModel? = null
    var friendModalViewModel: FriendModalViewModel? = null
    var uploadSnapViewModel: UploadSnapViewModel? = null
    var userProfileViewModel: UserProfileViewModel? = null

    fun resetAllViewModels() {
        loginViewModel?.resetState()
        registrationViewModel?.resetState()
        captureViewModel?.resetState()
        chatListViewModel?.resetState()
        chatViewModel?.resetState()
        feedViewModel?.resetState()
        friendModalViewModel?.resetState()
        uploadSnapViewModel?.resetState()
        userProfileViewModel?.resetState()
    }
} 