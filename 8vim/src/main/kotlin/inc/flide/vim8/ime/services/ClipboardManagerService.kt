package inc.flide.vim8.ime.services

import android.content.ClipboardManager
import android.content.Context
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.lib.android.systemServiceOrNull

class ClipboardManagerService(context: Context) {
    private val prefs by appPreferenceModel()
    private var clipboardHistoryListener: ClipboardHistoryListener? = null

    init {
        context.systemServiceOrNull(ClipboardManager::class)?.let { clipboardManager ->
            clipboardManager.addPrimaryClipChangedListener {
                clipboardManager.primaryClip?.let {
                    if (prefs.clipboard.enabled.get()) {
                        val clip = it.getItemAt(0)
                        val newClip = clip.text.toString()
                        addClipToHistory(newClip)
                        clipboardHistoryListener?.onClipboardHistoryChanged()
                    }
                }
            }
        }
    }

    private fun getTimestampFromTimestampedClip(timestampedClip: String): Long {
        val timestampString = timestampedClip.substring(1, timestampedClip.indexOf("] "))
        return timestampString.toLong()
    }

    private fun getClipFromTimestampedClip(timestampedClip: String): String {
        return timestampedClip.substring(timestampedClip.indexOf("] ") + 2)
    }

    private fun addClipToHistory(newClip: String) {
        if (newClip.isNotEmpty()) {
            val timestampedClip = "[${System.currentTimeMillis()}] $newClip"
            (prefs.clipboard.history.get().asSequence() + timestampedClip)
                .fold(mapOf<String, Long>()) { acc, clip ->
                    val cleanedClip = getClipFromTimestampedClip(clip)
                    val timestamp = getTimestampFromTimestampedClip(clip)
                    val toAdd = mapOf(cleanedClip to timestamp)
                    acc + (
                        acc[cleanedClip]?.let {
                            if (timestamp > it) {
                                toAdd
                            } else {
                                emptyMap()
                            }
                        } ?: toAdd
                        )
                }
                .toList()
                .sortedByDescending { it.second }
                .take(MAX_HISTORY_SIZE)
                .map { "[${it.second}] ${it.first}" }
                .toSet()
                .let { prefs.clipboard.history.set(it) }
        }
    }

    val clipHistory: List<String>
        get() = prefs.clipboard.history.get()
            .toList()
            .sortedByDescending { getTimestampFromTimestampedClip(it) }
            .map { getClipFromTimestampedClip(it) }

    fun setClipboardHistoryListener(listener: ClipboardHistoryListener) {
        clipboardHistoryListener = listener
    }

    interface ClipboardHistoryListener {
        fun onClipboardHistoryChanged()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10 // This could be made user-configurable
    }
}
