package org.helioviewer.viewmodel.view.jp2view;

import java.awt.Rectangle;

import org.helioviewer.base.Region;
import org.helioviewer.base.Viewport;
import org.helioviewer.base.logging.Log;
import org.helioviewer.viewmodel.imagedata.ImageData;
import org.helioviewer.viewmodel.view.jp2view.image.JP2ImageParameter;
import org.helioviewer.viewmodel.view.jp2view.image.ResolutionSet;
import org.helioviewer.viewmodel.view.jp2view.image.ResolutionSet.ResolutionLevel;
import org.helioviewer.viewmodel.view.jp2view.image.SubImage;

public class JP2CallistoView extends JP2View {

    private Viewport viewport = new Viewport(86400, 380);

    public JP2Image getJP2Image() {
        return _jp2Image;
    }

    public void setViewport(Viewport v) {
        viewport = v;
    }

    public boolean setRegion(Region r) {
        targetRegion = r;
        signalRender(_jp2Image);
        return true;
    }

    @Override
    public void render() {
        // should be called only during setJP2Image
    }

    @Override
    void setSubimageData(ImageData newImageData, JP2ImageParameter params) {
        if (dataHandler != null) {
            dataHandler.handleData(this, newImageData);
        }
    }

    @Override
    protected JP2ImageParameter calculateParameter(JP2Image jp2Image, Region r, int frameNumber) {
        double rWidth = r.getWidth();
        double rHeight = r.getHeight();

        ResolutionSet set = jp2Image.getResolutionSet();
        int maxHeight = set.getResolutionLevel(0).getResolutionBounds().height;
        int maxWidth = set.getResolutionLevel(0).getResolutionBounds().width;

        ResolutionLevel res = set.getPreviousResolutionLevel((int) Math.ceil(viewport.getWidth() / rWidth * maxWidth),
                                                        2 * (int) Math.ceil(viewport.getHeight() / rHeight * maxHeight));
        Rectangle rect = res.getResolutionBounds();

        SubImage subImage = new SubImage((int) (r.getLowerLeftCorner().x / maxWidth * rect.width),
                                         (int) (r.getLowerLeftCorner().y / maxHeight * rect.height),
                                         (int) Math.ceil(rWidth / maxWidth * rect.width),
                                         (int) Math.ceil(rHeight / maxHeight * rect.height), rect);

        return new JP2ImageParameter(jp2Image, subImage, res, frameNumber);
    }

}
