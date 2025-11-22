package io.nightfish.lightnovelreader.api

object ApiMetadata {
    /** 宿主当前支持的 API 版本
     *
     * 开发需要时手动递增 */
    const val API_VERSION: Int = 1
}

object ApiCompat {
    /**
     * 版本分组。按发布维护
     *
     * 需要调整时，修改此列表并随宿主发布
     */
    private val groups: List<Set<Int>> = listOf(
        setOf(1),
    )

    private fun groupOf(v: Int): Int? =
        groups.indexOfFirst { v in it }.takeIf { it >= 0 }

    /**
     * 规则：
     * - 同组且 pluginApi <= hostApi 视为支持
     * - 不同组之间一律视为不兼容
     * - pluginApi > hostApi 视为不兼容
     */
    fun isSupported(pluginApi: Int, hostApi: Int = ApiMetadata.API_VERSION): Boolean {
        val gh = groupOf(hostApi) ?: return false
        val gp = groupOf(pluginApi) ?: return false
        if (gh != gp) return false
        return pluginApi <= hostApi
    }
}
