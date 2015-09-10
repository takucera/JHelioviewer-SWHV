package org.helioviewer.viewmodel.view.jp2view;

import java.awt.Dimension;

import org.helioviewer.base.Region;
import org.helioviewer.base.Viewport;
import org.helioviewer.viewmodel.view.jp2view.image.JP2ImageParameter;
import org.helioviewer.viewmodel.view.jp2view.image.ResolutionSet;
import org.helioviewer.viewmodel.view.jp2view.image.ResolutionSet.ResolutionLevel;
import org.helioviewer.viewmodel.view.jp2view.image.SubImage;

public class JHVJP2CallistoView extends JHVJP2View {

    private Viewport viewport;

    public JHVJP2CallistoView() {
        region = new Region(0, 0, 86400, 380);
        viewport = new Viewport(2700, 12);
    }

    public JP2Image getJP2Image() {
        return _jp2Image;
    }

    public boolean setViewport(Viewport v) {
        viewport = v;
        signalRender(_jp2Image, true);
        return true;
    }

    public boolean setRegion(Region r) {
        boolean changed = region == null ? r == null : !region.equals(r);
        region = r;
        return changed;
    }

    @Override
    public void render() {
    }

    @Override
    protected JP2ImageParameter calculateParameter(JP2Image jp2Image, Region r, int frameNumber) {
        ResolutionSet set = jp2Image.getResolutionSet();
        int maxHeight = set.getResolutionLevel(0).getResolutionBounds().height;
        int maxWidth = set.getResolutionLevel(0).getResolutionBounds().width;
        ResolutionLevel res = set.getClosestResolutionLevel(new Dimension((int) Math.ceil(viewport.getWidth() / r.getWidth() * maxWidth), 2 * (int) Math.ceil(viewport.getHeight() / r.getHeight() * maxHeight)));

        SubImage subImage = new SubImage((int) (r.getLowerLeftCorner().x / maxWidth * res.getResolutionBounds().width), (int) (r.getLowerLeftCorner().y / maxHeight * res.getResolutionBounds().height), (int) (r.getWidth() / maxWidth * res.getResolutionBounds().width), (int) (r.getHeight() / maxHeight * res.getResolutionBounds().height));
        return new JP2ImageParameter(jp2Image, subImage, res, frameNumber);
    }

}
