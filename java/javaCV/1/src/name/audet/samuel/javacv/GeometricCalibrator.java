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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import name.audet.samuel.javacv.jna.cv;
import name.audet.samuel.javacv.jna.cxcore;
import name.audet.samuel.javacv.jna.cxcore.CvMat;
import name.audet.samuel.javacv.jna.cxcore.CvSize;
import name.audet.samuel.javacv.jna.cxcore.CvTermCriteria;
import name.audet.samuel.javacv.jna.cxcore.IplImage;

/**
 *
 * @author Samuel Audet
 */
public class GeometricCalibrator {
    public GeometricCalibrator(Settings settings, MarkerDetector.Settings detectorSettings,
            MarkedPlane markedPlane, ProjectiveDevice projectiveDevice) {
        this.settings = settings;
        this.markerDetector = new MarkerDetector(detectorSettings);
        this.markedPlane = markedPlane;
        this.projectiveDevice = projectiveDevice;

        cxcore.cvSetIdentity(prevWarp);
        cxcore.cvSetIdentity(lastWarp);

        if (markedPlane != null) {
            int w = markedPlane.getImage().width;
            int h = markedPlane.getImage().height;
            warpSrcPts.put(0.0, 0.0,  w, 0.0,  w, h,  0.0, h);
        }
    }

    public static class Settings extends BaseSettings {
        double detectedBoardMin = 0.5;
        double patternSteadyMax = 0.005;
        double patternMovedMin = 0.05;

        public double getDetectedBoardMin() {
            return detectedBoardMin;
        }
        public void setDetectedBoardMin(double detectedBoardMin) {
            this.detectedBoardMin = detectedBoardMin;
        }

        public double getPatternSteadyMax() {
            return patternSteadyMax;
        }
        public void setPatternSteadyMax(double patternSteadyMax) {
            this.patternSteadyMax = patternSteadyMax;
        }

        public double getPatternMovedMin() {
            return patternMovedMin;
        }
        public void setPatternMovedMin(double patternMovedMin) {
            this.patternMovedMin = patternMovedMin;
        }
    }

    private Settings settings;

    MarkerDetector markerDetector;
    private MarkedPlane markedPlane;
    private ProjectiveDevice projectiveDevice;
    private LinkedList<Marker[]> allObjectMarkers = new LinkedList<Marker[]>();
    private LinkedList<Marker[]> allImageMarkers  = new LinkedList<Marker[]>();
    private IplImage tempImage = null;
    private Marker[] lastDetectedMarkers = null;
    private CvMat warp = CvMat.create(3, 3);
    private CvMat prevWarp = CvMat.create(3, 3);
    private CvMat lastWarp = CvMat.create(3, 3);
    private CvMat warpSrcPts = CvMat.create(1, 4, cxcore.CV_64F, 2);
    private CvMat warpDstPts = CvMat.create(1, 4, cxcore.CV_64F, 2);
    private CvMat tempPts = CvMat.create(1, 4, cxcore.CV_64F, 2);

    public MarkerDetector getMarkerDetector() {
        return markerDetector;
    }
    public MarkedPlane getMarkedPlane() {
        return markedPlane;
    }
    public ProjectiveDevice getProjectiveDevice() {
        return projectiveDevice;
    }

    public LinkedList<Marker[]> getAllObjectMarkers() {
        return allObjectMarkers;
    }
    public void setAllObjectMarkers(LinkedList<Marker[]> allObjectMarkers) {
        this.allObjectMarkers = allObjectMarkers;
    }

    public LinkedList<Marker[]> getAllImageMarkers() {
        return allImageMarkers;
    }
    public void setAllImageMarkers(LinkedList<Marker[]> allImageMarkers) {
        this.allImageMarkers = allImageMarkers;
    }

