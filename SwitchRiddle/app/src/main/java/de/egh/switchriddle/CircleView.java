package de.egh.switchriddle;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CircleView extends View {
	public interface EventListener {
		public void onActionDown(float angle);

		public void onActionUp(float angle);
	}

	private static final int BORDER = 2;
	private static final int COLOR_OFF = 0x200091BD;
	private static final int COLOR_ON = 0xAF0091BD;
	private static final int COLOR_PRESSED = 0xFFF05E32;
	private static final String TAG = "CircleView";
	private List<Area> areaList = new ArrayList<Area>();
	private EventListener eventListener;
	private final Point mid;

	private final RectF oval;
	private final List<Paint> paintList = new ArrayList<Paint>();

	public CircleView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		oval = new RectF();
		mid = new Point();
	}

	private int getColor(final Area area) {
		if (area.isPressed())
			return COLOR_PRESSED;
		if (area.isOn())
			return COLOR_ON;
		return COLOR_OFF;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		for (int i = 0; i < areaList.size(); i++) {
			paintList.get(i).setColor(getColor(areaList.get(i)));
			canvas.drawArc(oval, areaList.get(i).getFromAngle() + BORDER,
					areaList.get(i).getSweepAngle() - BORDER, true,
					paintList.get(i));
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int left,
			final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		mid.set((getWidth() / 2), (getHeight() / 2));
		Log.v(TAG, "onLayout() set mid=(" + mid.x + "," + mid.y + ")");

		oval.set(0, 0, getWidth(), getHeight());
		Log.v(TAG, "set rect= (" + oval.left + "," + oval.top + ") ("
				+ oval.right + "," + oval.bottom + ")");

	}

	@Override
	public void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		final int size = width > height ? height : width;
		setMeasuredDimension(size, size);
	}

	// @Override
	// protected void onMeasure(final int widthMeasureSpec,
	// final int heightMeasureSpec) {
	//
	// final int width = getMeasuredWidth() > 0 ? getMeasuredWidth() : 200;
	// final int height = getMeasuredHeight() > 0 ? getMeasuredHeight()
	// : width;
	// final int newWidth = Math.min(width, height);
	//
	// Log.v(TAG, "onMeasure() : " + newWidth);
	// setMeasuredDimension(newWidth, newWidth);
	// }

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		final float x = event.getX() - mid.x;
		final float y = event.getY() - mid.y;
		String text = "";

		final float deg = (float) (((Math.atan2(y, x) * 180.0 / Math.PI) + 360) % 360);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (eventListener != null)
				eventListener.onActionDown(deg);
			text = "Action down";
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (eventListener != null)
				eventListener.onActionUp(deg);
			text = "Action up";
		}

		return true;
	}

	public void setAreaList(final List<Area> areaList) {
		this.areaList = areaList;
		paintList.clear();

		for (final Area area : areaList) {
			final Paint paint = new Paint();
			paint.reset();
			paint.setAntiAlias(true);
			paint.setStyle(Style.FILL);
			paint.setStrokeWidth(0);
			paint.setColor(getColor(area));
			paintList.add(paint);
		}

		invalidate();
	}

	public void setEventListener(final EventListener eventListener) {
		this.eventListener = eventListener;
	}
}
