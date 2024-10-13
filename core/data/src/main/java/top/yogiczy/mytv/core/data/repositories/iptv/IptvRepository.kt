package top.yogiczy.mytv.core.data.repositories.iptv

import kotlinx.serialization.encodeToString
import top.yogiczy.mytv.core.data.entities.channel.ChannelGroupList
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSource
import top.yogiczy.mytv.core.data.network.HttpException
import top.yogiczy.mytv.core.data.network.request
import top.yogiczy.mytv.core.data.repositories.FileCacheRepository
import top.yogiczy.mytv.core.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.core.data.utils.Globals
import top.yogiczy.mytv.core.data.utils.Logger
import kotlin.time.measureTimedValue

/**
 * 直播源数据获取
 */
class IptvRepository(
    private val source: IptvSource,
) : FileCacheRepository("iptv-${source.url.hashCode().toUInt().toString(16)}.json") {
    private val log = Logger.create("IptvRepository")
    private val rawRepository = IptvRawRepository(source)

    /**
     * 获取直播源分组列表
     */
    suspend fun getChannelGroupList(cacheTime: Long): ChannelGroupList {
        try {
            val json = getOrRefresh({ lastModified, _ ->
                val timeout =
                    System.currentTimeMillis() - lastModified >= (if (source.isLocal) Long.MAX_VALUE else cacheTime)
                val rawChanged = lastModified < rawRepository.lastModified()

                timeout || rawChanged
            }) {
                val raw = rawRepository.getRaw()
                val parser = IptvParser.instances.first { it.isSupport(source.url, raw) }

                log.d("开始解析直播源（${source.name}）...")
                measureTimedValue { parser.parse(raw) }.let {
                    log.i("解析直播源（${source.name}）完成", null, it.duration)
                    Globals.json.encodeToString(it.value)
                }
            }

            return Globals.json.decodeFromString<ChannelGroupList>(json).also { groupList ->
                log.i(
                    listOf(
                        "加载直播源（${source.name}）：${groupList.size}个分组",
                        "${groupList.sumOf { it.channelList.size }}个频道",
                    ).joinToString("，")
                )
            }
        } catch (ex: Exception) {
            log.e("获取直播源失败", ex)
            throw ex
        }
    }

    suspend fun getEpgUrl(): String? {
        return runCatching {
            val sourceData = getOrRefresh(Long.MAX_VALUE) { "" }
            val parser = IptvParser.instances.first { it.isSupport(source.url, sourceData) }
            parser.getEpgUrl(sourceData)
        }.getOrNull()
    }

    override suspend fun clearCache() {
        rawRepository.clearCache()
        super.clearCache()
    }
}

private class IptvRawRepository(
    private val source: IptvSource,
) : FileCacheRepository(
    if (source.isLocal) source.url
    else "iptv-${source.url.hashCode().toUInt().toString(16)}.txt",
    source.isLocal,
) {
    private val log = Logger.create("IptvRawRepository")

    suspend fun getRaw(): String {
        return getOrRefresh(if (source.isLocal) Long.MAX_VALUE else 0) {
            log.d("获取直播源: $source")

            try {
                source.url.request { body -> body.string() } ?: ""
            } catch (ex: Exception) {
                log.e("获取直播源失败", ex)
                throw HttpException("获取直播源失败，请检查网络连接", ex)
            }
        }
    }

    override suspend fun clearCache() {
        if (source.isLocal) return
        super.clearCache()
    }
}
