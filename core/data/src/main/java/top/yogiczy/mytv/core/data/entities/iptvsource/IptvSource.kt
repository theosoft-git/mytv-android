package top.yogiczy.mytv.core.data.entities.iptvsource

import kotlinx.serialization.Serializable
import top.yogiczy.mytv.core.data.utils.Globals
import java.io.File

/**
 *  直播源
 */
@Serializable
data class IptvSource(
    /**
     * 名称
     */
    val name: String = "",

    /**
     * 链接
     */
    val url: String = "",

    /**
     * 是否本地
     */
    val isLocal: Boolean = false,

    /**
     * 分组名称映射
     */
    val groupNameMap: Map<String, String> = mapOf(),

    /**
     * 分组排序
     */
    val groupSort: List<String> = listOf(),
) {
    fun cacheFileName(ext: String) =
        "${cacheDir.name}/iptv_source_${hashCode().toUInt().toString(16)}.$ext"

    companion object {
        val cacheDir = File(Globals.cacheDir, "iptv_source_cache")

        val EXAMPLE = IptvSource(
            name = "测试直播源1",
            url = "http://1.2.3.4/tv.txt",
        )

        fun IptvSource.needExternalStoragePermission(): Boolean {
            return this.isLocal && !this.url.startsWith(Globals.cacheDir.path)
        }
    }
}