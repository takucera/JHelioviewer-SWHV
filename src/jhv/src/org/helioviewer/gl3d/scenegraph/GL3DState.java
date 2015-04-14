package org.helioviewer.gl3d.scenegraph;

import java.util.Date;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.helioviewer.base.logging.Log;
import org.helioviewer.gl3d.camera.GL3DCamera;
import org.helioviewer.gl3d.math.GL3DMat4d;
import org.helioviewer.viewmodel.view.opengl.GL3DComponentView;

/**
 * The {@link GL3DState} is recreated every render pass by the
 * {@link GL3DComponentView}. It provides the reference to the {@link GL2}
 * object and stores some globally relevant information such as width and height
 * of the viewport, etc. Also it allows for the stacking of the view
 * transformations.
 *
 * @author Simon Spoerri (simon.spoerri@fhnw.ch)
 *
 */
public class GL3DState {

    private static GL3DState instance;

    public GL2 gl;

    protected GL3DCamera activeCamera;

    protected int viewportWidth;
    protected int viewportHeight;

    private Date currentObservationDate;

    public static GL3DState create(GL2 gl) {
        instance = new GL3DState(gl);
        return instance;
    }

    public static GL3DState get() {
        return instance;
    }

    public static GL3DState setUpdated(GL2 gl, int width, int height) {
        instance.gl = gl;
        instance.viewportWidth = width;
        instance.viewportHeight = height;
        return instance;
    }

    private GL3DState(GL2 gl) {
        this.gl = gl;
    }

    public void loadIdentity() {
        this.gl.glLoadIdentity();
    }

    public void multiplyMV(GL3DMat4d m) {
        gl.glMultMatrixd(m.m, 0);
    }

    public boolean checkGLErrors(String message) {
        if (gl == null) {
            Log.warn("OpenGL not yet Initialised!");
            return true;
        }
        /*
         * To allow for distributed implementations, there may be several error
         * flags. If any single error flag has recorded an error, the value of
         * that flag is returned and that flag is reset to GL_NO_ERROR when
         * glGetError is called. If more than one flag has recorded an error,
         * glGetError returns and clears an arbitrary error flag value. Thus,
         * glGetError should always be called in a loop, until it returns
         * GL_NO_ERROR, if all error flags are to be reset.
         */
        int glErrorCode = gl.glGetError();

        if (glErrorCode != GL2.GL_NO_ERROR) {
            GLU glu = new GLU();
            Log.error("GL Error (" + glErrorCode + "): " + glu.gluErrorString(glErrorCode) + " - @" + message);

            return true;
        } else {
            return false;
        }
    }

    public boolean checkGLErrors() {
        return checkGLErrors(this.gl);
    }

    public boolean checkGLErrors(GL2 gl) {
        if (gl == null) {
            Log.warn("OpenGL not yet Initialised!");
            return true;
        }
        int glErrorCode = gl.glGetError();

        if (glErrorCode != GL2.GL_NO_ERROR) {
            GLU glu = new GLU();
            Log.error("GL Error (" + glErrorCode + "): " + glu.gluErrorString(glErrorCode));

            return true;
        } else {
            return false;
        }
    }

    public void setActiveChamera(GL3DCamera camera) {
        this.activeCamera = camera;
    }

    public GL3DCamera getActiveCamera() {
        return activeCamera;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public Date getCurrentObservationDate() {
        return currentObservationDate;
    }

    public void setCurrentObservationDate(Date currentObservationDate) {
        this.currentObservationDate = currentObservationDate;
    }

}
