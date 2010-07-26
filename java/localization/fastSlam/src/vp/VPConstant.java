package vp;

import java.awt.BasicStroke;

public final class VPConstant {
	public static final int imageHeight = 500;
	public static final int imageWidth = 800;
	public static final double robotImageX = VPConstant.imageWidth / 2.0;
	public static final double robotImageY = VPConstant.imageHeight / 10.0;
	public static final double scaleDown = 12.0; // cm/pixel
	public static final BasicStroke basicStroke = new BasicStroke((float)VPConstant.scaleDown);
	//	private static final double scale = 2.0;

	private VPConstant() {
		super();
	}
}