    public Marker[] processImage(IplImage image) {
        projectiveDevice.imageWidth = image.width;
        projectiveDevice.imageHeight = image.height;

        final boolean whiteMarkers = markedPlane.getForegroundColor().getMagnitude() >
                                     markedPlane.getBackgroundColor().getMagnitude();
        if (image.depth > 8) {
            if (tempImage == null ||
                    tempImage.width     != image.width  ||
                    tempImage.height    != image.height) {
                tempImage = (IplImage)IplImage.create(image.width, image.height,
                        cxcore.IPL_DEPTH_8U, 1, image.origin);
            }
            cxcore.cvConvertScale(image, tempImage, 1.0/(1<<8), 0);
            lastDetectedMarkers = markerDetector.detect(tempImage, whiteMarkers);
        } else {
            lastDetectedMarkers = markerDetector.detect(image, whiteMarkers);
        }

        // First, check if we detected enough markers
        if (lastDetectedMarkers.length < markedPlane.getPlaneMarkers().length*settings.detectedBoardMin) {
            return null;
        }

        // then check by how much the corners of the calibration board moved
        markedPlane.getTotalWarp(lastDetectedMarkers, warp);
        cxcore.cvPerspectiveTransform  (warpSrcPts, warpDstPts, warp);
        cxcore.cvPerspectiveTransform  (warpSrcPts, tempPts,    prevWarp);
        double rmsePrev = cxcore.cvNorm(warpDstPts, tempPts);
        cxcore.cvPerspectiveTransform  (warpSrcPts, tempPts,    lastWarp);
        double rmseLast = cxcore.cvNorm(warpDstPts, tempPts);
//System.out.println("rmsePrev = " + rmsePrev + " rmseLast = " + rmseLast);
        // save warp for next iteration...
        cxcore.cvCopy(warp, prevWarp);

        // send upstream our recommendation for addition or not of these markers...
        int size = (image.width+image.height)/2;
        if (rmsePrev < settings.patternSteadyMax*size && rmseLast > settings.patternMovedMin*size) {
            return lastDetectedMarkers;
        } else {
            return null;
        }
    }

    public void drawMarkers(IplImage image) {
        markerDetector.draw(image, lastDetectedMarkers);
    }

    public void addMarkers() {
        addMarkers(markedPlane.getPlaneMarkers(), lastDetectedMarkers);
    }
    public void addMarkers(Marker[] imageMarkers) {
        addMarkers(markedPlane.getPlaneMarkers(), imageMarkers);
    }
    public void addMarkers(Marker[] objectMarkers, Marker[] imageMarkers) {
        // add only matching markers...
        int maxLength = Math.min(objectMarkers.length, imageMarkers.length);
        Marker[] om = new Marker[maxLength];
        Marker[] im = new Marker[maxLength];
        int i = 0;
        for (Marker m1 : objectMarkers) {
            for (Marker m2 : imageMarkers) {
                if (m1.id == m2.id) {
                    om[i] = m1;
                    im[i] = m2;
                    i++;
                    break;
                }
            }
        }
        if (i < maxLength) {
            om = Arrays.copyOf(om, i);
            im = Arrays.copyOf(im, i);
        }
        allObjectMarkers.add(om);
        allImageMarkers.add(im);

        // we added the detected markers, so save last computed warp too...
        cxcore.cvCopy(prevWarp, lastWarp);
    }

    public int getImageCount() {
        assert(allObjectMarkers.size() == allImageMarkers.size());
        return allObjectMarkers.size();
    }

