package dev.vku.livesnap.ui.screen.navigation

interface NavigationDestination {
    val route: String
}

object HomeDestination : NavigationDestination {
    override val route = "home"
}

object ProfileDestination : NavigationDestination {
    override val route = "profile"
}

object ChatListDestination : NavigationDestination {
    override val route = "chat_list"
}

object ChatDestination : NavigationDestination {
    override val route = "chat/{chatId}"
    fun createNavigationRoute(chatId: String) = "chat/$chatId"
}