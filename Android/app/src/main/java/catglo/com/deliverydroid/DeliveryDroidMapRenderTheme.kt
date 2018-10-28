package catglo.com.deliverydroid

import org.mapsforge.map.rendertheme.XmlRenderTheme
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback
import java.io.InputStream

enum class DeliveryDroidMapRenderTheme  constructor(private val path: String) : XmlRenderTheme {

    DEFAULT("/assets/default.xml"),
    OSMARENDER("/assets/osmarender.xml");

    override fun getMenuCallback(): XmlRenderThemeMenuCallback? { return null }

    override fun getRelativePathPrefix(): String { return "/assets/" }

    override fun getRenderThemeAsStream(): InputStream { return javaClass.getResourceAsStream(this.path) }

    override fun setMenuCallback(menuCallback: XmlRenderThemeMenuCallback) {}
}