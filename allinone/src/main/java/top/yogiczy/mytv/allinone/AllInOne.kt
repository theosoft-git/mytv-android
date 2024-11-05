package top.yogiczy.mytv.allinone

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

object AllInOne {
    private const val TAG = "AllInOne"
    private var process: Process? = null

    suspend fun start(
        context: Context,
        allinoneBin: File,
        onFail: () -> Unit = {},
        onUnsupported: () -> Unit = {},
    ) {
        if (process != null) return

        withContext(Dispatchers.IO) {
            runCatching {
                val proot = File(context.applicationInfo.nativeLibraryDir, "libproot.so")
                val loader = File(context.applicationInfo.nativeLibraryDir, "libloader.so")
                val libTermuxExec =
                    File(context.applicationInfo.nativeLibraryDir, "libtermux-exec.so")

                if (!proot.exists()) {
                    onUnsupported()
                    return@runCatching
                }

                val allinone = File(context.filesDir, "allinone").apply {
                    if (allinoneBin.lastModified() > lastModified()) {
                        allinoneBin.copyTo(this, true)
                        setExecutable(true)
                    }
                }

                val prootDir = File(context.filesDir, "proot").apply { mkdirs() }
                val rootfsDir = File(prootDir, "rootfs").apply { mkdirs() }
                val pwdDir = File(prootDir, "pwd").apply { mkdirs() }
                val tmpDir = File(prootDir, "tmp").apply { mkdirs() }

                val etcResolvConf = File(rootfsDir, "etc/resolv.conf").apply {
                    parentFile?.mkdirs()
                    context.assets.open("resolv.conf").reader().readLines().let {
                        val localDnsServerList =
                            getLocalGatewayList().map { gateway -> "nameserver $gateway" }

                        Log.d(TAG, "resolv.conf: ${(localDnsServerList + it).joinToString()}")
                        writeText((localDnsServerList + it).joinToString("\n"))
                    }
                }
                val etcSsl = File(rootfsDir, "etc/ssl").apply {
                    mkdirs()
                    File(this, "certs/ca-bundle.crt").apply {
                        parentFile?.mkdirs()
                        context.assets.open("ca-bundle.crt").copyTo(outputStream())
                    }
                }

                val processBuilder = ProcessBuilder(
                    "sh", "-c",
                    listOf(
                        "export PROOT_LOADER=${loader.absolutePath}",
                        "&& export PROOT_TMP_DIR=${tmpDir.absolutePath}",
                        "&& export LD_PRELOAD=${libTermuxExec.absolutePath}",
                        "&& ${proot.absolutePath}",
                        "-b ${etcResolvConf.absolutePath}:/etc/resolv.conf",
                        "-b ${etcSsl}:/etc/ssl",
                        "-w ${pwdDir.absolutePath}",
                        allinone.absolutePath,
                    ).joinToString(" ")
                )

                processBuilder.redirectErrorStream(true)
                process = processBuilder.start().apply {
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    while (true) {
                        val line = reader.readLine() ?: break
                        Log.d(TAG, line)
                    }
                }

                val ret = process?.waitFor()
                stop()

                if (ret != 0) onFail()
            }.onFailure {
                onFail()
                it.printStackTrace()
            }
        }
    }

    suspend fun stop() {
        withContext(Dispatchers.IO) {
            process?.destroy()
            process = null
        }
    }

    private fun getLocalIpAddressList(): List<String> {
        val localIpList = mutableListOf<String>()

        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        inetAddress.hostAddress?.let { localIpList.add(it) }
                    }
                }
            }

            return localIpList
        } catch (ex: SocketException) {
            return localIpList
        }
    }

    private fun getLocalGatewayList(): List<String> {
        try {
            val localIpList = getLocalIpAddressList()

            return localIpList.map {
                val nnIp = it.split(".").subList(0, 3).joinToString(".")
                "$nnIp.1"
            }
        } catch (ex: SocketException) {
            return emptyList()
        }
    }
}