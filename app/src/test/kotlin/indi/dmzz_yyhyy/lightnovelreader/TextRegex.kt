package indi.dmzz_yyhyy.lightnovelreader

fun main() {
    println("hello world (i am sb) (i am clever people)".replaceTextWithRegex(Regex("\\((.*?)\\)"), "[$1]"))
}

fun String.replaceTextWithRegex(regex: Regex, replaced: String): String {
    var result = this
    for (matchResult in regex.findAll(this)) {
        var progressedReplacedText = replaced
        matchResult.groups.forEachIndexed { index, matchGroup ->
            matchGroup?.value?.let {
                progressedReplacedText = progressedReplacedText.replace("$$index", it)
            }
        }
        result = result.replace(matchResult.value, progressedReplacedText)
    }
    return result
}