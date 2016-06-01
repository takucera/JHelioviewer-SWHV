package org.helioviewer.jhv.plugins.eveplugin.view.linedataselector;

import org.helioviewer.jhv.plugins.eveplugin.EVEPlugin;

public abstract class AbstractLineDataSelectorElement implements LineDataSelectorElement {

    protected boolean isVisible = true;

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisibility(boolean visible) {
        isVisible = visible;
        EVEPlugin.dc.fireRedrawRequest();
    }

}