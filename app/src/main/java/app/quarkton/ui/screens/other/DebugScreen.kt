package app.quarkton.ui.screens.other

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.quarkton.ton.Wallet
import app.quarkton.ton.extensions.toKey
import app.quarkton.ui.screens.BaseScreen
import app.quarkton.ui.theme.Colors
import com.benasher44.uuid.Uuid
import com.juul.kable.Filter
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.juul.kable.peripheral
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.block.TransOrd
import org.ton.lite.client.LiteClient
import org.ton.mnemonic.Mnemonic
import java.net.URL

class DebugScreen : BaseScreen() {

    @Preview
    @Composable
    fun P() {
        Preview()
    }

    @Composable
    override fun Content() {
        Init(dark = true)
        if (!mdl.developmentMode) {
            SideEffect {
               nav?.replaceAll(StartupScreen())
            }
            return
        }
        val json = remember { Json{ ignoreUnknownKeys = true } }
        val text = remember { mutableStateOf("") }
        var btperm by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        Surface(color = Color.Black) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .verticalScroll(scrollState)) {
                OutlinedButton(onClick = {
                    crs?.launch(context = Dispatchers.IO) {
                        text.value = "Loading config"

                        val config = json.decodeFromString<LiteClientConfigGlobal>(
                            URL("https://ton.org/global-config.json").readText()
                        )

                        text.value = "LiteClient init"

                        val liteClient = LiteClient(Dispatchers.IO, config)

                        val lastBlockId = liteClient.getLastBlockId()
                        text.value = "Last block ID: $lastBlockId"

                        text.value = "Loading block..."
                        //val block: Block
                        while (true) {
                            liteClient.getBlock(lastBlockId) ?: continue
                            break
                        }

//                    val p = TlbPrettyPrinter()
//                    block.info.value.print(p)
//                    text.value = p.toString()

                        text.value = "Loading account..."

                        val sf = per.getSeedPhrase()
                        if (sf == null) {
                            text.value = "No seed phrase found"
                            return@launch
                        }

                        val wal = Wallet(Wallet.V4R2, Mnemonic.toKey(sf))
                        wal.update(liteClient)

//                        text.value = "Sending transaction..."
//                        wal.transfer(liteClient, WalletTransfer {
//                            destination = wal.address
//                            coins = Coins(1)
//                            messageData = MessageData.comment("What does Kotlin say?")
//                        })

                        text.value = "Loading transactions..."
                        val txs = liteClient.getTransactions(wal.address, wal.data.value.fullState!!.lastTransactionId!!, 10)

                        text.value = txs.joinToString("\n") {
                            val t = it.transaction.value
                            val r = t.r1.value
                            val d = t.description.value as? TransOrd
                            buildString {
                                append("id.hash: " )
                                appendLine(it.id.hash.toHex())
                                append("id.lt: ")
                                appendLine(it.id.lt)
                                append("prev.hash: " )
                                appendLine(t.prevTransHash.toHex())
                                append("prev.lt: ")
                                appendLine(t.prevTransLt)
                                append("blockId.workchain: ")
                                appendLine(it.blockId.workchain)
                                append("blockId.seqno: ")
                                appendLine(it.blockId.seqno)
                                append("t.accountAddr: ")
                                appendLine(t.accountAddr.toHex())
                                append("t.status: ")
                                appendLine("${t.origStatus.name} -> ${t.endStatus.name}")
                                append("t.now(lt): ")
                                appendLine("${t.now} (${t.lt})")
                                appendLine()
                                appendLine("r1:")
                                appendLine(r.toString())
                                if (d != null) {
                                    appendLine()
                                    appendLine("description:")
                                    appendLine(d.toString())
                                }
                                appendLine()
                                appendLine()
                            }
                        }
                        // text.value = wal.data.value.toString()

                    }
                }) {
                    Text(text = "Try TON-Kotlin")
                }
                OutlinedButton(onClick = {
                    val sf = per.getSeedPhrase()
                    if (sf != null) {
                        val sk = Mnemonic.toKey(sf)
                        val sp = sk.publicKey()
                        val sb = StringBuilder()
                        for (ver in 1..4) {
                            for (rev in 1..3) {
                                try {
                                    val addr = Wallet.getAddress(ver * 0x100 + rev, sp).toString(
                                        userFriendly = true, urlSafe = true, testOnly = false
                                    )
                                    sb.appendLine("$ver.$rev: $addr")
                                }
                                catch (_: Error) {}
                            }
                        }
                        text.value = sb.toString()
                    }
                }) {
                    Text(text = "Account addresses")
                }
                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())
                    { granted -> btperm = granted }

                LaunchedEffect(true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        launcher.launch(Manifest.permission.BLUETOOTH_SCAN)
                        launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else
                        btperm = true
                }

                OutlinedButton(onClick = {
                    if (!btperm) return@OutlinedButton
                    crs?.launch {
                        val lid = Uuid.fromString("13D63400-2C97-0004-0000-4C6564676572")
                        val found = mutableSetOf<String>()
                        val scanner = Scanner {
                            filters = listOf(Filter.Service(lid))
                            logging {
                                engine = SystemLogEngine
                                level = Logging.Level.Warnings
                                format = Logging.Format.Multiline
                            }
                        }
                        text.value = ""
                        scanner.advertisements.collect {
                            if (found.contains(it.address))
                                return@collect
                            found.add(it.address)
                            text.value += "${it.name} ${it.peripheralName} ${it.address} ${it.bondState.name}\n"
                            var per = crs!!.peripheral(it) {

                            }
                            per.connect()
                            per.services?.forEach { ds ->
                                text.value += "${ds.serviceUuid} ${ds.characteristics.map { i -> i.characteristicUuid }.joinToString(" ")}\n"
                            }
                            per.disconnect()
                        }
                    }
                }) {
                    Text(text = "Ledger", color = if (btperm) Colors.Primary else Color.Red)
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = text.value, Modifier.fillMaxWidth(), color = Color.White)
            }
        }
    }

}
