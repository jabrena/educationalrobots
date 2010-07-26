package vp.data;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.TreeSet;

import oursland.naming.UniqueNameGenerator;
import vp.mapping.LandmarkObservation;
import vp.mapping.LandmarkObservationSet;
import vp.robot.RobotPath;

public class SimplifyData {
	public static void main(String[] args) {
		UniqueNameGenerator lmGen = new UniqueNameGenerator("lm");
		RobotPath path = new RobotPath(null, null);
		try {
			BufferedReader pathFile = new BufferedReader(new FileReader(args[0]));
			path = RobotPath.read(pathFile, lmGen);
			pathFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RobotPath newPath = processPath(path);
//		RobotPath newPath = path;
		
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(args[1]));
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(3);
			df.setMinimumFractionDigits(3);
			df.setGroupingUsed(false);
			newPath.write(out, df, false);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done");
	}
	
	private static RobotPath processPath(RobotPath path) {
		RobotPath rc = null;
		if( path.previousPath() != null ) {
			rc = processPath(path.previousPath());
		}
		LandmarkObservationSet lms = path.getLandmarkObservations();
		TreeSet<LandmarkObservation> closest = new TreeSet<LandmarkObservation>(new LandmarkComparator());
		for(int i = 0; i < lms.size(); i++ ) {
			closest.add(lms.get(i));
		}
		final int maxCount = 20;
		while( closest.size() > maxCount ) closest.remove(closest.last());
		LandmarkObservation[] newArray = new LandmarkObservation[Math.min(maxCount, closest.size())];
		closest.toArray(newArray);
		LandmarkObservationSet newSet = new LandmarkObservationSet(newArray, lms.getTime());
		rc = new RobotPath(newSet, path.getLatestPose(), rc);
		return rc;
	}
	
	private static class LandmarkComparator implements Comparator<LandmarkObservation> {

		public int compare(LandmarkObservation o1, LandmarkObservation o2) {
			double d1 = distance2(o1.getX(), o1.getY());
			double d2 = distance2(o2.getX(), o2.getY());
			if( d1 < d2 ) return -1;
			else if( d1 > d2 ) return 1;
			else return 0;
		}
		
		private static final double distance2(double x, double y) {
			return x*x + y*y;
		}
	}
}
