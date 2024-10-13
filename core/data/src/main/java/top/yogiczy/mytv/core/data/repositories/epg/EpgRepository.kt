package top.yogiczy.mytv.core.data.repositories.epg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import top.yogiczy.mytv.core.data.entities.epg.EpgList
import top.yogiczy.mytv.core.data.entities.epgsource.EpgSource
import top.yogiczy.mytv.core.data.network.HttpException
import top.yogiczy.mytv.core.data.network.request
import top.yogiczy.mytv.core.data.repositories.FileCacheRepository
import top.yogiczy.mytv.core.data.repositories.epg.fetcher.EpgFetcher
import top.yogiczy.mytv.core.data.utils.Globals
import top.yogiczy.mytv.core.data.utils.Logger
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.measureTimedValue

/**
 * 节目单获取
 */
class EpgRepository(
    private val source: EpgSource,
) : FileCacheRepository("epg-${source.url.hashCode().toUInt().toString(16)}.json") {
    private val log = Logger.create("EpgRepository")
    private val epgXmlRepository = EpgXmlRepository(source.url)

    /**
     * 获取节目单列表
     */
    suspend fun getEpgList(): EpgList = withContext(Dispatchers.Default) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val xmlJson = getOrRefresh({ lastModified, _ ->
                dateFormat.format(System.currentTimeMillis()) != dateFormat.format(lastModified)
            }) {
                val xml = epgXmlRepository.getXml()
                val epgList = EpgParser.fromXml(xml)
                if (epgList.isEmpty()) throw Exception("获取节目单为空")

                Globals.json.encodeToString(epgList)
            }

            return@withContext Globals.json.decodeFromString<EpgList>(xmlJson).also { epgList ->
                log.i("加载节目单（${source.name}）：${epgList.size}个频道，${epgList.sumOf { it.programmeList.size }}个节目")
            }
        } catch (ex: Exception) {
            log.e("获取节目单失败", ex)
            throw ex
        }
    }

    override suspend fun clearCache() {
        epgXmlRepository.clearCache()
        super.clearCache()
    }
}

/**
 * 节目单xml获取
 */
private class EpgXmlRepository(
    private val url: String
) : FileCacheRepository("epg-${url.hashCode().toUInt().toString(16)}.xml") {
    private val log = Logger.create("EpgXmlRepository")

    suspend fun getXml(): InputStream {
        return getOrRefreshInputStream(0) {
            log.i("开始获取节目单xml: $url")

            try {
                val t = measureTimedValue {
                    url.request { response, request ->
                        val fetcher =
                            EpgFetcher.instances.first { it.isSupport(request.url.toString()) }
                        fetcher.fetch(response.body!!)
                    }
                }
                log.i("获取节目单xml成功", null, t.duration)

                t.value
            } catch (ex: Exception) {
                log.e("获取节目单xml失败", ex)
                throw HttpException("获取节目单xml失败，请检查网络连接", ex)
            }
        }
    }
}
