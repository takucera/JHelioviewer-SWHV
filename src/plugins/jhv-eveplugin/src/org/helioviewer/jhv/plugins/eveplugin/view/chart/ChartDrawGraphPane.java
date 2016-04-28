package org.helioviewer.jhv.plugins.eveplugin.view.chart;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;

import org.helioviewer.jhv.base.time.JHVDate;
import org.helioviewer.jhv.base.time.TimeUtils;
import org.helioviewer.jhv.data.datatype.event.JHVRelatedEvents;
import org.helioviewer.jhv.data.guielements.SWEKEventInformationDialog;
import org.helioviewer.jhv.export.ExportMovie;
import org.helioviewer.jhv.gui.UIGlobals;
import org.helioviewer.jhv.layers.Layers;
import org.helioviewer.jhv.opengl.GLInfo;
import org.helioviewer.jhv.plugins.eveplugin.DrawConstants;
import org.helioviewer.jhv.plugins.eveplugin.EVEPlugin;
import org.helioviewer.jhv.plugins.eveplugin.draw.DrawController;
import org.helioviewer.jhv.plugins.eveplugin.draw.DrawControllerListener;
import org.helioviewer.jhv.plugins.eveplugin.draw.TimeAxis;
import org.helioviewer.jhv.plugins.eveplugin.draw.YAxis;
import org.helioviewer.jhv.plugins.eveplugin.events.model.EventModel;
import org.helioviewer.jhv.plugins.eveplugin.view.linedataselector.LineDataSelectorElement;

@SuppressWarnings("serial")
public class ChartDrawGraphPane extends JComponent implements MouseInputListener, ComponentListener, DrawControllerListener, MouseWheelListener {

    private final DrawController drawController;
    private long movieTimestamp = Long.MIN_VALUE;
    private int movieLinePosition = -1;
    private Point mousePressedPosition = null;
    private Point mouseDragPosition = null;

    private Rectangle graphArea = new Rectangle();
    private Rectangle graphSize = new Rectangle();
    private Rectangle plotArea = new Rectangle();
    private BufferedImage screenImage = null;
    private final EventModel eventModel;
    private Rectangle leftAxisArea;

    private Point mousePosition;
    private boolean mouseOverEvent;
    private int lastWidth;
    private int lastHeight;
    private boolean updateRequestReceived;

    private boolean movieLineRequest = false;
    private boolean forceRedrawGraph = false;

    public ChartDrawGraphPane() {
        setOpaque(true);
        setDoubleBuffered(false);

        updateRequestReceived = false;
        drawController = EVEPlugin.dc;

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
        drawController.addDrawControllerListener(this);
        eventModel = EventModel.getSingletonInstance();

        Timer redrawTimer = new Timer(1000 / 20, new RedrawListener());
        redrawTimer.start();

        setChartInformation();
    }

    private class RedrawListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (updateRequestReceived) {
                updateRequestReceived = false;
                updateDrawInformation();
                redrawGraph();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);

