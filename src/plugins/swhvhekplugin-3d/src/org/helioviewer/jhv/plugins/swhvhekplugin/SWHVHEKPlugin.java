package org.helioviewer.jhv.plugins.swhvhekplugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.helioviewer.base.math.Interval;
import org.helioviewer.jhv.gui.ImageViewerGui;
import org.helioviewer.jhv.plugins.swhvhekplugin.cache.SWHVHEKData;
import org.helioviewer.jhv.plugins.swhvhekplugin.controller.SWHVHEKImagePanelEventPopupController;
import org.helioviewer.jhv.plugins.swhvhekplugin.settings.SWHVHEKSettings;
import org.helioviewer.viewmodelplugin.controller.PluginManager;
import org.helioviewer.viewmodelplugin.controller.PluginSettings;
import org.helioviewer.viewmodelplugin.interfaces.Plugin;
import org.helioviewer.viewmodelplugin.overlay.OverlayContainer;
import org.helioviewer.viewmodelplugin.overlay.OverlayPlugin;

/**
 * @author Malte Nuhn
 * */
public class SWHVHEKPlugin extends OverlayPlugin implements Plugin {

    private boolean builtin_mode = false;

    /**
     * Reference to the eventPlugin
     */
    private final SWHVHEKPluginContainer eventPlugin;

    /**
     * Default constructor.
     */
    public SWHVHEKPlugin() {
        this(false);
    }

    /**
     * Constructor with debug flag. If debug flag is set, the plugin name shows
     * "HEK Plugin Built-In Version"
     *
     * @param builtin_mode
     *            - debug flag
     */
    public SWHVHEKPlugin(boolean builtin_mode) {
        this.builtin_mode = builtin_mode;

        try {
            this.pluginLocation = new URI(SWHVHEKSettings.PLUGIN_LOCATION);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        eventPlugin = new SWHVHEKPluginContainer(builtin_mode);
        addOverlayContainer(eventPlugin);

        SWHVHEKData.getSingletonInstance();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overrides the default method because the internal provided filters are
     * activated by default.
     */
    @Override
    public void installPlugin() {
        for (OverlayContainer overlay : overlayContainerList) {
            overlay.setActive(PluginSettings.getSingletonInstance().isOverlayInPluginActivated(pluginLocation, overlay.getOverlayClass(), true));
            overlay.setPosition(PluginSettings.getSingletonInstance().getOverlayPosition(pluginLocation, overlay.getOverlayClass()));
            PluginManager.getSingletonInstance().addOverlayContainer(overlay);
            ImageViewerGui.getSingletonInstance().getMainImagePanel().addPlugin(new SWHVHEKImagePanelEventPopupController());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * A description is not needed here because this plug-in is activated always
     * and will not be visible in the corresponding dialogs.
     */
    @Override
    public String getDescription() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "SWHV HEK Overlay Plugin " + "$Rev$" + (builtin_mode ? " Built-In Version" : "");
    }

    /**
     * {@inheritDoc}
     *
     * null because this is an internal plugin
     */
    @Override
    public String getAboutLicenseText() {
        String description = "";
        description += "<p>" + "The plugin uses the <a href=\"http://www.json.org/java/\">JSON in Java</a> Library, licensed under a <a href=\"http://www.json.org/license.html\">custom License</a>.";
        description += "<p>" + "The plugin uses the <a href=\"http://code.google.com/p/poly2tri/\">Poly2Tri</a> Library, licensed under the <a href=\"http://www.opensource.org/licenses/bsd-license.php\">BSD License</a>.";
        description += "<p>" + "The plugin uses the <a href=\"http://www.slf4j.org/\">Simple Logging Facade for Java (SLF4J)</a> Library,<br>licensed under the <a href=\"http://www.slf4j.org/license.html\">MIT License</a>.";

        return description;
    }

    public static URL getResourceUrl(String name) {
        return SWHVHEKPlugin.class.getResource(name);
    }

    /**
     * {@inheritDoc} In this case, does nothing.
     */
    @Override
    public void setState(String state) {
        // TODO Implement setState for HEKPlugin
    }

    /**
     * {@inheritDoc} In this case, does nothing.
     */
    @Override
    public String getState() {
        // TODO Implement getState for HEKPlugin
        return "";
    }

}
