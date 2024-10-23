package top.yogiczy.mytv.core.data.entities.channel

import kotlinx.serialization.Serializable

/**
 * 频道线路
 */
@Serializable
data class ChannelLine(
    val url: String = "",
    val httpUserAgent: String? = null,
    val hybridType: HybridType = HybridType.None,
    val name: String? = if (url.contains("$")) url.split("$").lastOrNull() else null,
) {

    val playableUrl: String
        get() = url.substringBefore("$")

    companion object {
        val EXAMPLE =
            ChannelLine(
                url = "http://1.2.3.4\$LR•IPV6『线路1』",
                httpUserAgent = "okhttp",
            )
    }

    enum class HybridType {
        None,
        WebView,
    }
}