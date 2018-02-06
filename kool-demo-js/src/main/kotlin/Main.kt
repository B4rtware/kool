import de.fabmax.kool.createContext
import de.fabmax.kool.demo.Demo
import kotlin.browser.window

/**
 * @author fabmax
 */
fun main() {
    Demo(createContext(), getParams()["demo"])
//    val ctx = createContext()
//    ctx.scenes += simpleShapesScene()
//    ctx.run()
}

fun getParams(): Map<String, String> {
    val params: MutableMap<String, String> = mutableMapOf()
    if (window.location.search.length > 1) {
        val vars = window.location.search.substring(1).split("&")
        for (pair in vars) {
            val keyVal = pair.split("=")
            if (keyVal.size == 2) {
                val keyEnc = keyVal[0]
                val valEnc = keyVal[1]
                val key = js("decodeURIComponent(keyEnc)").toString()
                val value = js("decodeURIComponent(valEnc)").toString()
                params[key] = value
            }
        }
    }
    return params
}