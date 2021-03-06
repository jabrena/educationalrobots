/*
 * Copyright (C) 2009 Samuel Audet
 *
 * This file is part of JavaCV.
 *
 * JavaCV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JavaCV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaCV.  If not, see <http://www.gnu.org/licenses/>.
 */

package name.audet.samuel.javacv;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import name.audet.samuel.javacv.jna.cv;
import name.audet.samuel.javacv.jna.cxcore;
import name.audet.samuel.javacv.jna.cxcore.CvMat;
import name.audet.samuel.javacv.jna.cxcore.IplImage;

/**
 *
 * @author Samuel Audet
 */
public class ProCamGeometricCalibrator {
    public ProCamGeometricCalibrator(Settings settings, MarkerDetector.Settings detectorSettings,
            MarkedPlane boardPlane, MarkedPlane projectorPlane,
            ProjectiveDevice camera, ProjectiveDevice projector) {
        this(settings, detectorSettings, boardPlane, projectorPlane, 
                new GeometricCalibrator[] { 
                new GeometricCalibrator(settings, detectorSettings, boardPlane, camera)},
                new GeometricCalibrator(settings, detectorSettings, projectorPlane, projector));
    }
    @SuppressWarnings("unchecked")
    public ProCamGeometricCalibrator(Settings settings, MarkerDetector.Settings detectorSettings,
            MarkedPlane boardPlane, MarkedPlane projectorPlane,
            GeometricCalibrator[] cameraCalibrators, GeometricCalibrator projectorCalibrator) {
        this.settings = settings;
        this.detectorSettings = detectorSettings;
        this.boardPlane = boardPlane;
        this.projectorPlane = projectorPlane;

        this.cameraCalibrators = cameraCalibrators;
        int n = cameraCalibrators.length;
        markerDetectors = new MarkerDetector[n];
        // "unchecked" warning here
        allImagedBoardMarkers = new LinkedList[n];
        grayscaleImage = new IplImage[n];
        tempImage1 = new IplImage[n];
        tempImage2 = new IplImage[n];
        lastDetectedMarkers1 = new Marker[n][];
        lastDetectedMarkers2 = new Marker[n][];
        rmseBoardWarp = new double[n];
        rmseProjWarp = new double[n];
        boardWarp = new CvMat[n];
        projWarp = new CvMat[n];
        prevBoardWarp = new CvMat[n];
        lastBoardWarp = new CvMat[n];
        tempPts1 = new CvMat[n];
        tempPts2 = new CvMat[n];
        for (int i = 0; i < n; i++) {
            markerDetectors[i] = new MarkerDetector(detectorSettings);
            allImagedBoardMarkers[i] = new LinkedList<Marker[]>();
            grayscaleImage[i] = null;
            tempImage1[i] = null;
            tempImage2[i] = null;
            lastDetectedMarkers1[i] = null;
            lastDetectedMarkers2[i] = null;
            rmseBoardWarp[i] = Double.POSITIVE_INFINITY;
            rmseProjWarp[i] = Double.POSITIVE_INFINITY;
            boardWarp[i] = CvMat.create(3, 3);
            projWarp[i] = CvMat.create(3, 3);
            prevBoardWarp[i] = CvMat.create(3, 3);
            lastBoardWarp[i] = CvMat.create(3, 3);
            cxcore.cvSetIdentity(prevBoardWarp[i]);
            cxcore.cvSetIdentity(lastBoardWarp[i]);
            tempPts1[i] = CvMat.create(1, 4, cxcore.CV_64F, 2);
            tempPts2[i] = CvMat.create(1, 4, cxcore.CV_64F, 2);
        }
        this.projectorCalibrator = projectorCalibrator;

        this.boardWarpSrcPts = CvMat.create(1, 4, cxcore.CV_64F, 2);
        if (boardPlane != null) {
            int w = boardPlane.getImage().width;
            int h = boardPlane.getImage().height;
            boardWarpSrcPts.put(0.0, 0.0,  w, 0.0,  w, h,  0.0, h);
        }
        if (projectorPlane != null) {
            int w = projectorPlane.getImage().width;
            int h = projectorPlane.getImage().height;
            projectorCalibrator.getProjectiveDevice().imageWidth = w;
            projectorCalibrator.getProjectiveDevice().imageHeight = h;
        }
    }

