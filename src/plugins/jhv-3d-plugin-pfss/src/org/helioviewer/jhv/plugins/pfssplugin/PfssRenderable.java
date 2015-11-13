package org.helioviewer.jhv.plugins.pfssplugin;

import java.awt.Component;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.helioviewer.jhv.camera.Camera;
import org.helioviewer.jhv.camera.Viewport;
import org.helioviewer.jhv.gui.ImageViewerGui;
import org.helioviewer.jhv.layers.Layers;
import org.helioviewer.jhv.layers.LayersListener;
import org.helioviewer.jhv.plugins.pfssplugin.data.PfssData;
import org.helioviewer.jhv.plugins.pfssplugin.data.PfssNewDataLoader;
import org.helioviewer.jhv.renderable.gui.AbstractRenderable;
import org.helioviewer.jhv.threads.CancelTask;
import org.helioviewer.jhv.viewmodel.view.View;

import com.jogamp.opengl.GL2;

/**
 * @author Stefan Meier (stefan.meier@fhnw.ch)
 * */
public class PfssRenderable extends AbstractRenderable implements LayersListener {

    private final PfssPluginPanel optionsPanel;
    private PfssData previousPfssData = null;

    /**
     * Default constructor.
     */
    public PfssRenderable() {
        optionsPanel = new PfssPluginPanel();
    }

    @Override
    public void render(Camera camera, Viewport vp, GL2 gl) {
        if (isVisible[vp.getIndex()]) {
            PfssData pfssData;
            long millis = Layers.getLastUpdatedTimestamp().getTime();
            if ((pfssData = PfssPlugin.getPfsscache().getData(millis)) != null) {
                if (previousPfssData != null && previousPfssData != pfssData && previousPfssData.isInit()) {
                    previousPfssData.clear(gl);
                }
                if (!pfssData.isInit())
                    pfssData.init(gl);
                if (pfssData.isInit()) {
                    pfssData.display(gl);
                    datetime = pfssData.getDateString();
                    ImageViewerGui.getRenderableContainer().fireTimeUpdated(this);
                }
                previousPfssData = pfssData;
            }
        }
    }

    @Override
    public void remove(GL2 gl) {
        dispose(gl);
    }

    @Override
    public Component getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public String getName() {
        return "PFSS Model";
    }

    private String datetime = null;

    @Override
    public String getTimeString() {
        return datetime;
    }

    @Override
    public void layerAdded(View view) {
        PfssPlugin.getPfsscache().clear();

        FutureTask<Void> dataLoaderTask = new FutureTask<Void>(new PfssNewDataLoader(Layers.getStartDate(), Layers.getEndDate()), null);
        PfssPlugin.pfssNewLoadPool.submit(dataLoaderTask);
        PfssPlugin.pfssReaperPool.schedule(new CancelTask(dataLoaderTask), 60 * 5, TimeUnit.SECONDS);
    }

    @Override
    public void activeLayerChanged(View view) {
    }

    @Override
    public boolean isDeletable() {
        return false;
    }

    @Override
    public void init(GL2 gl) {
    }

    @Override
    public void dispose(GL2 gl) {
        PfssPlugin.getPfsscache().destroy(gl);
    }

}
