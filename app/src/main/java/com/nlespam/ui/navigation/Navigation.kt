package com.nlespam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nlespam.ui.NleSpamViewModel
import com.nlespam.ui.screens.*

object Routes {
    const val DASHBOARD = "dashboard"
    const val FAST_PAIR = "fast_pair"
    const val APPLE = "apple"
    const val SAMSUNG = "samsung"
    const val SWIFT_PAIR = "swift_pair"
    const val LOVESPOUSE = "lovespouse"
    const val MIX_ALL = "mix_all"
    const val IBEACON_FLOOD = "ibeacon_flood"
    const val CHROMECAST_SPAM = "chromecast_spam"
    const val AIRTAG_CLONE = "airtag_clone"

    const val PACKET_LOGGER = "packet_logger"
    const val MIX_SPAM = "mix_spam"
    const val UUID_DATABASE = "uuid_database"
    const val AD_DECODER = "ad_decoder"
    const val BT_FILE_SENDER = "bt_file_sender"
    const val SETTINGS = "settings"
}

@Composable
fun NleSpamNavHost(
    navController: NavHostController,
    viewModel: NleSpamViewModel,
) {
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToFastPair = { navController.navigate(Routes.FAST_PAIR) },
                onNavigateToApple = { navController.navigate(Routes.APPLE) },
                onNavigateToSamsung = { navController.navigate(Routes.SAMSUNG) },
                onNavigateToSwiftPair = { navController.navigate(Routes.SWIFT_PAIR) },
                onNavigateToLovespouse = { navController.navigate(Routes.LOVESPOUSE) },
                onNavigateToMixAll = { navController.navigate(Routes.MIX_ALL) },
                onNavigateToIBeaconFlood = { navController.navigate(Routes.IBEACON_FLOOD) },
                onNavigateToChromecastSpam = { navController.navigate(Routes.CHROMECAST_SPAM) },
                onNavigateToAirTagClone = { navController.navigate(Routes.AIRTAG_CLONE) },
                onNavigateToMixSpam = { navController.navigate(Routes.MIX_SPAM) },
                
                onNavigateToPacketLogger = { navController.navigate(Routes.PACKET_LOGGER) },
                onNavigateToUuidDatabase = { navController.navigate(Routes.UUID_DATABASE) },
                onNavigateToAdDecoder = { navController.navigate(Routes.AD_DECODER) },
                onNavigateToBtFileSender = { navController.navigate(Routes.BT_FILE_SENDER) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.FAST_PAIR) {
            FastPairScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.APPLE) {
            AppleScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.SAMSUNG) {
            SamsungScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.SWIFT_PAIR) {
            SwiftPairScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.LOVESPOUSE) {
            LovespouseScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.MIX_ALL) {
            MixAllScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.IBEACON_FLOOD) {
            IBeaconFloodScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.CHROMECAST_SPAM) {
            ChromecastSpamScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.AIRTAG_CLONE) {
            AirTagCloneScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.PACKET_LOGGER) {
            PacketLoggerScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.MIX_SPAM) {
            MixSpamScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.UUID_DATABASE) {
            UuidDatabaseScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.AD_DECODER) {
            AdvertisementDecoderScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.BT_FILE_SENDER) {
            BluetoothFileSenderScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