    private final int
            MSB_IMAGE_SHIFT = 8,
            LSB_IMAGE_SHIFT = 7;

    public static class Settings extends GeometricCalibrator.Settings {
        double detectedProjectorMin = 0.5;
        boolean addOnlyIntersection = true;
        double prewarpUpdateErrorMax = 0.01;

        public double getDetectedProjectorMin() {
            return detectedProjectorMin;
        }
        public void setDetectedProjectorMin(double detectedProjectorMin) {
            this.detectedProjectorMin = detectedProjectorMin;
        }

        public boolean isAddOnlyIntersection() {
            return addOnlyIntersection;
        }
        public void setAddOnlyIntersection(boolean addOnlyIntersection) {
            this.addOnlyIntersection = addOnlyIntersection;
        }

        public double getPrewarpUpdateErrorMax() {
            return prewarpUpdateErrorMax;
        }
        public void setPrewarpUpdateErrorMax(double prewarpUpdateErrorMax) {
            this.prewarpUpdateErrorMax = prewarpUpdateErrorMax;
        }
    }

    private Settings settings;
    private MarkerDetector.Settings detectorSettings;

    // (possibly) multiple camera stuff in arrays
    private GeometricCalibrator[] cameraCalibrators;
    private MarkerDetector[] markerDetectors;
    // keep our own list of markers for the camera, since cameraCalibrators
    // might be used outside ProCamGeometricCalibrator as well...
    LinkedList<Marker[]>[] allImagedBoardMarkers;
    private IplImage[] grayscaleImage, tempImage1, tempImage2;
    private Marker[][] lastDetectedMarkers1, lastDetectedMarkers2;
    private double[] rmseBoardWarp, rmseProjWarp;
    private CvMat[] boardWarp, projWarp;
    private CvMat[] prevBoardWarp, lastBoardWarp;
    private CvMat[] tempPts1, tempPts2;

    // single board and projector stuff
    private boolean updatePrewarp = false;
    private final MarkedPlane boardPlane, projectorPlane;
    private final GeometricCalibrator projectorCalibrator;
    private final CvMat boardWarpSrcPts;

    public MarkedPlane getBoardPlane() {
        return boardPlane;
    }
    public MarkedPlane getProjectorPlane() {
        return projectorPlane;
    }
    public GeometricCalibrator[] getCameraCalibrators() {
        return cameraCalibrators;
    }
    public GeometricCalibrator getProjectorCalibrator() {
        return projectorCalibrator;
    }
    public int getImageCount() {
        int n = projectorCalibrator.getImageCount()/cameraCalibrators.length;
        for (GeometricCalibrator c : cameraCalibrators) {
            assert(c.getImageCount() == n);
        }
        return n;
    }

