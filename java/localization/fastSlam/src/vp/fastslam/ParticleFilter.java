package vp.fastslam;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import oursland.RandomSingleton;
import oursland.naming.UniqueNameGenerator;
import vp.mapping.LandmarkObservationSet;
import vp.model.ActionModel;
import vp.model.ControlModel;
import vp.robot.RobotPose;
import vp.sim.SensorImageGenerator;

public class ParticleFilter {
	private static final Random rand = RandomSingleton.instance;

	private LandmarkMap[] particles = new LandmarkMap[0];
	private LandmarkMap[] sortedParticles = new LandmarkMap[0];
	private double[] weightSums = new double[0];

	private SensorErrorModel sensorErrorModel;
	private double[] processError;
	private boolean useKnownDataAssociations = false;

	final private SensorImageGenerator sensorImageGen;

	public ParticleFilter( int particleCount, SensorErrorModel sensorErrorModel, double[] processError, Rectangle2D mapBounds, SensorImageGenerator sensorImageGen ) {
		this.particles = new LandmarkMap[particleCount];
		for (int i = 0; i < particles.length; i++) {
			particles[i] = new LandmarkMap(mapBounds);
		}
		this.weightSums = new double[particleCount + 1];
		this.sensorImageGen = sensorImageGen;
		this.sensorErrorModel = sensorErrorModel;
		this.processError = processError;
		normalizeWeights();
		sortParticles();
	}
	
	public void update(LandmarkObservationSet observations, ControlModel control, ActionModel action, UniqueNameGenerator lmGen) {
		// localLandmarks are with respect to the robot
		LandmarkMap[] next = new LandmarkMap[size()];
		for (int i = 0; i < next.length; i++) {
			// select with a preference for better paths 
			double rsel;
			next[i] = selectParticleAndCopy();
			double dt = action.getElapsedTime();
//			SimpleActionModel prevAction = next[i].getPreviousAction(dt);
			double lastWeight = next[i].getImportanceWeight();
			RobotPose currentPose = next[i].getLatestPose();
			action.selectActionHypothesis(rand);
			// TODO: should combine the prevAction and the supplied action (and maybe include the association action)
//			ActionModel averageAction = prevAction.average(action);
			RobotPose nextPose = control.nextPose(rand, currentPose, action);
			next[i].updateMap(
				nextPose,
				observations,
				sensorImageGen,
				dt,
				sensorErrorModel,
				processError,
//				assoc,
//				useKnownDataAssociations,
				lmGen);
		}

		// reduce memory use in the old maps
		for (int i = 0; i < particles.length; i++) {
			particles[i].pack();
		}

		particles = next;

		// normalize the particle weights
		normalizeWeights();
		sortParticles();
	}

	public LandmarkMap getBestMap() {
		LandmarkMap rc = particles[0];
		double best = rc.getImportanceWeight();
		for (int i = 1; i < particles.length; i++) {
			double currentWeight = particles[i].getImportanceWeight();
			if (currentWeight > best) {
				rc = particles[i];
				best = currentWeight;
			}
		}
		return rc;
	}

//	private boolean validateParticleOrder() {
//		double lastWeight = Double.POSITIVE_INFINITY;
//		for (int i = 0; i < particles.length; i++) {
//			final double currentWeight = particles[i].getImportanceWeight();
//			if (currentWeight > lastWeight) {
//				System.out.println("not in best order at " + i);
//				return false;
//			}
//			lastWeight = currentWeight;
//		}
//		return true;
//	}

	public void paintParticles(Graphics2D g2, LandmarkMap currentBestMap) {
		final int incr = Math.max(1, particles.length / 50);
//		final int incr = 1;
		for (int i = 0; i < particles.length; i += incr) {
//			double w = particles[i].getImportanceWeight();
//			if( rand.nextDouble() < w ) {
				particles[i].paintSmallRobot(g2);
//			}
		}
	}

//	public double[] getSensorError() {
//		return sensorError;
//	}

	public double[] getProcessError() {
		return processError;
	}

