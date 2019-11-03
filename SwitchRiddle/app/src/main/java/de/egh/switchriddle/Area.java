package de.egh.switchriddle;

import android.util.Log;

/** Area of light is a part of the circle. */
public class Area {
	private static final String TAG = "Area";
	private float fromAngle;
	private Area nextArea;
	private final int nr;
	private boolean on;
	private boolean pressed;
	private Area previousArea;
	private float sweepAngle;

	public Area(final int nr) {
		this.nr = nr;
	}

	public float getFromAngle() {
		return fromAngle;
	}

	public Area getNextArea() {
		return nextArea;
	}

	public int getNr() {
		return nr;
	}

	public Area getPreviousArea() {
		return previousArea;
	}

	public float getSweepAngle() {
		return sweepAngle;
	}

	private boolean isIn(final float angle) {
		boolean ret;
		float min;
		float max;
		if (angle >= fromAngle) {
			min = fromAngle;
		} else {
			min = fromAngle - 360;
		}
		max = min + sweepAngle;

		String text = "Area + " + nr + " isIn(" + angle + ")" + min + "° "
				+ max + "°";
		if (angle <= max && angle >= min) {
			text += ": TRUE";
			ret = true;
		} else {
			text += ": FALSE";
			ret = false;
		}

		if (nr == 2)
			Log.v(TAG, text);

		return ret;
	}

	public boolean isOn() {
		return on;
	}

	public boolean isPressed() {
		return pressed;
	}

	/** Event pressed. Returns TRUE if event is for this area, otherwise FALSE. */
	public boolean onPressed(final float angle) {
		if (isIn(angle)) {
			Log.v(TAG, nr + " is pressed");
			pressed = true;
			return true;
		} else {
			pressed = false;
			return false;
		}
	}

	/**
	 * Event touch released. Returns TRUE if event is for this area, otherwise
	 * FALSE.
	 */
	public boolean onReleased(final float angle) {
		pressed = false;
		if (isIn(angle)) {
			setOn(!isOn());
			previousArea.setOn(!previousArea.isOn());
			nextArea.setOn(!nextArea.isOn());
			return true;
		} else {
			return false;
		}
	}

	public void setFromAngle(final float fromAngle) {
		this.fromAngle = fromAngle;
	}

	public void setNextArea(final Area nextArea) {
		this.nextArea = nextArea;
	}

	public void setOn(final boolean on) {
		this.on = on;
	}

	public void setPressed(final boolean pressed) {
		this.pressed = pressed;
	}

	public void setPreviousArea(final Area previousArea) {
		this.previousArea = previousArea;
	}

	public void setSweepAngle(final float sweepAngle) {
		this.sweepAngle = sweepAngle;
	}

}