    public Marker[][] processCameraImage(IplImage cameraImage) {
        return processCameraImage(cameraImage, 0);
    }
    public Marker[][] processCameraImage(IplImage cameraImage, final int cameraNumber) {
        cameraCalibrators[cameraNumber].getProjectiveDevice().imageWidth = cameraImage.width;
        cameraCalibrators[cameraNumber].getProjectiveDevice().imageHeight = cameraImage.height;

        if (cameraImage.nChannels > 1) {
            if (grayscaleImage[cameraNumber] == null ||
                    grayscaleImage[cameraNumber].width  != cameraImage.width  ||
                    grayscaleImage[cameraNumber].height != cameraImage.height ||
                    grayscaleImage[cameraNumber].depth  != cameraImage.depth) {
                grayscaleImage[cameraNumber] = IplImage.create(cameraImage.width,
                        cameraImage.height, cameraImage.depth, 1, cameraImage.origin);
            }
            cv.cvCvtColor(cameraImage, grayscaleImage[cameraNumber], cv.CV_BGR2GRAY);
        } else {
            grayscaleImage[cameraNumber] = cameraImage;
        }

        final boolean boardWhiteMarkers = boardPlane.getForegroundColor().getMagnitude() >
                                          boardPlane.getBackgroundColor().getMagnitude();
        final boolean projWhiteMarkers = projectorPlane.getForegroundColor().getMagnitude() >
                                         projectorPlane.getBackgroundColor().getMagnitude();
        if (grayscaleImage[cameraNumber].depth > 8) {
            if (tempImage1[cameraNumber] == null ||
                    tempImage1[cameraNumber].width  != grayscaleImage[cameraNumber].width  ||
                    tempImage1[cameraNumber].height != grayscaleImage[cameraNumber].height) {
                tempImage1[cameraNumber] = IplImage.create(grayscaleImage[cameraNumber].width,
                        grayscaleImage[cameraNumber].height, cxcore.IPL_DEPTH_8U, 1,
                        grayscaleImage[cameraNumber].origin);
                tempImage2[cameraNumber] = IplImage.create(grayscaleImage[cameraNumber].width,
                        grayscaleImage[cameraNumber].height, cxcore.IPL_DEPTH_8U, 1,
                        grayscaleImage[cameraNumber].origin);
            }
            Parallel.run(new Runnable() { public void run() {
                cxcore.cvConvertScale(grayscaleImage[cameraNumber],
                        tempImage1[cameraNumber], 1.0/(1<<LSB_IMAGE_SHIFT), 0);
                lastDetectedMarkers1[cameraNumber] = cameraCalibrators[cameraNumber].
                        markerDetector.detect(tempImage1[cameraNumber], boardWhiteMarkers);
            }}, new Runnable() { public void run() {
                cxcore.cvConvertScale(grayscaleImage[cameraNumber],
                        tempImage2[cameraNumber], 1.0/(1<<MSB_IMAGE_SHIFT), 0);
                lastDetectedMarkers2[cameraNumber] = 
                        markerDetectors[cameraNumber].detect(tempImage2[cameraNumber], projWhiteMarkers);
            }});
        } else {
            Parallel.run(new Runnable() { public void run() {
                lastDetectedMarkers1[cameraNumber] = cameraCalibrators[cameraNumber].
                        markerDetector.detect(grayscaleImage[cameraNumber], boardWhiteMarkers);
            }}, new Runnable() { public void run() {
                lastDetectedMarkers2[cameraNumber] = 
                        markerDetectors[cameraNumber].detect(grayscaleImage[cameraNumber], projWhiteMarkers);
            }});
        }

        return processMarkers(cameraNumber) ? 
            new Marker[][] { lastDetectedMarkers1[cameraNumber],
                             lastDetectedMarkers2[cameraNumber] } : null;
    }

    public void drawMarkers(IplImage image) {
        drawMarkers(image, 0);
    }
    public void drawMarkers(IplImage image, int cameraNumber) {
        cameraCalibrators[cameraNumber].
                markerDetector.draw(image, lastDetectedMarkers1[cameraNumber]);
        projectorCalibrator.
                markerDetector.draw(image, lastDetectedMarkers2[cameraNumber]);
    }