	public int size() {
		return particles.length;
	}
	
	public LandmarkMap getMap(int i) {
		return particles[i];
	}

	public LandmarkMap getSortedMap(int i) {
		return sortedParticles[i];
	}

	public boolean isUseKnownDataAssociations() {
		return useKnownDataAssociations;
	}

	public void setUseKnownDataAssociations(boolean b) {
		useKnownDataAssociations = b;
	}

	private LandmarkMap selectParticleAndCopy() {
		double sel = rand.nextDouble();
		int index = Arrays.binarySearch(weightSums, sel);
		if (index < 0) {
			index = - (index + 1);
		}
		if (index > 0) {
			index--;
		}
		return new LandmarkMap(particles[index]);
	}

	private double calculateSums() {
		double totalSum = 0.0;
		for (int i = 0; i < this.particles.length; i++) {
			totalSum += this.particles[i].getImportanceWeight();
		}
		return totalSum;
	}

	private void normalizeWeights() {
		convertLogWeights();
		double sum = calculateSums();
		if (sum == 0.0) {
			// sum of weights is zero. Treat all particles equally.
			System.out.println("Sum of weights is zero. Treat all particles equally.");
			for (int i = 0; i < this.particles.length; i++) {
				this.particles[i].setImportanceWeight(1.0);
			}
			normalizeWeights();
		}
		for (int i = 0; i < this.particles.length; i++) {
			this.particles[i].normalizeImportanceWeight(sum);
		}
		calculateFilterArray();
	}
	
	private void convertLogWeights() {
		scaleLogWeights();
		for (int i = 0; i < this.particles.length; i++) {
			this.particles[i].convertLogImportance();
		}
	}
	
	private void scaleLogWeights() {
		double max = maxLogWeight();
		for (int i = 0; i < this.particles.length; i++) {
			this.particles[i].scaleLogImportanceWeight(-max);
		}
	}
	
	private double maxLogWeight() {
		double maxLogWeight = -Double.MAX_VALUE;
		for (int i = 0; i < this.particles.length; i++) {
			maxLogWeight = Math.max(maxLogWeight, this.particles[i].getLogImportanceWeight());
		}
		return maxLogWeight;
	}
	
	private void calculateFilterArray() {
		this.weightSums[0] = 0.0;
		for (int i = 0; i < this.particles.length; i++) {
			double value = this.particles[i].getImportanceWeight() / this.particles[i].getLastImportanceWeight();
			this.weightSums[i + 1] = this.weightSums[i] + value;
		}
		final double total = this.weightSums[this.weightSums.length-1];
		for (int i = 0; i < this.weightSums.length; i++) {
			this.weightSums[i] /= total;
		}
	}

	private static final Comparator mapFitness = new Comparator() {
		/** 
		 * Fitness is an approximation of p(z|x,z,m)
		 * Larger values are better.
		 * Values are normalized and selection using monte-carlo selection.
		 */

		public int compare(Object o1, Object o2) {
			LandmarkMap map1 = (LandmarkMap) o1;
			LandmarkMap map2 = (LandmarkMap) o2;
			if (map1.getImportanceWeight() < map2.getImportanceWeight()) {
				return -1;
			} else if (map1.getImportanceWeight() > map2.getImportanceWeight()) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	private void sortParticles() {
		sortedParticles = new LandmarkMap[particles.length];
		System.arraycopy(particles, 0, sortedParticles, 0, particles.length);
//		sortedParticles = (LandmarkMap[])particles.clone();
		Arrays.sort(sortedParticles, particleFitnessComparator);
	}
	
	private static final Comparator<LandmarkMap> particleFitnessComparator = new Comparator<LandmarkMap>() {
		public int compare(LandmarkMap o1, LandmarkMap o2) {
			double w1 = o1.getImportanceWeight();
			double w2 = o2.getImportanceWeight();
			if( w1 == w2 ) return 0;
			return (w1 < w2)?1:-1;
		}
		
	};

	public SensorErrorModel getSensorErrorModel() {
		return sensorErrorModel;
	}
}
