package org.helioviewer.jhv.plugins.eveplugin;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

import javax.swing.JComponent;

import org.helioviewer.jhv.base.plugin.interfaces.Plugin;
import org.helioviewer.jhv.data.datatype.event.JHVRelatedEvents;
import org.helioviewer.jhv.gui.ImageViewerGui;
import org.helioviewer.jhv.gui.interfaces.MainContentPanelPlugin;
import org.helioviewer.jhv.layers.Layers;
import org.helioviewer.jhv.plugins.eveplugin.draw.DrawController;
import org.helioviewer.jhv.plugins.eveplugin.radio.RadioData;
import org.helioviewer.jhv.plugins.eveplugin.lines.BandTypeAPI;
import org.helioviewer.jhv.plugins.eveplugin.view.ObservationDialogUIPanel;
import org.helioviewer.jhv.plugins.eveplugin.view.chart.PlotPanel;
import org.helioviewer.jhv.plugins.eveplugin.view.linedataselector.LineDataSelectorModel;
import org.helioviewer.jhv.plugins.eveplugin.view.linedataselector.LineDataSelectorTablePanel;
import org.helioviewer.jhv.threads.JHVExecutor;
import org.helioviewer.jhv.threads.JHVWorker;

public class EVEPlugin implements Plugin, MainContentPanelPlugin {

    private static final int MAX_WORKER_THREADS = 12;
    public static final ExecutorService executorService = JHVExecutor.getJHVWorkersExecutorService("EVE", MAX_WORKER_THREADS);

    private final LinkedList<JComponent> pluginPanes = new LinkedList<>();
    private final PlotPanel plotOne = new PlotPanel();

    public static final LineDataSelectorModel ldsm = new LineDataSelectorModel();
    public static final DrawController dc = new DrawController();
    public static final RadioData rdm = new RadioData();
    public static final ObservationDialogUIPanel op = new ObservationDialogUIPanel();

    private static final LineDataSelectorTablePanel timelinePluginPanel = new LineDataSelectorTablePanel();

    @Override
    public void installPlugin() {
        pluginPanes.add(plotOne);

        ImageViewerGui.getLeftContentPane().add("Timeline Layers", timelinePluginPanel, true);
        ImageViewerGui.getLeftContentPane().revalidate();

        ImageViewerGui.getMainContentPanel().addPlugin(EVEPlugin.this);

        Layers.addLayersListener(dc);
        Layers.addTimeListener(dc);
        Layers.addTimespanListener(dc);
        JHVRelatedEvents.addHighlightListener(dc);

        ldsm.addLineDataSelectorModelListener(op);

        JHVWorker<Void, Void> loadSources = new JHVWorker<Void, Void>() {

            @Override
            protected Void backgroundWork() {
                BandTypeAPI.getSingletonInstance().getDatasets();
                return null;
            }

            @Override
            protected void done() {
                op.setupDatasets();
            }

        };

        loadSources.setThreadName("EVE--LoadSources");
        executorService.execute(loadSources);
    }

    @Override
    public void uninstallPlugin() {
        JHVRelatedEvents.removeHighlightListener(dc);
        Layers.removeTimeListener(dc);
        Layers.removeLayersListener(dc);

        ImageViewerGui.getMainContentPanel().removePlugin(this);

        ImageViewerGui.getLeftContentPane().remove(timelinePluginPanel);
        ImageViewerGui.getLeftContentPane().revalidate();
        pluginPanes.remove(plotOne);
    }

    @Override
    public String getName() {
        return "EVEPlugin " + "$Rev$";
    }

    @Override
    public String getDescription() {
        return "This plugin visualizes 1D and 2D time series";
    }

    @Override
    public String getAboutLicenseText() {
        return "<p>The plugin uses the <a href=\"https://github.com/stleary/JSON-java\">JSON in Java</a> Library, licensed under a custom <a href=\"http://www.json.org/license.html\">License</a>.";
    }

    @Override
    public LinkedList<JComponent> getVisualInterfaces() {
        return pluginPanes;
    }

    @Override
    public String getTabName() {
        return "Timelines";
    }

    @Override
    public void setState(String state) {
    }

    @Override
    public String getState() {
        return null;
    }

}
