package app.quarkton.ui.screens.onboarding

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.quarkton.BuildConfig
import app.quarkton.MainActivity
import app.quarkton.R
import app.quarkton.ui.elements.JumboButtons
import app.quarkton.ui.elements.JumboTemplate
import app.quarkton.ui.elements.Overlay
import app.quarkton.ui.elements.TopBar
import app.quarkton.ui.screens.BaseScreen
import app.quarkton.ui.theme.Colors
import com.airbnb.lottie.compose.LottieConstants

class WelcomeScreen : BaseScreen() {

    @Preview
    @Composable
    fun P() {
        Preview()
    }

    @Composable
    override fun Content() {
        Init()
        var overlay by remember { mutableStateOf(false) }

        // [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [ [
        fun createWalletClicked() {
            overlay = true
            mdl.generateSeedPhrase {
                if (it) nav?.push(WalletCreatedScreen())
                else overlay = false
            }
        }

        fun importWalletClicked() {
            nav?.push(ImportWalletScreen())
        }
        // ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ]

        if (BuildConfig.DEBUG) {
            TopBar {
                Box(modifier = Modifier.size(56.dp, 56.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = {
                        per.devMode = !per.devMode
                        val int = Intent(act, MainActivity::class.java)
                        act.startActivity(int)
                        act.finishAffinity()
                    }) {
                        Icon(
                            tint = if (per.devMode) Colors.BalGreen else Colors.Primary,
                            imageVector = Icons.Outlined.Build,
                            contentDescription = "Dev Mode"
                        )
                    }
                }
            }
        }
        JumboTemplate(
            // imageId = R.drawable.ph_main,
            lottieId = R.raw.start,
            titleText = stringResource(R.string.ton_wallet),
            mainText = stringResource(R.string.ton_wallet_allows),
            lottieIterations = LottieConstants.IterateForever
        ) {
            JumboButtons(
                mainText = stringResource(R.string.create_my_wallet),
                mainClicked = ::createWalletClicked,
                secText = stringResource(R.string.import_existing_wallet),
                secClicked = ::importWalletClicked
            )
        }
        Overlay(
            visible = overlay, showProgress = true
        )
    }

}
