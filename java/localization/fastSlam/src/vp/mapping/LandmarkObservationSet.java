package vp.mapping;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;

import oursland.RandomSingleton;
import oursland.math.UsefulConstants;
import oursland.naming.UniqueName;
import oursland.naming.UniqueNameGenerator;
import vp.robot.RangeFinderData;

public class LandmarkObservationSet implements Cloneable {
	private List<LandmarkObservation> landmarks = new ArrayList<LandmarkObservation>();
	private double[][] distanceMatrix = new double[0][0];
	private double time;

	public LandmarkObservationSet(LandmarkObservationSet copy) {
		this.landmarks = new LinkedList<LandmarkObservation>();
		for( Iterator iter = copy.landmarks.iterator(); iter.hasNext(); ) {
			LandmarkObservation lm = (LandmarkObservation)iter.next();
			this.landmarks.add(new LandmarkObservation(lm));
		}
	}

	public LandmarkObservationSet() {
		this(new LandmarkObservation[0], 0);
	}

	public LandmarkObservationSet(LandmarkObservation[] set, double time) {
		this.landmarks.addAll(Arrays.asList(set));
		this.time = time;
	}
	
	public void pack() {
		distanceMatrix = null;
	}
	
	protected Object clone() {
		try {
			LandmarkObservationSet rc = (LandmarkObservationSet) super.clone();
			LandmarkObservation[] lma = new LandmarkObservation[landmarks.size()];
			landmarks.toArray(lma);
			for (int i = 0; i < lma.length; i++) {
				lma[i] = lma[i].clone();
			}
			rc.landmarks = new LinkedList<LandmarkObservation>(Arrays.asList(lma));
			return rc;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new Error(e);
		}
	}

	public int size() {
		return landmarks.size();
	}

//	public double getImportanceWeight( LandmarkSet otherSet ) {
//		double rc = 0.0;
//		
//		Iterator iter = landmarks.iterator();
//		while(iter.hasNext()) {
//			LandmarkObservation lm1 = (LandmarkObservation) iter.next();
//			double err = lm1.associate(otherSet);
////			Landmark lm2 = otherSet.closestLandmark(lm1);
////			double err = lm1.distance(lm2);
//			if( err < 5000 )
//				rc += err;	// assume this landmark does not have a match
//		}
//		
//		return rc;
//	}
	
	public void write(PrintStream out) {
		for (Iterator iter = landmarks.iterator(); iter.hasNext();) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			lm.write(out);
//			out.print("(" + lm.getX() + ", " + lm.getY() + ")\t");
		}
		out.println();
	}


	public LandmarkObservation closestLandmark(LandmarkObservation lm1) {
		LandmarkObservation rc = null;
		double closestDistance = Double.MAX_VALUE;
		Iterator iter = landmarks.iterator();
		while(iter.hasNext()) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			double checkDistance = lm1.distanceSquare(lm);
			if( checkDistance < closestDistance ) {
				rc = lm;
				closestDistance = checkDistance;
			}
		}
		return rc;
	}


//	public int removeDeadLandmarks(double threshold ) {
//		int rc = 0;
//		Iterator iter = landmarks.iterator();
//		while( iter.hasNext() ) {
//			Landmark lm = (Landmark) iter.next();
//			if( lm.deadAge > threshold ) {
//				iter.remove();
//				rc++;
//			}
//		}
//		return rc;
//	}
	
	public LandmarkObservationSet transform(AffineTransform xform) {
		LandmarkObservation[] nla = new LandmarkObservation[size()];
		int i = 0;
		Iterator iter = landmarks.iterator();
		while( iter.hasNext() ) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			nla[i++] = lm.transform(xform);
		}
		return new LandmarkObservationSet(nla, time);
	}

	public void paint(Graphics2D g, Color fillColor, Color borderColor) {
		Iterator iter = landmarks.iterator();
		while( iter.hasNext() ) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			lm.paint(g, fillColor, borderColor);
		}
	}

	public void paint(Graphics2D g, Color fillColor, Color borderColor, AffineTransform xform) {
		Iterator iter = landmarks.iterator();
		while( iter.hasNext() ) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			lm.paint(g, fillColor, borderColor, xform);
		}
	}
	
	public double getTime() {
		return time;
	}
	
	public Iterator iterator() {
		return landmarks.iterator();
	}

	public void read(BufferedReader lmIn, UniqueNameGenerator lmGen) throws IOException {
		String str = lmIn.readLine();
		while(str != null) {
			try {
				StringTokenizer st = new StringTokenizer(str);
				UniqueName name = null;
				double x;
				String firstToken = st.nextToken();
				try {
					x = Double.parseDouble(firstToken);
				} catch( NumberFormatException e) {
					name = lmGen.getName(firstToken);
					x = Double.parseDouble(st.nextToken());
				}
				double y = Double.parseDouble(st.nextToken());
				landmarks.add(new LandmarkObservation(x,y, name));
			} catch (NoSuchElementException e) {
				// do nothing -- just ignore the line
			} catch (NumberFormatException e) {
				// do nothing -- just ignore the line
			}
			str = lmIn.readLine();
		}
	}
	
	public LandmarkObservation get(int i) {
		return landmarks.get(i);
	}

	public LandmarkObservation get(UniqueName name) {
		for (Iterator iter = landmarks.iterator(); iter.hasNext();) {
			LandmarkObservation element = (LandmarkObservation) iter.next();
			if( element.getAssociationName() == name ) {
				return element;
			}
		}
		throw new NoSuchElementException();
	}

	public void removeDataAssociation() {
		Iterator iter = landmarks.iterator();
		while( iter.hasNext() ) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			lm.removeDataAssociation();
		}
	}

	public LandmarkObservation[] toArray() {
		LandmarkObservation[] rc = new LandmarkObservation[landmarks.size()];
		landmarks.toArray(rc);
		return rc;
	}
	
	public double getDistanceSq(int i, int j) {
		final int landmarkCount = landmarks.size();
		if( distanceMatrix.length != landmarkCount ) {
			distanceMatrix = new double[landmarkCount][landmarkCount];
		}
		if( distanceMatrix[i][j] == 0 ) {
			LandmarkObservation lm1 = get(i);
			LandmarkObservation lm2 = get(j);
			distanceMatrix[i][j] = lm1.distanceSquare(lm2);
			distanceMatrix[j][i] = distanceMatrix[i][j];
		}
		return distanceMatrix[i][j];
	}

	public LandmarkObservationSet getNoisySensors(double var) {
		Random r = RandomSingleton.instance;
		LandmarkObservation[] rc = new LandmarkObservation[size()];
		for (int i = 0; i < rc.length; i++) {
			LandmarkObservation lm = get(i);
			rc[i] = new LandmarkObservation(lm.getX()+var*r.nextGaussian(), lm.getY()+var*r.nextGaussian(), lm.getAssociationName());
		}
		return new LandmarkObservationSet(rc, getTime());
	}

	public RangeFinderData getRangeFinderData(final int oversampleCount, final double variance) {
		RangeFinderData observed = new RangeFinderData(oversampleCount, -UsefulConstants.PIOVERTWO, UsefulConstants.PIOVERTWO, 2600);
		for (Iterator iter = this.iterator(); iter.hasNext();) {
			LandmarkObservation lm = (LandmarkObservation) iter.next();
			observed.addLandmarkReading(lm.getAngle(), lm.getDistance(), variance);
		}
		return observed;
	}
}