        Graphics2D g = (Graphics2D) g1;
        if (screenImage != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(screenImage, 0, 0, getWidth(), getHeight(), 0, 0, screenImage.getWidth(), screenImage.getHeight(), null);
            drawMovieLine(g);
        }
    }

    private void updateGraph() {
        updateRequestReceived = true;
    }

    private void redrawGraph() {
        int sx = GLInfo.pixelScale[0], sy = GLInfo.pixelScale[1];
        int width = sx * getWidth();
        int height = sy * getHeight();

        if (width > 0 && height > 0 && sy * (DrawConstants.GRAPH_TOP_SPACE + DrawConstants.GRAPH_BOTTOM_SPACE + 1) < height && sx * (DrawConstants.GRAPH_LEFT_SPACE + DrawConstants.GRAPH_RIGHT_SPACE + 1) < width && (!movieLineRequest || forceRedrawGraph)) {
            if (width != lastWidth || height != lastHeight) {
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice device = env.getDefaultScreenDevice();
                GraphicsConfiguration config = device.getDefaultConfiguration();

                screenImage = config.createCompatibleImage(width, height, Transparency.OPAQUE);
                ExportMovie.EVEImage = screenImage;

                lastWidth = width;
                lastHeight = height;
            }

            final Graphics2D g = screenImage.createGraphics();
            AffineTransform tf = g.getTransform();
            tf.preConcatenate(AffineTransform.getScaleInstance(sx, sy));
            g.setTransform(tf);
            drawBackground(g);

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(DrawConstants.font);
            drawData(g, mousePosition);

            g.dispose();
        }
        this.repaint();
        movieLineRequest = false;
        forceRedrawGraph = false;
    }

    private void drawData(Graphics2D plotG, Point mousePosition) {
        List<LineDataSelectorElement> els = EVEPlugin.ldsm.getAllLineDataSelectorElements();
        for (LineDataSelectorElement el : els) {
            el.draw(plotG, graphArea, leftAxisArea, drawController.selectedAxis, mousePosition);
        }
        drawLabels(plotG);
    }

    private void updateDrawInformation() {
        updateGraphArea();
        updateMovieLineInformation();
    }

    private void drawBackground(Graphics2D g) {
        g.setColor(DrawConstants.SELECTED_INTERVAL_BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawLabels(final Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, graphSize.width, DrawConstants.GRAPH_TOP_SPACE);
        g.fillRect(0, graphArea.height + DrawConstants.GRAPH_TOP_SPACE, graphSize.width, DrawConstants.GRAPH_BOTTOM_SPACE);
        g.fillRect(0, 0, DrawConstants.GRAPH_LEFT_SPACE, graphSize.height);

        TimeAxis xAxis = EVEPlugin.dc.selectedAxis;
        int ct = 0;
        Color c = DrawConstants.TICK_LINE_COLOR;
        for (LineDataSelectorElement el : EVEPlugin.ldsm.getAllLineDataSelectorElements()) {
            if (el.showYAxis()) {
                if (ct == 0) {
                    c = el.getDataColor();
                }
                drawVerticalLabels(g, el, ct);
                ct++;
            }
        }
        if (ct == 0) {
            drawNoData(g, graphArea);
            return;
        }
        Rectangle2D tickTextBounds = g.getFontMetrics().getStringBounds(DrawConstants.FULL_DATE_TIME_FORMAT.format(new Date(xAxis.start)), g);
        int tickTextWidth = (int) tickTextBounds.getWidth();
        final int tickTextHeight = (int) tickTextBounds.getHeight();
        final int horizontalTickCount = Math.max(2, (graphArea.width - tickTextWidth * 2) / tickTextWidth);
        final long tickDifferenceHorizontal = (xAxis.end - xAxis.start) / (horizontalTickCount - 1);

        long previousDate = Long.MIN_VALUE;
        for (int i = 0; i < horizontalTickCount; ++i) {
            final long tickValue = xAxis.start + i * tickDifferenceHorizontal;
            final int x = drawController.selectedAxis.value2pixel(graphArea.x, graphArea.width, tickValue);
            final String tickText;
            if (previousDate == Long.MIN_VALUE) {
                tickText = DrawConstants.FULL_DATE_TIME_FORMAT_REVERSE.format(tickValue);
            } else {
                long tickDayNumber = tickValue / TimeUtils.DAY_IN_MILLIS;
                long prevDayNumber = previousDate / TimeUtils.DAY_IN_MILLIS;

                if (tickDayNumber == prevDayNumber) {
                    tickText = DrawConstants.HOUR_TIME_FORMAT.format(tickValue);
                } else {
                    tickText = DrawConstants.FULL_DATE_TIME_FORMAT_REVERSE.format(tickValue);
                }
            }

            g.setColor(c);
            g.drawLine(x, graphArea.y, x, graphArea.y + graphArea.height + 3);

            g.setColor(Color.BLACK);
            int yl = graphArea.y + graphArea.height + 2 + tickTextHeight;
            for (String line : tickText.split("\n")) {
                tickTextBounds = g.getFontMetrics().getStringBounds(line, g);
                tickTextWidth = (int) tickTextBounds.getWidth();
                int xl = x - (tickTextWidth / 2);
                if (xl > getWidth() - DrawConstants.GRAPH_RIGHT_SPACE - tickTextWidth) {
                    xl = getWidth() - DrawConstants.GRAPH_RIGHT_SPACE - tickTextWidth;
                }
                g.drawString(line, xl, yl);
                yl += g.getFontMetrics().getHeight() * 2 / 3;
            }

            previousDate = tickValue;
        }
    }

    private void drawNoData(Graphics2D g, Rectangle graphArea) {
        final String text = DrawConstants.absentText;
        final int textWidth = (int) g.getFontMetrics().getStringBounds(text, g).getWidth();
        final int x = graphArea.x + (graphArea.width / 2) - (textWidth / 2);
        final int y = graphArea.y + graphArea.height / 2;

        g.setColor(DrawConstants.LABEL_TEXT_COLOR);
        g.drawString(text, x, y);
    }

    private void drawVerticalLabels(Graphics2D g, LineDataSelectorElement el, int leftSide) {
        g.setColor(Color.WHITE);
        if (leftSide == 0) {
            g.fillRect(0, DrawConstants.GRAPH_TOP_SPACE, DrawConstants.GRAPH_LEFT_SPACE, graphArea.height);
            g.fillRect(graphArea.x + graphArea.width, DrawConstants.GRAPH_TOP_SPACE, DrawConstants.RIGHT_AXIS_WIDTH, graphArea.height);
        } else {
            g.fillRect(graphArea.x + graphArea.width + (leftSide - 1) * DrawConstants.RIGHT_AXIS_WIDTH, DrawConstants.GRAPH_TOP_SPACE, DrawConstants.RIGHT_AXIS_WIDTH, graphArea.height);
        }
        YAxis yAxis = el.getYAxis();
        int axis_x_offset;
        if (leftSide == 0) {
            axis_x_offset = graphArea.x;
        }
        else {
            axis_x_offset = graphArea.x + graphArea.width + (leftSide - 1) * DrawConstants.RIGHT_AXIS_WIDTH;
        }

        {
            String verticalLabel = yAxis.getLabel();
            final Rectangle2D verticalLabelBounds = g.getFontMetrics().getStringBounds(verticalLabel, g);
            int vWidth = (int) verticalLabelBounds.getWidth();
            int vHeight = (int) verticalLabelBounds.getHeight();
            int labelCompensation = vWidth / 2;
            g.setColor(el.getDataColor());
            g.drawString(verticalLabel, axis_x_offset - labelCompensation, vHeight);
        }

        final int sizeSteps = graphArea.height / DrawConstants.MIN_VERTICAL_TICK_SPACE;
        int verticalTicks = 2;
        if (sizeSteps >= 4) {
            verticalTicks = 5;
        } else if (verticalTicks >= 2) {
            verticalTicks = 3;
        }
        if (verticalTicks == 0) {
            final int y = graphArea.y + graphArea.height;
            g.drawLine(graphArea.x - 3, y, graphArea.x + graphArea.width, y);
        } else {
            final int tickDifferenceVertical = (graphArea.height) / (verticalTicks - 1);
            for (int i = 0; i < verticalTicks; i++) {
                final int y = graphArea.y + graphArea.height - i * tickDifferenceVertical;
                double tickValue = yAxis.pixel2ScaledValue(graphArea.y, graphArea.height, y);
                String tickText = DrawConstants.DECIMAL_FORMAT.format(tickValue);
                if (leftSide == 0) {
                    g.drawLine(graphArea.x - 3, y, graphArea.x + graphArea.width, y);
                }
                else {
                    g.drawLine(axis_x_offset, graphArea.y, axis_x_offset, graphArea.y + graphArea.height + 3);
                }
                final Rectangle2D bounds = g.getFontMetrics().getStringBounds(tickText, g);
                int x;
                if (leftSide == 0) {
                    x = axis_x_offset - 6 - (int) bounds.getWidth();
                }
                else {
                    x = axis_x_offset;
                }
                g.drawString(tickText, x, y + (int) (bounds.getHeight() / 2));
            }
        }
    }

    private void drawMovieLine(Graphics2D g) {
        if (movieLinePosition < 0 || graphArea.height < 0) {
            return;
        }
        g.setColor(DrawConstants.MOVIE_FRAME_COLOR);
        g.drawLine(movieLinePosition, graphArea.y, movieLinePosition, graphArea.y + graphArea.height);
    }

    private void updateGraphArea() {
        graphSize = drawController.getGraphSize();
        graphArea = drawController.getGraphArea();
        plotArea = drawController.getPlotArea();
        leftAxisArea = drawController.getLeftAxisArea();
    }

    private boolean updateMovieLineInformation() {
        int newMovieLine = -1;
        if (movieTimestamp == Long.MIN_VALUE) {
            newMovieLine = -1;
        } else {
            newMovieLine = drawController.selectedAxis.value2pixel(graphArea.x, graphArea.width, movieTimestamp);

            if (newMovieLine < graphArea.x || newMovieLine > (graphArea.x + graphArea.width)) {
                newMovieLine = -1;
            }
        }
        if (newMovieLine != movieLinePosition) {
            movieLinePosition = newMovieLine;
            ExportMovie.EVEMovieLinePosition = movieLinePosition;
            return true;
        }
        return false;
    }

    private void setMovieFrameManually(Point point) {
        if (movieTimestamp == Long.MIN_VALUE) {
            return;
        }
        long millis = drawController.selectedAxis.pixel2value(graphArea.x, graphArea.width, point.x);
        Layers.setTime(new JHVDate(millis));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        JHVRelatedEvents event = eventModel.getEventUnderMouse();
        Point p = e.getPoint();

        if (event != null) {
            SWEKEventInformationDialog dialog = new SWEKEventInformationDialog(event, event.getClosestTo(drawController.selectedAxis.pixel2value(graphArea.x, graphArea.width, p.x)));
            dialog.setLocation(e.getLocationOnScreen());
            dialog.validate();
            dialog.pack();
            dialog.setVisible(true);
        } else if (graphArea.contains(p)) {
            setMovieFrameManually(p);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mousePosition = null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        mousePressedPosition = plotArea.contains(p) ? p : null;
        if (p.x >= graphArea.x && p.x <= graphArea.x + graphArea.width && p.y >= graphArea.y && p.y <= graphArea.y + graphArea.height &&
                eventModel.getEventAtPosition(new Point(p.x - DrawConstants.GRAPH_LEFT_SPACE, p.y - DrawConstants.GRAPH_TOP_SPACE)) == null) {
            setCursor(UIGlobals.closedHandCursor);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Point p = e.getPoint();
        if (p.x >= graphArea.x && p.x <= graphArea.x + graphArea.width && p.y >= graphArea.y && p.y <= graphArea.y + graphArea.height) {
            setCursor(UIGlobals.openHandCursor);
        } else {
            setCursor(Cursor.getDefaultCursor());
        }

        if (mousePressedPosition != null && mouseDragPosition != null) {
            double distanceX = mousePressedPosition.x - p.x;
            drawController.move(graphArea.x, graphArea.width, distanceX);

            double distanceY = p.y - mousePressedPosition.y;
            mouseHelper(distanceY);
        }

        mousePressedPosition = null;
        mouseDragPosition = null;
    }

    private void mouseHelper(double distanceY) {
        for (LineDataSelectorElement el : EVEPlugin.ldsm.getAllLineDataSelectorElements()) {
            if (el.showYAxis())
                el.getYAxis().shiftDownPixels(distanceY, graphArea.height);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();

        mouseDragPosition = p;

        if (mousePressedPosition != null) {
            setCursor(UIGlobals.closedHandCursor);
            double distanceX = mousePressedPosition.x - p.x;
            double distanceY = p.y - mousePressedPosition.y;
            drawController.move(graphArea.x, graphArea.width, distanceX);
            mouseHelper(distanceY);
        }
        mousePressedPosition = p;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Rectangle frame = new Rectangle(movieLinePosition - 3, graphArea.y, 7, graphArea.height);
        mousePosition = e.getPoint();
        if (movieLinePosition >= 0 && frame.contains(mousePosition)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        } else if (EventModel.getSingletonInstance().getEventUnderMouse() != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            mouseOverEvent = true;
        } else if (mousePosition.x >= graphArea.x && mousePosition.x <= graphArea.x + graphArea.width && mousePosition.y >= graphArea.y && mousePosition.y <= graphArea.y + graphArea.height) {
            setCursor(UIGlobals.openHandCursor);
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
        if (mouseOverEvent && (eventModel.getEventAtPosition(mousePosition) == null)) {
            mouseOverEvent = false;
        }
        redrawGraph();
        updateGraph();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        setChartInformation();
    }

    @Override
    public void componentShown(ComponentEvent e) {
        setChartInformation();
    }

    private void setChartInformation() {
        drawController.setGraphInformation(new Rectangle(getWidth(), getHeight()));
    }

    @Override
    public void drawRequest() {
        forceRedrawGraph = true;
        updateGraph();
    }

    @Override
    public void drawMovieLineRequest(long time) {
        if (movieTimestamp == Long.MIN_VALUE || movieTimestamp != time) {
            movieTimestamp = time;
            if (!drawController.isLocked()) {
                if (updateMovieLineInformation()) {
                    movieLineRequest = true;
                    updateGraph();
                }
            }
        }
        if (time == Long.MIN_VALUE) {
            movieTimestamp = Long.MIN_VALUE;
            if (updateMovieLineInformation()) {
                updateGraph();
            }
        }
    }

    @Override
    public void movieIntervalChanged(long start, long end) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int scrollDistance = e.getWheelRotation() * e.getScrollAmount();
            double zoomTimeFactor = 10;
            final int mouseX = e.getX();
            final int mouseY = e.getY();
            boolean inGraphArea = (mouseX >= graphArea.x && mouseX <= graphArea.x + graphArea.width && mouseY > graphArea.y && mouseY <= graphArea.y + graphArea.height);
            boolean inXAxisOrAboveGraph = (mouseX >= graphArea.x && mouseX <= graphArea.x + graphArea.width && (mouseY <= graphArea.y || mouseY >= graphArea.y + graphArea.height));
            boolean inYAxis = (mouseX < graphArea.x || mouseX > graphArea.x + graphArea.width && mouseY > graphArea.y && mouseY <= graphArea.y + graphArea.height);
            if (inGraphArea || inXAxisOrAboveGraph) {
                if ((!e.isAltDown() && !e.isShiftDown()) || inXAxisOrAboveGraph) {
                    drawController.zoom(graphArea.x, graphArea.width, mouseX, zoomTimeFactor * scrollDistance);
                } else if (e.isShiftDown()) {
                    drawController.move(graphArea.x, graphArea.width, zoomTimeFactor * scrollDistance);
                }
            }
            if (inGraphArea || inYAxis) {
                if (((e.isControlDown() || e.isAltDown()) && !e.isShiftDown()) || inYAxis) {
                    for (LineDataSelectorElement el : EVEPlugin.ldsm.getAllLineDataSelectorElements()) {
                        if (el.showYAxis()) {
                            el.getYAxis().zoomSelectedRange(scrollDistance, getHeight() - mouseY - graphArea.y, graphArea.height);
                            el.yaxisChanged();
                            drawController.fireRedrawRequest();
                        }
                    }
                }
            }
        }
    }

}
