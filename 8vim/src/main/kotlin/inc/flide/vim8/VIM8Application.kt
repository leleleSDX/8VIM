package inc.flide.vim8

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import inc.flide.vim8.ime.KeyboardTheme
import inc.flide.vim8.ime.YamlLayoutLoader
import inc.flide.vim8.ime.layout.Cache
import inc.flide.vim8.ime.layout.parsers.CborParser
import inc.flide.vim8.ime.layout.parsers.YamlParser
import inc.flide.vim8.lib.android.tryOrNull
import inc.flide.vim8.theme.ThemeMode
import java.lang.ref.WeakReference

private var vim8ApplicationReference = WeakReference<VIM8Application?>(null)

class VIM8Application : Application() {
    private val prefs by appPreferenceModel()

    private val layoutParser = YamlParser()
    val cache = lazy { Cache(CborParser(), this) }
    val layoutLoader = lazy { YamlLayoutLoader(layoutParser, cache.value, this) }

    override fun onCreate() {
        super.onCreate()
        vim8ApplicationReference = WeakReference(this)
        prefs.initialize(this)
        KeyboardTheme.initialize(this)

        when (prefs.theme.mode.get()) {
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }
    }
}

private tailrec fun Context.vim8Application(): VIM8Application {
    return when (this) {
        is VIM8Application -> this
        is ContextWrapper -> when {
            this.baseContext != null -> this.baseContext.vim8Application()
            else -> vim8ApplicationReference.get()!!
        }

        else -> tryOrNull { this.applicationContext as VIM8Application }
            ?: vim8ApplicationReference.get()!!
    }
}

fun Context.cache() = this.vim8Application().cache
fun Context.layoutLoader() = this.vim8Application().layoutLoader