    public boolean processMarkers() {
        return processMarkers(0);
    }
    public boolean processMarkers(int cameraNumber) {
        return processMarkers(lastDetectedMarkers1[cameraNumber],
                lastDetectedMarkers2[cameraNumber], cameraNumber);
    }
    public boolean processMarkers(Marker[] imagedBoardMarkers, Marker[] imagedProjectorMarkers) {
        return processMarkers(imagedBoardMarkers, imagedProjectorMarkers, 0);
    }
    public boolean processMarkers(Marker[] imagedBoardMarkers, 
            Marker[] imagedProjectorMarkers, int cameraNumber) {
        rmseBoardWarp[cameraNumber] = boardPlane    .getTotalWarp(imagedBoardMarkers,     boardWarp[cameraNumber]);
        rmseProjWarp [cameraNumber] = projectorPlane.getTotalWarp(imagedProjectorMarkers, projWarp[cameraNumber]);

        int size = (cameraCalibrators[cameraNumber].getProjectiveDevice().imageWidth+
                    cameraCalibrators[cameraNumber].getProjectiveDevice().imageHeight)/2;
        if (rmseBoardWarp[cameraNumber] <= settings.prewarpUpdateErrorMax*size &&
                rmseProjWarp[cameraNumber] <= settings.prewarpUpdateErrorMax*size) {
            updatePrewarp = true;
        } else {
            // not detected accurately enough..
            return false;
        }

        // First, check if we detected enough markers...
        if (imagedBoardMarkers    .length < boardPlane    .getPlaneMarkers().length*settings.detectedBoardMin ||
            imagedProjectorMarkers.length < projectorPlane.getPlaneMarkers().length*settings.detectedProjectorMin) {
                return false;
        }

        // then check by how much the corners of the calibration board moved
        cxcore.cvPerspectiveTransform  (boardWarpSrcPts,        tempPts1[cameraNumber], boardWarp[cameraNumber]);
        cxcore.cvPerspectiveTransform  (boardWarpSrcPts,        tempPts2[cameraNumber], prevBoardWarp[cameraNumber]);
        double rmsePrev = cxcore.cvNorm(tempPts1[cameraNumber], tempPts2[cameraNumber]);
        cxcore.cvPerspectiveTransform  (boardWarpSrcPts,        tempPts2[cameraNumber], lastBoardWarp[cameraNumber]);
        double rmseLast = cxcore.cvNorm(tempPts1[cameraNumber], tempPts2[cameraNumber]);

//System.out.println("rmsePrev = " + rmsePrev + " rmseLast = " + rmseLast + " cameraNumber = " + cameraNumber);
        // save boardWarp for next iteration...
        cxcore.cvCopy(boardWarp[cameraNumber], prevBoardWarp[cameraNumber]);

        // send upstream our recommendation for addition or not of these markers...
        if (rmsePrev < settings.patternSteadyMax*size && rmseLast > settings.patternMovedMin*size) {
            return true;
        } else {
            return false;
        }
    }

    public void addMarkers() {
        addMarkers(0);
    }
    public void addMarkers(int cameraNumber) {
        addMarkers(lastDetectedMarkers1[cameraNumber], lastDetectedMarkers2[cameraNumber], cameraNumber);
    }
    public void addMarkers(Marker[] imagedBoardMarkers, Marker[] imagedProjectorMarkers) {
        addMarkers(imagedBoardMarkers, imagedProjectorMarkers, 0);
    }
    public void addMarkers(Marker[] imagedBoardMarkers, 
            Marker[] imagedProjectorMarkers, int cameraNumber) {
        // just because we do not use it now, let's use it as temporary CvMat..
        CvMat tempWarp = lastBoardWarp[cameraNumber];

        if (!settings.addOnlyIntersection) {
            cameraCalibrators[cameraNumber].addMarkers(boardPlane.getPlaneMarkers(),
                    imagedBoardMarkers);
            allImagedBoardMarkers[cameraNumber].add(imagedBoardMarkers);
        } else {
            // deep cloning... warp board markers in projector plane
            Marker[] inProjectorBoardMarkers = new Marker[imagedBoardMarkers.length];
            for (int i = 0; i < inProjectorBoardMarkers.length; i++) {
                inProjectorBoardMarkers[i] = (Marker)imagedBoardMarkers[i].clone();
            }
            cxcore.cvInvert(projWarp[cameraNumber], tempWarp);
            Marker.applyWarp(inProjectorBoardMarkers, tempWarp);

            // only add markers that are within the projector plane as well
            int w = projectorPlane.getImage().width;
            int h = projectorPlane.getImage().height;
            Marker[] boardMarkersToAdd = new Marker[imagedBoardMarkers.length];
            int totalToAdd = 0;
            for (int i = 0; i < inProjectorBoardMarkers.length; i++) {
                double[] c = inProjectorBoardMarkers[i].corners;
                boolean outside = false;
                for (int j = 0; j < 4; j++) {
                    int margin = detectorSettings.subPixelWindow/2;
                    if (c[2*j  ] < margin || c[2*j  ] >= w-margin ||
                        c[2*j+1] < margin || c[2*j+1] >= h-margin) {
                            outside = true;
                            break;
                    }
                }
                if (!outside) {
                    boardMarkersToAdd[totalToAdd++] = imagedBoardMarkers[i];
                }
            }
            Marker[] a = Arrays.copyOf(boardMarkersToAdd, totalToAdd);
            cameraCalibrators[cameraNumber].addMarkers(boardPlane.getPlaneMarkers(), a);
            allImagedBoardMarkers[cameraNumber].add(a);
        }

        // deep cloning...
        Marker[] prewrappedProjMarkers = new Marker[projectorPlane.getPlaneMarkers().length];
        for (int i = 0; i < prewrappedProjMarkers.length; i++) {
            prewrappedProjMarkers[i] = (Marker)projectorPlane.getPlaneMarkers()[i].clone();
        }
        // prewarp points for the projectorCalibrator
        Marker.applyWarp(prewrappedProjMarkers, projectorPlane.getPrewarp());
        synchronized (projectorCalibrator) {
            // wait our turn to add markers orderly in the projector calibrator...
            while (projectorCalibrator.getImageCount()%cameraCalibrators.length < cameraNumber) {
                try {
                    projectorCalibrator.wait();
                } catch (InterruptedException ex) { }
            }
            projectorCalibrator.addMarkers(imagedProjectorMarkers, prewrappedProjMarkers);
            projectorCalibrator.notify();
        }

        // we added the detected markers, so save last computed warp too...
        cxcore.cvCopy(boardWarp[cameraNumber], lastBoardWarp[cameraNumber]);
    }

