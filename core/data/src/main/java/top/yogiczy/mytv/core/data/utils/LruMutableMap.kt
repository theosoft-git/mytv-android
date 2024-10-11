package top.yogiczy.mytv.core.data.utils

class LruMutableMap<K, V>(
    initialSize: Int,
    private val maxSize: Int = initialSize,
) : LinkedHashMap<K, V>(initialSize, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }
}