    private CvMat[] getPoints(boolean useCenters) {
        // fill up pointCounts, objectPoints and imagePoints, with data from
        // srcMarkers and dstMarkers
        assert(allObjectMarkers.size() == allImageMarkers.size());
        Iterator<Marker[]> i1 = allObjectMarkers.iterator(),
                           i2 = allImageMarkers.iterator();
        CvMat pointCounts = CvMat.create(1, allImageMarkers.size(), cxcore.CV_32S, 1);
        IntBuffer pointCountsBuf = pointCounts.getIntBuffer();
        int totalPointCount = 0;
        while (i1.hasNext() && i2.hasNext()) {
            Marker[] m1 = i1.next(),
                     m2 = i2.next();
            assert(m1.length == m2.length);
            int n = m1.length*(useCenters ? 1 : 4);
            pointCountsBuf.put(n);
            totalPointCount += n;
        }
        i1 = allObjectMarkers.iterator();
        i2 = allImageMarkers.iterator();
        CvMat objectPoints = CvMat.create(1, totalPointCount, cxcore.CV_32F, 3);
        CvMat imagePoints  = CvMat.create(1, totalPointCount, cxcore.CV_32F, 2);
        FloatBuffer objectPointsBuf = objectPoints.getFloatBuffer();
        FloatBuffer imagePointsBuf  = imagePoints.getFloatBuffer();
        while (i1.hasNext() && i2.hasNext()) {
            Marker[] m1 = i1.next(),
                     m2 = i2.next();
            for (int j = 0; j < m1.length; j++) {
                if (useCenters) {
                    double[] c1 = m1[j].getCenter();
                    objectPointsBuf.put((float)c1[0]);
                    objectPointsBuf.put((float)c1[1]);
                    objectPointsBuf.put(0);

                    double[] c2 = m2[j].getCenter();
                    imagePointsBuf.put((float)c2[0]);
                    imagePointsBuf.put((float)c2[1]);
                } else { // use corners...
                    for (int k = 0; k < 4; k++) {
                        objectPointsBuf.put((float)m1[j].corners[2*k    ]);
                        objectPointsBuf.put((float)m1[j].corners[2*k + 1]);
                        objectPointsBuf.put(0);

                        imagePointsBuf.put((float)m2[j].corners[2*k    ]);
                        imagePointsBuf.put((float)m2[j].corners[2*k + 1]);
                    }
                }
            }
        }

        return new CvMat[] { objectPoints, imagePoints, pointCounts };
    }

    public static double computeReprojectionError(CvMat object_points,
            CvMat image_points, CvMat point_counts, CvMat camera_matrix,
            CvMat dist_coeffs, CvMat rot_vects, CvMat trans_vects,
            CvMat per_view_errors ) {
        CvMat image_points2 = CvMat.create(image_points.rows,
            image_points.cols, image_points.type);

        int i, image_count = rot_vects.rows, points_so_far = 0;
        double total_err = 0, err;

        for (i = 0; i < image_count; i++) {
            CvMat object_points_i = new CvMat(), 
                  image_points_i  = new CvMat(),
                  image_points2_i = new CvMat();
            int point_count = point_counts.getIntBuffer().get(i);
            CvMat rot_vect = new CvMat(), trans_vect = new CvMat();

            cxcore.cvGetCols(object_points, object_points_i,
                    points_so_far, points_so_far + point_count);
            cxcore.cvGetCols(image_points, image_points_i,
                    points_so_far, points_so_far + point_count);
            cxcore.cvGetCols(image_points2, image_points2_i,
                    points_so_far, points_so_far + point_count);
            points_so_far += point_count;

            cxcore.cvGetRows(rot_vects,   rot_vect,   i, i+1, 1);
            cxcore.cvGetRows(trans_vects, trans_vect, i, i+1, 1);

            cv.cvProjectPoints2(object_points_i, rot_vect, trans_vect,
                                camera_matrix, dist_coeffs, image_points2_i);
            err = cxcore.cvNorm(image_points_i, image_points2_i);
            err *= err;
            if (per_view_errors != null)
                per_view_errors.put(i, Math.sqrt(err/point_count));
            total_err += err;
        }

        return Math.sqrt(total_err/points_so_far);
    }