    public IplImage getProjectorImage() {
        if (updatePrewarp) {
            // find which camera has the smallest RMSE
            double minRmse = Double.MAX_VALUE;
            int minCameraNumber = 0;
            for (int i = 0; i < cameraCalibrators.length; i++) {
                double rmse = rmseBoardWarp[i]+rmseProjWarp[i];
                if (rmse < minRmse) {
                    minRmse = rmse;
                    minCameraNumber = i;
                }
            }

            // and use it to update the prewarp...
            CvMat prewarp = projectorPlane.getPrewarp();
            cxcore.cvInvert(          projWarp[minCameraNumber], prewarp);
            cxcore.cvMatMul(prewarp, boardWarp[minCameraNumber], prewarp);
            projectorPlane.setPrewarp(prewarp);
        }

        return projectorPlane.getImage();
    }

    public double[] calibrate(boolean useCenters, boolean calibrateCameras) {
        return calibrate(useCenters, calibrateCameras);
    }
    @SuppressWarnings("unchecked")
    public double[] calibrate(boolean useCenters, boolean calibrateCameras, int cameraAtOrigin) {
        GeometricCalibrator calibratorAtOrigin = cameraCalibrators[cameraAtOrigin];

        // calibrate camera if not already calibrated...
        if (calibrateCameras) {
            for (int cameraNumber = 0; cameraNumber < cameraCalibrators.length; cameraNumber++) {
                cameraCalibrators[cameraNumber].calibrate(useCenters);
                if (cameraCalibrators[cameraNumber] != calibratorAtOrigin) {
                    calibratorAtOrigin.calibrateStereo(useCenters, cameraCalibrators[cameraNumber]);
                }
            }
        }

        // remove distortion from corners of imaged markers for projector calibration
        // (in the case of the projector, markers imaged by the cameras, that is
        // those affected by their distortions, are the "object" markers, but
        // we need to remove this distortion, something we can do now that we
        // have calibrated the cameras...)
        LinkedList<Marker[]> allDistortedProjectorMarkers = projectorCalibrator.getAllObjectMarkers(),
                             distortedProjectorMarkersAtOrigin = new LinkedList<Marker[]>(),
                             allUndistortedProjectorMarkers = new LinkedList<Marker[]>(),
                             undistortedProjectorMarkersAtOrigin = new LinkedList<Marker[]>();
        Iterator<Marker[]> ip = allDistortedProjectorMarkers.iterator();
        // "unchecked" warning here
        Iterator<Marker[]>[] ib = new Iterator[cameraCalibrators.length];
        for (int cameraNumber = 0; cameraNumber < cameraCalibrators.length; cameraNumber++) {
            ib[cameraNumber] = allImagedBoardMarkers[cameraNumber].iterator();
        }

        // iterate over all the saved markers in the right order...
        // eew, this is getting ugly...
        while (ip.hasNext()) {
            for (int cameraNumber = 0; cameraNumber < cameraCalibrators.length; cameraNumber++) {
                double maxError = settings.prewarpUpdateErrorMax *
                        (cameraCalibrators[cameraNumber].getProjectiveDevice().imageWidth+
                         cameraCalibrators[cameraNumber].getProjectiveDevice().imageHeight)/2;

                Marker[] distortedBoardMarkers = ib[cameraNumber].next(),
                         distortedProjectorMarkers = ip.next(),
                         undistortedBoardMarkers = new Marker[distortedBoardMarkers.length],
                         undistortedProjectorMarkers = new Marker[distortedProjectorMarkers.length];

                // remove radial distortion from all points imaged by the camera
                for (int i = 0; i < distortedBoardMarkers.length; i++) {
                    Marker m = undistortedBoardMarkers[i] = (Marker)distortedBoardMarkers[i].clone();
                    m.corners = cameraCalibrators[cameraNumber].getProjectiveDevice().undistort(m.corners);
                }
                for (int i = 0; i < distortedProjectorMarkers.length; i++) {
                    Marker m = undistortedProjectorMarkers[i] = (Marker)distortedProjectorMarkers[i].clone();
                    m.corners = cameraCalibrators[cameraNumber].getProjectiveDevice().undistort(m.corners);
                }

                // remove linear distortion/warping of camera imaged markers from
                // the projector, to get their physical location on the board
                if (boardPlane.getTotalWarp(undistortedBoardMarkers, boardWarp[cameraNumber]) > maxError) {
                    assert(false);
                }
                cxcore.cvInvert(boardWarp[cameraNumber], boardWarp[cameraNumber]);
                Marker.applyWarp(undistortedProjectorMarkers, boardWarp[cameraNumber]);

                // tadam, we not have undistorted "object" corners for the projector..
                allUndistortedProjectorMarkers.add(undistortedProjectorMarkers);
                if (cameraCalibrators[cameraNumber] == calibratorAtOrigin) {
                    undistortedProjectorMarkersAtOrigin.add(undistortedProjectorMarkers);
                    distortedProjectorMarkersAtOrigin.add(distortedProjectorMarkers);
                } else {
                    undistortedProjectorMarkersAtOrigin.add(new Marker[0]);
                    distortedProjectorMarkersAtOrigin.add(new Marker[0]);
                }
            }
        }

        // calibrate projector
        projectorCalibrator.setAllObjectMarkers(allUndistortedProjectorMarkers);
        double avgProjReprojErr = projectorCalibrator.calibrate(useCenters);
        projectorCalibrator.getProjectiveDevice().nominalDistance =
                projectorCalibrator.getProjectiveDevice().getNominalDistance(boardPlane);

        // calibrate as a stereo pair (find rotation and translation)
        // let's use the projector markers only...
        LinkedList<Marker[]> om = calibratorAtOrigin.getAllObjectMarkers(),
                             im = calibratorAtOrigin.getAllImageMarkers();
        calibratorAtOrigin.setAllObjectMarkers(undistortedProjectorMarkersAtOrigin);
        calibratorAtOrigin.setAllImageMarkers(distortedProjectorMarkersAtOrigin);
        double avgStereoReprojErr = calibratorAtOrigin.calibrateStereo(useCenters, projectorCalibrator);

        // reset everything as it was before we started, so we get the same
        // result if called a second time..
        projectorCalibrator.setAllObjectMarkers(allDistortedProjectorMarkers);
        calibratorAtOrigin.setAllObjectMarkers(om);
        calibratorAtOrigin.setAllImageMarkers(im);

        return new double[] { avgProjReprojErr, avgStereoReprojErr };
    }

}
