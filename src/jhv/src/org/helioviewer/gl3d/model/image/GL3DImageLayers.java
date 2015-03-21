package org.helioviewer.gl3d.model.image;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import org.helioviewer.gl3d.scenegraph.GL3DDrawBits.Bit;
import org.helioviewer.gl3d.scenegraph.GL3DGroup;
import org.helioviewer.gl3d.scenegraph.GL3DNode;
import org.helioviewer.gl3d.scenegraph.GL3DState;
import org.helioviewer.viewmodel.view.opengl.shader.GLSLShader;

/**
 * The {@link GL3DImageLayers} node offers special capabilities for grouping
 * {@link GL3DImageLayer} nodes, because image nodes require special ordering
 * for the blending of different image layers.
 *
 * @author Simon Spoerri (simon.spoerri@fhnw.ch)
 *
 */
public class GL3DImageLayers extends GL3DGroup {

    private boolean coronaVisibility = true;

    public GL3DImageLayers() {
        super("Images");
    }

    @Override
    public void shapeInit(GL3DState state) {
        super.shapeInit(state);
    }

    @Override
    public void shapeDraw(GL3DState state) {
        GL3DNode node = this.getFirst();

        ArrayList<GL3DImageLayer> layers = new ArrayList<GL3DImageLayer>();
        while (node != null) {
            if (!node.isDrawBitOn(Bit.Hidden) && node instanceof GL3DImageLayer) {
                layers.add((GL3DImageLayer) node);
            }
            node = node.getNext();
        }

        GL2 gl = state.gl;
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        GLSLShader.bind(gl);
        for (GL3DImageLayer layer : layers) {
            layer.draw(state);
        }
        GLSLShader.unbind(gl);

        gl.glDisable(GL2.GL_BLEND);
    }

    @Override
    public void shapeUpdate(GL3DState state) {
        super.shapeUpdate(state);
        this.markAsChanged();
    }

    public void setCoronaVisibility(boolean visible) {
        GL3DNode node = first;
        while (node != null) {
            if (node instanceof GL3DImageLayer) {
                ((GL3DImageLayer) node).setCoronaVisibility(visible);
            }
            node = node.getNext();
        }
        coronaVisibility = visible;
    }

    public boolean getCoronaVisibility() {
        return coronaVisibility;
    }

    public void insertLayer(GL3DImageLayer layer) {
        this.addNode(layer);
    }

}
