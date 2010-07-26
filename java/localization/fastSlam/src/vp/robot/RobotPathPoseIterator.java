package vp.robot;


public class RobotPathPoseIterator extends RobotPathIterator {

	public RobotPathPoseIterator(RobotPath path) {
		super(path);
	}

	public Object next() {
		return ((RobotPath)super.next()).getLatestPose();
	}
}
