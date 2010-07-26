package vp.mapping;

public interface GenericLandmark extends Cloneable {
	public double getX();
	public double getY();
//	public void addUpdateListener(LandmarkListener l);
//	public void removeUpdateListener(LandmarkListener l);
	public GenericLandmark clone();
}
