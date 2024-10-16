package top.yogiczy.mytv.core.data.repositories.iptv.parser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * m3u直播源解析
 */
class M3uIptvParser : IptvParser {

    override fun isSupport(url: String, data: String): Boolean {
        return data.startsWith("#EXTM3U")
    }

    override suspend fun parse(data: String) =
        withContext(Dispatchers.Default) {
            val lines = data.split("\r\n", "\n")
            val channelList = mutableListOf<IptvParser.ChannelItem>()

            var addChannel: ((String) -> Unit)? = null
            lines.forEach { line ->
                if (line.isBlank()) return@forEach

                if (line.startsWith("#EXTINF")) {
                    val name = line.split(",").last().trim()
                    val epgName =
                        Regex("tvg-name=\"(.*?)\"").find(line)?.groupValues?.get(1)?.trim()
                            ?.ifBlank { name } ?: name
                    val groupNames =
                        Regex("group-title=\"(.+?)\"").find(line)?.groupValues?.get(1)?.split(";")
                            ?.map { it.trim() }
                            ?: listOf("其他")
                    val logo = Regex("tvg-logo=\"(.+?)\"").find(line)?.groupValues?.get(1)?.trim()
                    val httpUserAgent =
                        Regex("http-user-agent=\"(.+?)\"").find(line)?.groupValues?.get(1)?.trim()

                    addChannel = { url ->
                        channelList.addAll(
                            groupNames.map { groupName ->
                                IptvParser.ChannelItem(
                                    name = name,
                                    epgName = epgName,
                                    groupName = groupName,
                                    url = url,
                                    logo = logo,
                                    httpUserAgent = httpUserAgent,
                                )
                            }
                        )
                    }
                } else {
                    addChannel?.invoke(line.trim())
                }
            }

            channelList
        }

    override suspend fun getEpgUrl(data: String): String? {
        val lines = data.split("\r\n", "\n")
        return lines.firstOrNull { it.startsWith("#EXTM3U") }?.let { defLine ->
            Regex("x-tvg-url=\"(.*?)\"").find(defLine)?.groupValues?.get(1)
                ?.split(",")
                ?.firstOrNull()
                ?.trim()
        }
    }
}