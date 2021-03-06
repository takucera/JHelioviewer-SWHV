package org.helioviewer.jhv.base.scale;

import org.helioviewer.jhv.camera.Camera;
import org.helioviewer.jhv.camera.CameraHelper;
import org.helioviewer.jhv.display.Viewport;
import org.helioviewer.jhv.layers.GridLayer;
import org.helioviewer.jhv.math.MathUtils;
import org.helioviewer.jhv.math.Quat;
import org.helioviewer.jhv.math.Vec2;
import org.helioviewer.jhv.math.Vec3;

public interface GridScale {

    double getInterpolatedXValue(double v);
    double getInterpolatedYValue(double v);
    double getXValueInv(double v);
    double getYValueInv(double v);
    double getYstart();
    double getYstop();
    void set(double _xStart, double _xStop, double _yStart, double _yStop);
    Vec2 mouseToGrid(int px, int py, Viewport vp, Camera camera, GridLayer.GridType gridType);
    Vec2 mouseToGridInv(int px, int py, Viewport vp, Camera camera);

    GridScale polar = new GridScaleIdentity(0, 360, 0, 0);
    GridScale lati = new GridScaleIdentity(0, 360, -90, 90);
    GridScale logpolar = new GridScaleLogY(0, 360, 0, 0);
    GridScale ortho = new GridScaleOrtho(0, 0, 0, 0);

    abstract class GridScaleAbstract implements GridScale {

        protected double xStart;
        protected double xStop;
        protected double yStart;
        protected double yStop;

        GridScaleAbstract(double _xStart, double _xStop, double _yStart, double _yStop) {
            set(_xStart, _xStop, _yStart, _yStop);
        }

        @Override
        public void set(double _xStart, double _xStop, double _yStart, double _yStop) {
            xStart = _xStart;
            xStop = _xStop;
            yStart = scaleY(_yStart);
            yStop = scaleY(_yStop);
        }

        @Override
        public double getInterpolatedXValue(double v) {
            return invScaleX(xStart + v * (xStop - xStart));
        }

        @Override
        public double getInterpolatedYValue(double v) {
            return invScaleY(yStart + v * (yStop - yStart));
        }

        @Override
        public double getXValueInv(double v) {
            return (scaleX(v) - xStart) / (xStop - xStart) - 0.5;
        }

        @Override
        public double getYValueInv(double v) {
            return (scaleY(v) - yStart) / (yStop - yStart) - 0.5;
        }

        @Override
        public double getYstart() {
            return yStart;
        }

        @Override
        public double getYstop() {
            return yStop;
        }

        @Override
        public Vec2 mouseToGrid(int px, int py, Viewport vp, Camera camera, GridLayer.GridType gridType) {
            double x = CameraHelper.computeUpX(camera, vp, px) / vp.aspect + 0.5;
            double y = CameraHelper.computeUpY(camera, vp, py) + 0.5;
            return new Vec2(getInterpolatedXValue(x), getInterpolatedYValue(y));
        }

        @Override
        public Vec2 mouseToGridInv(int px, int py, Viewport vp, Camera camera) {
            double x = CameraHelper.computeUpX(camera, vp, px) / vp.aspect;
            double y = CameraHelper.computeUpY(camera, vp, py);
            return new Vec2(x, y);
        }

        protected abstract double scaleX(double val);

        protected abstract double invScaleX(double val);

        protected abstract double scaleY(double val);

        protected abstract double invScaleY(double val);

    }

    class GridScaleLogY extends GridScaleAbstract {

        GridScaleLogY(double _xStart, double _xStop, double _yStart, double _yStop) {
            super(_xStart, _xStop, _yStart, _yStop);
        }

        @Override
        public double scaleX(double val) {
            return val;
        }

        @Override
        public double invScaleX(double val) {
            return val;
        }

        @Override
        public double scaleY(double val) {
            return Math.log(val);
        }

        @Override
        public double invScaleY(double val) {
            return Math.exp(val);
        }

    }

    class GridScaleIdentity extends GridScaleAbstract {

        GridScaleIdentity(double _xStart, double _xStop, double _yStart, double _yStop) {
            super(_xStart, _xStop, _yStart, _yStop);
        }

        @Override
        public double scaleX(double val) {
            return val;
        }

        @Override
        public double invScaleX(double val) {
            return val;
        }

        @Override
        public double scaleY(double val) {
            return val;
        }

        @Override
        public double invScaleY(double val) {
            return val;
        }
    }

    class GridScaleOrtho extends GridScaleIdentity {
        GridScaleOrtho(double _xStart, double _xStop, double _yStart, double _yStop) {
            super(_xStart, _xStop, _yStart, _yStop);
        }

        @Override
        public Vec2 mouseToGrid(int px, int py, Viewport vp, Camera camera, GridLayer.GridType gridType) {
            Vec3 p = CameraHelper.getVectorFromSphere(camera, vp, px, py, Quat.ZERO, true);
            if (p == null)
                return Vec2.NAN_VECTOR;

            if (gridType != GridLayer.GridType.Viewpoint) {
                Quat q = Quat.rotateWithConjugate(camera.getViewpoint().orientation, GridLayer.getGridQuat(camera, gridType));
                p = q.rotateInverseVector(p);
            }

            double theta = 90 - MathUtils.radeg * Math.acos(p.y);
            double phi = 90 - MathUtils.radeg * Math.atan2(p.z, p.x);
            phi = MathUtils.mapToMinus180To180(phi);

            if (gridType == GridLayer.GridType.Carrington && phi < 0)
                phi += 360;
            return new Vec2(phi, theta);
        }
    }

}
