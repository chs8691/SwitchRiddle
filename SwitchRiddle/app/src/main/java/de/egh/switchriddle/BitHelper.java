package de.egh.switchriddle;

/** Helper class for bit operations. */
public class BitHelper {

	static int flipBit(final int n, final int pos) {
		return n ^ (1 << pos);
	}

	static int setBit(final int n, final int pos) {
		return n | (1 << pos);
	}

	static boolean testBit(final int n, final int pos) {
		final int mask = 1 << pos;

		return (n & mask) == mask;
		// alternativ: return (n & 1<<pos) != 0;
	}

	private int bitMask;
	private final int size;

	/** Create BitHelper for a bit array with the give size. */
	public BitHelper(final int size) {
		this.size = size;

		for (int i = 0; i < size; i++) {
			bitMask = setBit(bitMask, i);
		}

	}

	/** Normalize bit array to its lowest integer value */
	public int normalize(final int bits) {

		int lowestInteger = bits;
		int actInteger = bits;
		for (int i = 0; i < size - 1; i++) {
			actInteger = rotateLeft(actInteger);
			if (actInteger < lowestInteger)
				lowestInteger = actInteger;
		}
		return lowestInteger;
	}

	int rotateLeft(final int bits) {
		return (bits >>> 6) | (bits << 1) & bitMask;
	}

	String toString(final int bits) {

		String res = "";
		for (int i = 0; i < size; i++) {
			res = (testBit(bits, i) == true ? "1" : "0") + res;
		}

		return res;
	}
}
