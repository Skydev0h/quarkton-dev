package app.quarkton.ton

import app.quarkton.QuarkApplication
import io.ktor.util.encodeBase64
import kotlinx.datetime.Clock
import org.ton.crypto.decodeHex

fun now(): Long = nowms() / 1000
fun nowms(): Long = Clock.System.now().toEpochMilliseconds()

fun supportedExplorers() = mapOf(
    "tonscan" to "TONScan",
    "tonapi"  to "TON Viewer",
    "toncx"   to "TON Scan",
    "whales"  to "Ton Whales",
    "dton"    to "dTon"
    // DONE: Add support for more explorers!!!
)

fun makeExplorerLink(address: String? = null, transaction: String? = null, explorer: String? = null): String {
    var txid = ""
    var txlt = ""
    var txhex = ""
    if (transaction != null) {
        val spl = transaction.split('@', limit = 2)
        txhex = spl[0].lowercase()
        txlt = spl[1]
        // txhex = txid.decodeBase64Bytes().encodeHex().lowercase()
        txid = txhex.decodeHex().encodeBase64()
    }
    return when (explorer ?: QuarkApplication.app.persistence.selectedExplorer) {
        "tonscan" ->
            when {
                transaction != null -> "https://tonscan.org/tx/$txid"
                address     != null -> "https://tonscan.org/address/$address"
                else                -> "https://tonscan.org/"
            }
        "tonapi" ->
            when {
                transaction != null -> "https://tonviewer.com/transaction/$txhex"
                address     != null -> "https://tonviewer.com/$address"
                else                -> "https://tonviewer.com/"
            }
        "toncx" ->
            when {
                transaction != null -> "https://ton.cx/tx/$txlt:$txid:$address"
                address     != null -> "https://ton.cx/address/$address"
                else                -> "https://ton.cx/"
            }
        "whales" ->
            when {
                transaction != null -> "https://tonwhales.com/explorer/address/$address/${txlt}_$txhex"
                address     != null -> "https://tonwhales.com/explorer/address/$address"
                else                -> "https://tonwhales.com/explorer"
            }
        "dton" ->
            when {
                transaction != null -> "https://dton.io/tx/${txhex.uppercase()}"
                address     != null -> "https://dton.io/a/$address"
                else                -> "https://dton.io"
            }
        else -> ""
    }
}