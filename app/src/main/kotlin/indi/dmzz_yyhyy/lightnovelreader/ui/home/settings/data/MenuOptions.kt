package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data

import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.update.AppCenterParser
import indi.dmzz_yyhyy.lightnovelreader.data.update.GithubParser
import indi.dmzz_yyhyy.lightnovelreader.data.update.UpdateParser

@Suppress("PropertyName", "unused")
sealed class MenuOptions {
    protected val _optionList: MutableList<Option>
    val optionList: List<Option> get() = _optionList.toList()
    constructor(vararg options: Option) {
        _optionList = options.toMutableList()
    }
    constructor(options: List<Option>) {
        _optionList = options.toMutableList()
    }
    fun option(key: String, nameId: Int): String {
        _optionList.add(Option(key, nameId))
        return key
    }

    open class MenuOptionsWithValues<T>: MenuOptions {
        private val optionWithValueList: MutableList<OptionWithValue<T>>
        constructor(vararg options: OptionWithValue<T>) : super(options.toList()) {
            optionWithValueList = options.toMutableList()
        }
        constructor(options: List<OptionWithValue<T>>): super(options) {
            optionWithValueList = options.toMutableList()
        }
        fun option(key: String, nameId: Int, value: T): String {
            _optionList.add(Option(key, nameId))
            optionWithValueList.add(OptionWithValue(key, nameId, value))
            return key
        }
        fun getOptionWithValue(key: String): OptionWithValue<T> = optionWithValueList.first { it.equals(key) }
    }

    open class Option(
        open val key: String,
        open val nameId: Int
    ) {
        override fun equals(other: Any?): Boolean = this.key == other
        override fun hashCode(): Int = key.hashCode()
    }

    fun get(key: String): Option = optionList.first { it.equals(key) }

    class OptionWithValue<T>(
        override val key: String,
        override val nameId: Int,
        val value: T
    ): Option(key, nameId)

    open class UpdateChannelOptions(vararg options: OptionWithValue<UpdateParser>): MenuOptionsWithValues<UpdateParser>(options.toList()) {
        companion object {
            const val Release = "Release"
            const val Development = "Development"
        }
    }

    data object GitHubUpdateChannelOptions: UpdateChannelOptions(
        OptionWithValue(Release, R.string.key_update_channel_release, GithubParser.ReleaseParser),
        OptionWithValue(Development, R.string.key_update_channel_development, GithubParser.DevelopmentParser),
        OptionWithValue("CI", R.string.key_update_channel_ci, GithubParser.CIParser)
    )

    data object AppCenterUpdateChannelOptions: UpdateChannelOptions(
        OptionWithValue(Release, R.string.key_update_channel_release, AppCenterParser.ReleaseParser),
        OptionWithValue(Development, R.string.key_update_channel_development, AppCenterParser.DevelopmentParser),
    )

    data object UpdatePlatformOptions: MenuOptionsWithValues<UpdateChannelOptions>() {
        val GitHub = option("GitHub", R.string.key_platform_github, GitHubUpdateChannelOptions)
        val AppCenter = option("AppCenter", R.string.key_platform_appcenter, AppCenterUpdateChannelOptions)
    }

    data object DarkModeOptions: MenuOptions(
        Option("FollowSystem", R.string.key_dark_mode_follow_system),
        Option("Enabled", R.string.key_dark_mode_enabled),
        Option("Disabled", R.string.key_dark_mode_disabled)
    )

    data object AppLocaleOptions: MenuOptions(
        Option("zh-CN", R.string.key_locale_zh_cn),
        Option("zh-HK", R.string.key_locale_zh_hk),
        Option("zh-TW", R.string.key_locale_zh_tw),
        Option("ja-JP", R.string.key_locale_ja_jp),
        Option("ko-kr", R.string.key_locale_ko_kr),
        Option("ko-kp", R.string.key_locale_ko_kp)
    )

    data object ReaderBgImageDisplayModeOptions: MenuOptions() {
        val Fixed = option("fixed", R.string.key_bg_image_display_mode_fixed)
        val Loop = option("loop", R.string.key_bg_image_display_mode_loop)
    }

    data object FlipAnimationOptions: MenuOptions() {
        val None = option("none", R.string.key_flip_animation_none)
        val ScrollWithoutShadow = option("scroll", R.string.key_flip_animation_scroll)
    }

    data object SelectImage: MenuOptions() {
        val Default = option("default", R.string.key_default_image)
        val Customize = option("customize", R.string.key_customize_image)
    }

    data object SelectText: MenuOptions() {
        val Default = option("default", R.string.key_default_text)
        val Customize = option("customize", R.string.key_customize_text)
    }

    data object MLKitLangOptions: MenuOptions() {
/*
        val Chinese = option("zh", R.string.key_ml_kit_lang_zh)
*/
        val English = option("en", R.string.key_ml_kit_lang_en)
        val German = option("de", R.string.key_ml_kit_lang_de)
        val Spanish = option("es", R.string.key_ml_kit_lang_es)
        val French = option("fr", R.string.key_ml_kit_lang_fr)
        val Italian = option("it", R.string.key_ml_kit_lang_it)
        val Japanese = option("ja", R.string.key_ml_kit_lang_ja)
        val Korean = option("ko", R.string.key_ml_kit_lang_ko)
        val Norwegian = option("no", R.string.key_ml_kit_lang_no)
        val Polish = option("pl", R.string.key_ml_kit_lang_pl)
        val Portuguese = option("pt", R.string.key_ml_kit_lang_pt)
        val Russian = option("ru", R.string.key_ml_kit_lang_ru)
    }
}