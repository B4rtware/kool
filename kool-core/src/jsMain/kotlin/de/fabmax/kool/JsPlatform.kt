package de.fabmax.kool

import de.fabmax.kool.math.clamp
import de.fabmax.kool.platform.JsContext
import de.fabmax.kool.platform.WebGL2RenderingContext
import de.fabmax.kool.util.logW
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.math.pow
import kotlin.math.round

/**
 * Javascript / WebGL platform implementation
 *
 * @author fabmax
 */

actual fun createDefaultContext(): KoolContext = createContext(JsContext.InitProps())

fun createContext(props: JsContext.InitProps): KoolContext = JsImpl.createContext(props)

actual fun now(): Double = js("performance.now()") as Double

actual fun Double.toString(precision: Int): String {
    val p = precision.clamp(0, 12)
    if (p == 0) {
        return "${round(this).toLong()}"
    }

    val shifted = round(this * 10.0.pow(p)).toLong()
    var str = "$shifted"
    var i = str.length - precision
    while (i < 1) {
        str = "0$str"
        i++
    }
    return str.substring(0 until i) + "." + str.substring(i)
}

actual inline fun <R> lock(lock: Any, block: () -> R): R = block()

internal object JsImpl {
    val dpi: Float
    var ctx: JsContext? = null
    val gl: WebGL2RenderingContext
        get() = ctx?.gl ?: throw KoolException("Platform.createContext() not called")

    init {
        val measure = document.getElementById("dpiMeasure")
        if (measure == null) {
            logW { "dpiMeasure element not found, falling back to 96 dpi" }
            logW { "Add this hidden div to your html:" }
            logW { "<div id=\"dpiMeasure\" style=\"height: 1in; width: 1in; left: 100%; position: fixed; top: 100%;\"></div>" }
            dpi = 96f
        } else {
            dpi = (measure as HTMLDivElement).offsetWidth.toFloat()
        }
    }

    fun createContext(props: JsContext.InitProps): KoolContext {
        if (ctx != null) {
            throw KoolException("Context was already created (multi-context is currently not supported in js")
        }
        ctx = JsContext(props)
        return ctx!!
    }
}
