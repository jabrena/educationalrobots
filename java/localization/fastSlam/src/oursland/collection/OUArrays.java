package oursland.collection;

public final class OUArrays {
	private OUArrays() {
	}

	public static boolean[] growArray(boolean[] a, int size) {
		if(a.length <= size) {
			boolean[] temp = new boolean[Math.max(2 * a.length, size + 1)];
			System.arraycopy(a, 0, temp, 0, a.length);
			a = temp;
		}
		return a;
	}
}