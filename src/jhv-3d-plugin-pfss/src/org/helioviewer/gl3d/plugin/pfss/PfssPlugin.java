package org.helioviewer.gl3d.plugin.pfss;

import javax.swing.JComponent;

import org.helioviewer.gl3d.plugin.GL3DAbstractModelPlugin;

/**
 * The PFSS Plugin provides a means to load file based PFSS Model, that is
 * extrapolated magnetic field lines. This plugin can read files that are
 * generated by the resources/idl/conversion/pfss_field_export.pro file is built
 * on top of Marc deRosas PFSS SSWIDL Package.
 * 
 * @author Simon Spoerri (simon.spoerri@fhnw.ch)
 * 
 */
public class PfssPlugin extends GL3DAbstractModelPlugin {

    private PfssConfigPanel configPanel;

    public void load() {
        configPanel = new PfssConfigPanel(this);
        configPanel.init();
    }

    public void unload() {

    }

    public String getPluginDescription() {
        return "PFSS Curves are 3-dimensional magnetic field lines";
    }

    public String getPluginName() {
        return "PFSS";
    }

    public JComponent getConfigurationComponent() {
        return this.configPanel;
    }

    public static void main(String[] args) {
        // nothing
    }
}