    public double calibrate(boolean useCenters) {
        ProjectiveDevice d = projectiveDevice;
        if (d.cameraMatrix == null) {
            d.cameraMatrix = CvMat.create(3, 3);
            cxcore.cvSetZero(d.cameraMatrix);
            if ((d.getSettings().flags & cv.CV_CALIB_FIX_ASPECT_RATIO) != 0) {
                d.cameraMatrix.put(0, d.getSettings().initAspectRatio);
                d.cameraMatrix.put(4, 1.);
            }
            cxcore.cvSetZero(d.distortionCoeffs);
        }

        CvMat rotVects = new CvMat(), transVects = new CvMat();
        d.extrParams = CvMat.create(allImageMarkers.size(), 6);
        cxcore.cvGetCols(d.extrParams, rotVects,   0, 3);
        cxcore.cvGetCols(d.extrParams, transVects, 3, 6);

        CvMat[] points = getPoints(useCenters);
        cv.cvCalibrateCamera2(points[0], points[1], points[2],
                new CvSize(d.imageWidth, d.imageHeight).byValue(),
                d.cameraMatrix, d.distortionCoeffs,
                rotVects, transVects, d.getSettings().flags);

        if (cxcore.cvCheckArr(d.cameraMatrix,     cxcore.CV_CHECK_QUIET, 0, 0) != 0 &&
            cxcore.cvCheckArr(d.distortionCoeffs, cxcore.CV_CHECK_QUIET, 0, 0) != 0 &&
            cxcore.cvCheckArr(d.extrParams,       cxcore.CV_CHECK_QUIET, 0, 0) != 0) {

            d.reprojErrs = CvMat.create(1, allImageMarkers.size());
            d.avgReprojErr = computeReprojectionError(points[0], points[1], points[2],
                    d.cameraMatrix, d.distortionCoeffs, rotVects, transVects, d.reprojErrs);

            d.nominalDistance = d.getNominalDistance(markedPlane);
        } else {
            d.avgReprojErr = -1;
            d.cameraMatrix = null;
        }

        return d.avgReprojErr;
    }

    public static double computeStereoError(CvMat imagePoints1, CvMat imagePoints2,
            CvMat M1, CvMat D1, CvMat M2, CvMat D2, CvMat F) {
        // CALIBRATION QUALITY CHECK
        // because the output fundamental matrix implicitly
        // includes all the output information,
        // we can check the quality of calibration using the
        // epipolar geometry constraint: m2^t*F*m1=0
        int N = imagePoints1.cols;
        CvMat L1 = CvMat.create(1, N, cxcore.CV_32F, 3);
        CvMat L2 = CvMat.create(1, N, cxcore.CV_32F, 3);
        //Always work in undistorted space
        cv.cvUndistortPoints(imagePoints1, imagePoints1, M1, D1, null, M1);
        cv.cvUndistortPoints(imagePoints2, imagePoints2, M2, D2, null, M2);
        //imagePoints1.put(d1.undistort(imagePoints1.get()));
        //imagePoints2.put(d2.undistort(imagePoints2.get()));
        cv.cvComputeCorrespondEpilines(imagePoints1, 1, F, L1);
        cv.cvComputeCorrespondEpilines(imagePoints2, 2, F, L2);
        double avgErr = 0;
        CvMat p1 = imagePoints1, p2 = imagePoints2;
        for(int i = 0; i < N; i++ ) {
            double e1 = p1.get(0, i, 0)*L2.get(0, i, 0) +
                        p1.get(0, i, 1)*L2.get(0, i, 1) + L2.get(0, i, 2);
            double e2 = p2.get(0, i, 0)*L1.get(0, i, 0) +
                        p2.get(0, i, 1)*L1.get(0, i, 1) + L1.get(0, i, 2);
            avgErr += e1*e1 + e2*e2;
       }
       return Math.sqrt(avgErr/N);
    }

