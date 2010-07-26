package vp.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import oursland.naming.UniqueName;
import oursland.naming.UniqueNameGenerator;
import vp.VPConstant;
import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.model.SimpleActionModel;

public class RobotPath {
	private RobotPose current;
	private RobotPath previous;
	private LandmarkObservationSet landmarks;	// landmarks are stored in a local coordinate frame
	private double error;
	
	public RobotPath(LandmarkObservationSet lms, RobotPose current) {
		this(lms, current, null);
	}
	
	public RobotPath(LandmarkObservationSet lms, RobotPose current, RobotPath previous) {
		this.current = current;
		this.previous = previous;
		this.landmarks = lms; //lms.transform(current.getTransform());
	}
	
	public static RobotPath read(BufferedReader in, UniqueNameGenerator lmGen) throws IOException {
		return read(in, lmGen, true);
	}
	
	public void pack() {
		landmarks.pack();
	}
	
	public static RobotPath read(BufferedReader in, UniqueNameGenerator lmGen, boolean flipCoordinates) throws IOException {
		RobotPath rc = null;
		int lineNumber = 0;
		String line;
		while( (line=in.readLine()) != null) {
			try {
				StringTokenizer st = new StringTokenizer(line);
				double time = Double.parseDouble(st.nextToken().trim());
				// TODO: Why do the x,y coordinates have to be switched?
				double x  = Double.parseDouble(st.nextToken().trim());
				double y  = Double.parseDouble(st.nextToken().trim());
				double theta  = Double.parseDouble(st.nextToken().trim());
				int landmarkCount = Integer.parseInt(st.nextToken().trim());
				LandmarkObservation[] lma = new LandmarkObservation[landmarkCount];
				for( int i = 0; i < landmarkCount; i++) {
					UniqueName name = null;
					double lmx;
					String firstToken = st.nextToken();
					try {
						lmx = Double.parseDouble(firstToken);
					} catch( NumberFormatException e) {
						name = lmGen.getName(firstToken);
						lmx = Double.parseDouble(st.nextToken().trim());
					}
					double lmy = Double.parseDouble(st.nextToken().trim());
					// TODO: We are currently rotating the landmarks -90 degrees for an updated coordinate system.
					// We need to stop the conversion and change the files at some point.
					if( flipCoordinates ) {
						lma[i] = new LandmarkObservation(lmy, -lmx, name);					
					} else {
						lma[i] = new LandmarkObservation(lmx, lmy, name);					
					}
				}
				if( flipCoordinates ) {
					rc = new RobotPath(new LandmarkObservationSet(lma, time), new RobotPose(y,-x,theta), rc);
				} else {
					rc = new RobotPath(new LandmarkObservationSet(lma, time), new RobotPose(x,y,theta), rc);
				}
				lineNumber++;
			} catch (NoSuchElementException e) {
				System.out.println("Badly formatted line " + lineNumber + ": " + line);
				throw new IOException();
			} catch (NumberFormatException e) {
				System.out.println("Badly formatted line " + lineNumber + ": " + line);
				throw new IOException();
			}
		}
		return rc;
	}

	public void write(PrintWriter out) throws IOException  {
		LinkedList<RobotPath> list = new LinkedList<RobotPath>();
		RobotPath temp = this;
		while(temp != null) {
			list.addFirst(temp);
			temp = temp.previous;
		}
		while(list.size() > 0) {
			RobotPath p = list.removeFirst();
			out.print(
				p.landmarks.getTime() + " " +
				p.current.getX() + " " +  
				p.current.getY() + " " +  
				p.current.getTheta() + " " +
				p.landmarks.size() + " "
			);
			for (Iterator iter = p.landmarks.iterator(); iter.hasNext();) {
				LandmarkObservation lm = (LandmarkObservation) iter.next();
				UniqueName name = lm.getAssociationName();
				if( name != null ) {
					out.print(name + " ");
				}
				out.print(lm.getX() + " " + lm.getY() + " ");
			}
			out.println();
		}
	}

