package org.helioviewer.jhv.viewmodel.view.jp2view;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.helioviewer.jhv.base.astronomy.Position;
import org.helioviewer.jhv.camera.Camera;
import org.helioviewer.jhv.display.Viewport;
import org.helioviewer.jhv.threads.JHVThread;
import org.helioviewer.jhv.viewmodel.view.jp2view.image.JP2ImageParameter;

class JP2ViewExecutor {

    private final ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(1);
    // no need to intercept exceptions
    private final ExecutorService executor = new ThreadPoolExecutor(1, 1, 10000L, TimeUnit.MILLISECONDS, blockingQueue,
                                                                    new JHVThread.NamedThreadFactory("Render"),
                                                                    new ThreadPoolExecutor.DiscardPolicy());

    void execute(Camera camera, Viewport vp, Position.Q viewpoint, JP2View view, JP2Image image, int frame, double factor) {
        // order is important, this will signal reader
        JP2ImageParameter params = image.calculateParameter(camera, vp, viewpoint, frame, factor);
        AtomicBoolean status = image.getStatusCache().frameLevelComplete(frame, params.resolution.level);
        if (status == null)
            return;

        blockingQueue.poll();
        executor.execute(new J2KRender(view, params, !status.get()));
    }

    void abolish() {
        try {
            executor.shutdown();
            while (!executor.awaitTermination(1000L, TimeUnit.MILLISECONDS)) ;
        } catch (Exception ignore) {
        }
    }

}
