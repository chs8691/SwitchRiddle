package de.egh.switchriddle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import de.egh.switchriddle.CircleView.EventListener;
import de.egh.switchriddle.SolveService.ResultListener;

public class MainActivity extends Activity {

	private static final String AREA_ON = "AREA_ON_";
	// For a 7 field area there are no more than 7 steps to solve any game.
	// Change this value if the quantity will be changed.
	private static final short MAX_NUMBER_OF_STEPS = 7;
	private static final String PREFS_COUNTER = "counter";
	private static final String PREFS_SCORE_NOMINAL = "scoreNominal";

	// ** Number of Areas */
	private static final int QUANTITY = 7;
	private static final String TAG = "MainActivity";

	/** Converts a game state to bit array as integer. */
	public static int toBits(final List<Area> list) {
		int bits = 0x0;

		for (int i = 0; i < list.size(); i++)
			if (list.get(i).isOn())
				bits = BitHelper.setBit(bits, i);

		return bits;
	}

	private final List<Area> areaList = new ArrayList<Area>();
	private CircleView circleView;
	private TextView counterView;
	private SharedPreferences prefs;
	private SolveService.ResultListener resultListener;

	private TextView solutionCounterView;
	SolveService solveService = new SolveService(QUANTITY, MAX_NUMBER_OF_STEPS);

	private void buildAreas(final int quantity) {
		final float angle = 360.0f / quantity;
		float fromAngle = 270 - (angle / 2.0f);

		areaList.clear();

		for (int i = 0; i < quantity; i++) {
			fromAngle = fromAngle % 360;
			final Area area = new Area(i);
			area.setFromAngle(fromAngle);
			area.setSweepAngle(angle);
			area.setOn(prefs.getBoolean(AREA_ON + area.getNr(), false));
			area.setPressed(false);

			areaList.add(area);

			fromAngle += angle;
		}

		Area prev = areaList.get(quantity - 1);
		for (int i = 0; i < quantity; i++) {
			final Area act = areaList.get(i);
			final Area next = areaList.get((i + 1) % quantity);

			act.setPreviousArea(prev);
			act.setNextArea(next);

			prev = act;
		}

		for (final Area area : areaList) {
			Log.v(TAG, "Area " + area.getNr() + " " + area.getFromAngle() + "°");
		}

	}

	private void makeToast() {

		Toast.makeText(this, R.string.toastSolved, Toast.LENGTH_LONG).show();

	}

	public void onClickClear(final View view) {
		resetScore();
		for (final Area area : areaList) {
			area.setOn(false);
		}
		circleView.invalidate();
		startSolutionCounter();
	}

	public void onClickRandom(final View view) {
		resetScore();
		final Random r = new Random();

		for (final Area area : areaList) {
			area.setOn(r.nextBoolean());
		}
		circleView.invalidate();
		startSolutionCounter();
	}

	public void onClickReset(final View view) {
		resetScore();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);

		prefs = getSharedPreferences("de.egh.switchriddle", MODE_PRIVATE);

		counterView = (TextView) findViewById(R.id.counterView);
		solutionCounterView = (TextView) findViewById(R.id.solutionCounterView);
		circleView = (CircleView) findViewById(R.id.circleView);
		circleView.setEventListener(new EventListener() {

			@Override
			public void onActionDown(final float angle) {
				for (int i = 0; i < areaList.size(); i++) {
					areaList.get(i).onPressed(angle);
				}
				circleView.invalidate();
			}

			@Override
			public void onActionUp(final float angle) {

				// Reset counter, if riddle solved
				int actCounter;

				if (solved()) {
					actCounter = 0;
				} else
					actCounter = Integer.valueOf(counterView.getText()
							.toString());
				counterView.setText("" + ++actCounter);

				for (int i = 0; i < areaList.size(); i++) {
					areaList.get(i).onReleased(angle);

				}

				if (solved())
					makeToast();

				circleView.invalidate();
				startSolutionCounter();
			}
		});

		solveService.setListener(new ResultListener() {

			@Override
			public void onResult(final short numberOfSteps) {
				solutionCounterView.setText("" + numberOfSteps);

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		if (item.getItemId() == R.id.action_about) {
			// 1. Instantiate an AlertDialog.Builder with its constructor
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// 2. Chain together various setter methods to set the dialog
			// characteristics
			builder.setMessage(R.string.about_message).setTitle(
					R.string.about_title);

			// 3. Get the AlertDialog from create()
			final AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		prefs.edit()
				.putInt(PREFS_COUNTER,
						Integer.valueOf(counterView.getText().toString()))
				.commit();
		for (final Area area : areaList) {
			prefs.edit().putBoolean(AREA_ON + area.getNr(), area.isOn())
					.commit();
		}

		solveService.stop();

	}

	@Override
	protected void onResume() {
		super.onResume();
		counterView.setText("" + prefs.getInt(PREFS_COUNTER, 0));

		buildAreas(QUANTITY);
		circleView.setAreaList(areaList);
		startSolutionCounter();
	}

	/**
	 * Resets counter and starts new game.
	 */
	private void resetScore() {

		counterView.setText("0");
	}

	/** Solved means all lights are on */
	private boolean solved() {
		final boolean on = true;
		boolean equal = true;
		for (int i = 0; i < areaList.size(); i++) {
			equal = areaList.get(i).isOn() == on;
			// End at first non-equal
			if (!equal)
				i = areaList.size();
		}
		Log.v(TAG, "solved()=" + equal);
		return equal;
	}

	private void startSolutionCounter() {
		solutionCounterView.setText("...");
		solveService.startSolveServiceCalculation(toBits(areaList));
	}
}