	public void write(PrintWriter out, NumberFormat nf, boolean telemetry) {
		LinkedList<RobotPath> list = new LinkedList<RobotPath>();
		RobotPath temp = this;
		while(temp != null) {
			list.addFirst(temp);
			temp = temp.previous;
		}
		while(list.size() > 0) {
			RobotPath p = list.removeFirst();
			out.print(
				nf.format(p.landmarks.getTime()) + " " +
				(telemetry?nf.format(p.current.getX()):"0") + " " +  
				(telemetry?nf.format(p.current.getY()):"0") + " " +  
				(telemetry?nf.format(p.current.getTheta()):"0") + " " +
				p.landmarks.size() + "\t"
			);
			for (Iterator iter = p.landmarks.iterator(); iter.hasNext();) {
				LandmarkObservation lm = (LandmarkObservation) iter.next();
				if( lm.getAssociationName() != null ) {
					out.print(lm.getAssociationName() + " ");
				}
				out.print(nf.format(lm.getX()) + " " + nf.format(lm.getY()) + " ");
			}
			out.println();
		}
	}
	
	public void setError(double error) {
		if( previous != null ) {
			this.error = error + 0.9*previous.getError();
		} else {
			this.error = error;
		}
//		System.out.println("setting error to " + this.error);
	}
	
	public double getError() {
		return this.error;
	}
	
	public void paint(Graphics2D g) {
		g.setColor(new Color(0.0f, 0.0f, 0.0f, 1.0f));
		current.paint(g, VPConstant.scaleDown);
	}
	
	public void paintPath(Graphics2D g, int length, float decay) {
		Point2D last = current.getLocation();
		RobotPath next = previous;
		float alpha = 1.0f;
		Stroke oldStroke = g.getStroke();
		g.setStroke(VPConstant.basicStroke);
		for( int i = 0; i < length; i++) {
			if( next != null ) {
				alpha *= decay;
				g.setColor(new Color(0.0f, 0.0f, 0.0f, alpha));
				g.draw(new Line2D.Double(last, next.current.getLocation()));
				last = next.current.getLocation();
//				next.current.paint(g);
//				next.getGlobalLandmarkSet().write(System.out);
				next = next.previous;					
			}
		}
		g.setStroke(oldStroke);
	}
	
	public void paintLandmarks(Graphics2D g, int length, Color fillColor, Color borderColor, float alpha, float decay) {
		if( length > 0 ) {
			if( previous != null ) {
				previous.paintLandmarks(g, length-1, fillColor, fillColor, alpha*decay, decay);
			}
			Color fillColor2 = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int)(255*alpha));
			Color borderColor2 = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), (int)(255*alpha));
			getGlobalLandmarkSet().paint(g, fillColor2, borderColor2);
		}
	}

	public RobotPose getLatestPose() {
		return current;
	}

//	public void error() {
//		double error = 0.0;
//		error = getPreviousError(getGlobalLandmarkSet(), previous, 4, 0.5);
//		setError(error);
//	}
	
//	private static double getPreviousError(LandmarkSet first, RobotPath prev, int count, double decay) {
//		double rc = first.getImportanceWeight(prev.getGlobalLandmarkSet());
//		if( prev.previous != null && count > 0 ) {
//			return rc + decay * getPreviousError(first, prev.previous, count-1, decay);
//		} else {
//			return rc;
//	}	}

	public RobotPath previousPath() {
		return previous;
	}

	public SimpleActionModel getPreviousAction() {
		if( previous == null ) {
			return new SimpleActionModel(0,0,0,0);
		} else if( current == null || previous.current == null ) {
			double dt = landmarks.getTime() - previous.landmarks.getTime();
			return new SimpleActionModel(0,0,0,dt);
		} else {
			double dt = landmarks.getTime() - previous.landmarks.getTime();
			// action is in global coordinate system
			return current.getActionFrom(previous.current, dt);
	}	}

	public LandmarkObservationSet getLandmarkObservations() {
		return landmarks;
	}
	
	public LandmarkObservationSet getGlobalLandmarkSet() {
		if( current == null) {
			return landmarks;
		}
		return landmarks.transform(current.getTransform());	
	}

	public final RobotPose[] getPoseArray() {
		ArrayList<RobotPose> poseList = new ArrayList<RobotPose>();
		RobotPath current = this;
		while(current != null) {
			 poseList.add(current.getLatestPose());
			 current = current.previous;
		}
		RobotPose[] rc = new RobotPose[poseList.size()];
		poseList.toArray(rc);
		return rc;
	}

	public Iterator iterator() {
		return new RobotPathIterator(this);
	}

	public void removeDataAssociation() {
		landmarks.removeDataAssociation();
		if( previous != null ) {
			previous.removeDataAssociation();
		}
	}

	public void adjustPose(double dx, double dy, double dt) {
		current = new RobotPose(current.getX()+dx, current.getY()+dy, current.getTheta()+dt);
	}

	public void addNoiseToObservations(int var) {
		landmarks = landmarks.getNoisySensors(var);
	}
}
