package org.helioviewer.jhv.opengl;

import java.awt.Dimension;
import java.awt.Point;
import java.nio.FloatBuffer;

import org.helioviewer.jhv.base.BufferUtils;
import org.helioviewer.jhv.camera.Camera;
import org.helioviewer.jhv.display.Display;
import org.helioviewer.jhv.display.Viewport;
import org.helioviewer.jhv.math.Vec2;
import org.helioviewer.jhv.math.Vec3;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
//import com.jogamp.opengl.awt.GLCanvas;

public class GLHelper {

    public static void initCircleFront(GL2 gl, GLShape circle, double x, double y, double r, int segments, float[] color) {
        FloatBuffer positionBuffer = BufferUtils.newFloatBuffer(4 * (segments + 1));
        FloatBuffer colorBuffer = BufferUtils.newFloatBuffer(4 * (segments + 1));
        for (int i = 0; i <= segments; ++i) {
            double t = -2 * Math.PI * i / segments; // + for backside
            BufferUtils.put4f(positionBuffer, (float) (x + Math.sin(t) * r), (float) (y + Math.cos(t) * r), 0, 1);
            colorBuffer.put(color);
        }
        positionBuffer.rewind();
        colorBuffer.rewind();
        circle.setData(gl, positionBuffer, colorBuffer);
    }

    public static void initRectangleFront(GL2 gl, GLShape rectangle, double x0, double y0, double w, double h, float[] color) {
        int vertices = 4;
        FloatBuffer positionBuffer = BufferUtils.newFloatBuffer(4 * vertices);
        FloatBuffer colorBuffer = BufferUtils.newFloatBuffer(4 * vertices);
        float x1 = (float) (x0 + w);
        float y1 = (float) (y0 + h);

        BufferUtils.put4f(positionBuffer, (float) x0, (float) -y0, 0, 1); // x0 -y0 backside
        BufferUtils.put4f(positionBuffer, (float) x0,         -y1, 0, 1); // x1 -y0
        BufferUtils.put4f(positionBuffer,         x1,         -y1, 0, 1); // x1 -y1
        BufferUtils.put4f(positionBuffer,         x1, (float) -y0, 0, 1); // x0 -y1

        for (int i = 0; i < vertices; i++)
            colorBuffer.put(color);

        positionBuffer.rewind();
        colorBuffer.rewind();
        rectangle.setData(gl, positionBuffer, colorBuffer);
    }

    public static Point GL2AWTPoint(int x, int y) {
        return new Point((int) (x / GLInfo.pixelScaleFloat[0]), (int) (y / GLInfo.pixelScaleFloat[1]));
    }

    public static Dimension GL2AWTDimension(int x, int y) {
        return new Dimension((int) (x / GLInfo.pixelScaleFloat[0]), (int) (y / GLInfo.pixelScaleFloat[1]));
    }

    public static Vec2 drawVertex(Camera camera, Viewport vp, GL2 gl, Vec3 current, Vec2 previous) {
        Vec3 pt = camera.getViewpoint().orientation.rotateVector(current);
        Vec2 tf = Display.mode.xform.transform(pt, Display.mode.scale);
        if (previous != null) {
            if (tf.x <= 0 && previous.x >= 0 && Math.abs(previous.x - tf.x) > 0.5) {
                gl.glVertex2f((float) (0.5 * vp.aspect), (float) tf.y);
                gl.glEnd();
                gl.glBegin(GL2.GL_LINE_STRIP);
                gl.glVertex2f((float) (-0.5 * vp.aspect), (float) tf.y);
                gl.glVertex2f((float) (tf.x * vp.aspect), (float) tf.y);
            } else if (tf.x >= 0 && previous.x <= 0 && Math.abs(previous.x - tf.x) > 0.5) {
                gl.glVertex2f((float) (-0.5 * vp.aspect), (float) tf.y);
                gl.glEnd();
                gl.glBegin(GL2.GL_LINE_STRIP);
                gl.glVertex2f((float) (0.5 * vp.aspect), (float) tf.y);
                gl.glVertex2f((float) (tf.x * vp.aspect), (float) tf.y);
            } else {
                gl.glVertex2f((float) (tf.x * vp.aspect), (float) tf.y);
            }
        } else {
            gl.glVertex2f((float) (tf.x * vp.aspect), (float) tf.y);
        }
        return tf;
    }
/*
    public static GLCanvas createGLCanvas() {
        GLProfile profile = GLProfile.getDefault();
        GLCapabilities capabilities = getGLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);

        // GUI events can lead to context destruction and invalidation of GL objects and state
        canvas.setSharedAutoDrawable(getSharedDrawable(profile, capabilities));

        return canvas;
    }
*/
    public static GLWindow createGLWindow() {
        GLProfile profile = GLProfile.getDefault();
        GLCapabilities capabilities = getGLCapabilities(profile);
        GLWindow window = GLWindow.create(capabilities);

        // GUI events can lead to context destruction and invalidation of GL objects and state
        window.setSharedAutoDrawable(getSharedDrawable(profile, capabilities));

        return window;
    }

    private static GLCapabilities getGLCapabilities(GLProfile profile) {
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setSampleBuffers(true);
        capabilities.setNumSamples(GLInfo.GLSAMPLES);
        return capabilities;
    }

    private static GLAutoDrawable getSharedDrawable(GLProfile profile, GLCapabilities capabilities) {
        GLAutoDrawable sharedDrawable = GLDrawableFactory.getFactory(profile).createDummyAutoDrawable(null, true, capabilities, null);
        sharedDrawable.display();
        return sharedDrawable;
    }

}
