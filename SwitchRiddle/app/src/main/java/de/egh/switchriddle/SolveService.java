package de.egh.switchriddle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Solves and count the number of steps for a give game state. A game state is
 * hold as bit series in an integer.
 */
public class SolveService {
	/** Returns the result. */
	public interface ResultListener {
		/** Returns the result for the last call of <code>solve()</code>. */
		public void onResult(short numberOfSteps);
	}

	/** Param: bits (int), Progress: actual result (short) and result (short) */
	private class Task extends AsyncTask<Integer, Integer, Short> {
		static final short RES_NOT_SOLVED = Short.MAX_VALUE;

		int gameBits = 0x0;

		@Override
		protected Short doInBackground(final Integer... params) {
			// State of the game
			gameBits = params[0];

			// Precondition: Game not solved
			if (gameBits == solutionBits)
				return 0;

			// call recursive
			return doIt(bitHelper.normalize(params[0]), (short) 0,
					RES_NOT_SOLVED);

		}

		/**
		 * Returns minimum steps of this branch as best result or, if not solved
		 * RES_NOT_FOUND. StartBits must be normalized.
		 */
		private short doIt(final int father, short actStep,
				final short totalBestResult) {

			if (isCancelled())
				return RES_NOT_SOLVED;

			// best result for this branch, will be returned
			short bestResult = totalBestResult;

			final String id = bitHelper.toString(father) + "/" + (actStep + 1)
					+ "/";

			log(id + ": Enter doIt()");

			actStep++;

			if (actStep > maxSteps) {
				log(id + ": maxSteps reached. Aborting with RES_NOT_SOLVED.");
				return RES_NOT_SOLVED;
			}

			if (actStep >= totalBestResult) {
				log(id
						+ ": actStep>=totalBestResult. Aborting with RES_NOT_SOLVED.");
				return RES_NOT_SOLVED;
			}

			// First create all possible movements without duplicate values
			// (same bit pattern). Ignore previous pattern.
			final Set<Integer> set = new HashSet<Integer>();
			int son = 0;
			for (int i = 0; i < numberOfBits; i++) {
				log("flip to " + bitHelper.toString(flipBits(father, i)));
				son = bitHelper.normalize(flipBits(father, i));
				log("normalized to " + bitHelper.toString(son));
				if (son != father)
					set.add(son);
			}

			// Walk through every combination
			for (final Integer bits : set) {

				log(id + ": flipBits to " + bitHelper.toString(bits));

				// Exit condition: Solved.
				if (bits == solutionBits) {
					Log.v(TAG,
							id
									+ ": solution found. Leaving with bestResult/actStep="
									+ actStep);
					return actStep;
				}

				// Ignore bits: We turn around in circles
				if (bits == gameBits) {
					log(id
							+ ": bits equal game bits. Aborting with RES_NOT_SOLVED.");

				} else {
					final short actResult = doIt(bits, actStep, bestResult);
					if (actResult < bestResult)
						bestResult = actResult;
				}
			}
			log(id + ": Finish. Leaving with bestResult=" + bestResult);
			return bestResult;
		}

		/** Flips bit and its neighbors **/
		private int flipBits(final int bits, final int pos) {
			int ret = bits;
			int i = pos;
			ret = BitHelper.flipBit(ret, i);

			i = pos == 0 ? numberOfBits - 1 : pos - 1;
			ret = BitHelper.flipBit(ret, i);

			i = pos == numberOfBits - 1 ? 0 : pos + 1;
			ret = BitHelper.flipBit(ret, i);

			return ret;
		}

		private void log(final String text) {
			// Log.v(TAG, text);
		}

		@Override
		protected void onPostExecute(final Short result) {
			Log.v(TAG, "Leave task with result=" + result);
			resultListener.onResult(result);
		}

	}

	private final static String TAG = "SolveService";

	private final BitHelper bitHelper;

	private final short maxSteps;

	private final int numberOfBits;
	private ResultListener resultListener;
	private int solutionBits;

	/** Holds the step counts for a game state. */
	private final Map<Integer, Short> solutionList = new HashMap<Integer, Short>();

	private Task task;

	/**
	 * 
	 * @param numberOfBits
	 *            integer with size of the bit array
	 * @param maxSteps
	 *            short with the permitted number of steps
	 */
	public SolveService(final int numberOfBits, final short maxSteps) {
		this.numberOfBits = numberOfBits;
		this.maxSteps = maxSteps;

		bitHelper = new BitHelper(numberOfBits);

		// Create Solution: all bit set
		for (int i = 0; i < numberOfBits; i++) {
			solutionBits = BitHelper.setBit(solutionBits, i);
		}

		// Null object
		resultListener = new ResultListener() {

			@Override
			public void onResult(final short numberOfSteps) {
				// Do nothing
			}
		};
	}

	public void setListener(final ResultListener resultListener) {
		this.resultListener = resultListener;
	}

	/**
	 * Order to solve for the given game. The result will be returned by the
	 * listener.
	 */
	public void startSolveServiceCalculation(final int bits) {
		Log.v(TAG, "startSolveServiceCalculation() " + bitHelper.toString(bits));

		// Already solved?
		if (solutionList.get(bits) != null) {
			resultListener.onResult(solutionList.get(bits));
			Log.v(TAG, "Already solved: " + solutionList.get(bits));
			return;
		}

		// Calculate new
		stop();

		Log.v(TAG, "Starting new task for " + bits);
		task = new Task();
		task.execute(bits);

	}

	public void stop() {
		// Previous result out of scope now
		if (task != null && !task.isCancelled()) {
			Log.v(TAG, "Cancel running task.");
			task.cancel(true);
		}
	}
}