    public double calibrateStereo(boolean useCenters, GeometricCalibrator peer) {
        ProjectiveDevice d = projectiveDevice;
        ProjectiveDevice dp = peer.projectiveDevice;

        CvMat[] points1 = getPoints(useCenters);
        CvMat[] points2 = peer.getPoints(useCenters);

        // find points in common from points1 and points2
        // since points1[0] and points2[0] might not be equal...
        FloatBuffer objPts1 = points1[0].getFloatBuffer();
        FloatBuffer imgPts1 = points1[1].getFloatBuffer();
        IntBuffer imgCount1 = points1[2].getIntBuffer();
        FloatBuffer objPts2 = points2[0].getFloatBuffer();
        FloatBuffer imgPts2 = points2[1].getFloatBuffer();
        IntBuffer imgCount2 = points2[2].getIntBuffer();
        assert(imgCount1.capacity() == imgCount2.capacity());

        CvMat objectPointsMat = CvMat.create(1, Math.min(objPts1.capacity(), objPts2.capacity()), cxcore.CV_32F, 3);
        CvMat imagePoints1Mat = CvMat.create(1, Math.min(imgPts1.capacity(), imgPts2.capacity()), cxcore.CV_32F, 2);
        CvMat imagePoints2Mat = CvMat.create(1, Math.min(imgPts1.capacity(), imgPts2.capacity()), cxcore.CV_32F, 2);
        CvMat pointCountsMat  = CvMat.create(1, imgCount1.capacity(), cxcore.CV_32S, 1);
        FloatBuffer objectPoints = objectPointsMat.getFloatBuffer();
        FloatBuffer imagePoints1 = imagePoints1Mat.getFloatBuffer();
        FloatBuffer imagePoints2 = imagePoints2Mat.getFloatBuffer();
        IntBuffer   pointCounts  = pointCountsMat .getIntBuffer();

        int end1 = 0, end2 = 0;
        for (int i = 0; i < imgCount1.capacity(); i++) {
            int start1 = end1;
            int start2 = end2;
            end1 = start1 + imgCount1.get(i);
            end2 = start2 + imgCount2.get(i);

            int count = 0;
            for (int j = start1; j < end1; j++) {
                float x1 = objPts1.get(j*3  );
                float y1 = objPts1.get(j*3+1);
                float z1 = objPts1.get(j*3+2);
                for (int k = start2; k < end2; k++) {
                    float x2 = objPts2.get(k*3  );
                    float y2 = objPts2.get(k*3+1);
                    float z2 = objPts2.get(k*3+2);
                    if (x1 == x2 && y1 == y2 && z1 == z2) {
                        objectPoints.put(x1);
                        objectPoints.put(y1);
                        objectPoints.put(z1);

                        imagePoints1.put(imgPts1.get(j*2));
                        imagePoints1.put(imgPts1.get(j*2+1));

                        imagePoints2.put(imgPts2.get(k*2));
                        imagePoints2.put(imgPts2.get(k*2+1));

                        count++;
                        break;
                    }
                }
            }
            if (count > 0) {
                pointCounts.put(count);
            }
        }
        objectPointsMat.cols = objectPoints.position()/3;
        imagePoints1Mat.cols = imagePoints1.position()/2;
        imagePoints2Mat.cols = imagePoints2.position()/2;
        pointCountsMat .cols = pointCounts.position();


        // place our ProjectiveDevice at the origin...
        d.R = CvMat.create(3, 3); d.R.put(1.0, 0.0, 0.0,  0.0, 1.0, 0.0,  0.0, 0.0, 1.0);
        d.T = CvMat.create(3, 1); d.T.put(0.0, 0.0, 0.0);
        d.E = CvMat.create(3, 3); cxcore.cvSetZero(d.E);
        d.F = CvMat.create(3, 3); cxcore.cvSetZero(d.F);

        dp.R = CvMat.create(3, 3);
        dp.T = CvMat.create(3, 1);
        dp.E = CvMat.create(3, 3);
        dp.F = CvMat.create(3, 3);

        cv.cvStereoCalibrate(objectPointsMat, imagePoints1Mat, imagePoints2Mat, pointCountsMat,
                d.cameraMatrix, d.distortionCoeffs, dp.cameraMatrix, dp.distortionCoeffs,
                new CvSize(d.imageWidth, d.imageHeight).byValue(), dp.R, dp.T, dp.E, dp.F,
                new CvTermCriteria(cxcore.CV_TERMCRIT_ITER+cxcore.CV_TERMCRIT_EPS,100,1e-6).byValue(),
                dp.getSettings().flags);

        // compute and return epipolar error...
        d.avgEpipolarErr = 0.0;
        return dp.avgEpipolarErr =
                computeStereoError(imagePoints1Mat, imagePoints2Mat,
                     d.cameraMatrix,  d.distortionCoeffs,
                    dp.cameraMatrix, dp.distortionCoeffs, dp.F);
    }

}
