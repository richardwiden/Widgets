package com.retorikdevelopment.widgets;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImageSpinner extends Gallery implements android.widget.AdapterView.OnItemSelectedListener {
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor = detector.getScaleFactor();
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
			getSelectedImageView().scale(mScaleFactor, detector.getFocusX(), detector.getFocusY());
			return true;
		}
	}

	private static final String TAG = "RetorikWidgets";
	private boolean atRightEdge;
	private boolean atLeftEdge;
	private OnItemSelectedListener listener;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor;
	boolean isScrollingRight, isScrollingLeft;
	int position = -1;
	View arg1 = null;
	AdapterView<?> parent = null;
	long id = -1;
	public static boolean isTouching;

	public ImageSpinner(Context context) {
		this(context, null);
	}

	public ImageSpinner(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setCallbackDuringFling(false);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	private void checkWidth() {
		ImageView imgView = getSelectedImageView();
		Rect r = imgView.getDrawable().getBounds();
		Matrix m = new Matrix(imgView.getImageMatrix());
		RectF rect = new RectF(0, 0, r.right, r.bottom);
		int width = getWidth();
		// int height = getHeight();
		m.mapRect(rect);
		if (rect.left >= -1)
			atLeftEdge = true;
		else
			atLeftEdge = false;
		if (rect.right <= width + 1)
			atRightEdge = true;
		else
			atRightEdge = false;
	}

	public SpecialImageView getSelectedImageView() {
		return (SpecialImageView) getSelectedView();
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {
		super.onCreateContextMenu(menu);
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		AdapterContextMenuInfo contextmenu = new AdapterContextMenuInfo(this, position, id);
		return contextmenu;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (velocityX == velocityY && velocityY == -1l) {
			return false;
		}
		checkWidth();
		if (mScaleDetector.isInProgress()) {
			Log.e(TAG, "SCROLL IN PROGRESS!!");
			return true;
		} else if (atRightEdge && velocityX < -50) {
			if (velocityX < -500) {
				velocityX = (float) (velocityX * 0.40);
				if (velocityX > -500)
					velocityX = -500;
			}
			Log.d(TAG, "" + velocityX);
			super.onFling(e1, e2, velocityX, velocityY);
			return true;
		} else if (atLeftEdge && velocityX > 50) {
			if (velocityX > 500) {
				velocityX = (float) (velocityX * 0.40);
				if (velocityX < 500)
					velocityX = 500;
			}
			Log.d(TAG, "" + velocityX);
			super.onFling(e1, e2, velocityX, velocityY);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int position, long id) {
		this.position = position;
		this.arg1 = arg1;
		this.parent = parent;
		this.id = id;
		if (isTouching)
			return;
		if (listener != null) {
			listener.onItemSelected(parent, arg1, position, id);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (getWidth() < getHeight())
					super.onFling(null, null, 1200, 0);
				else
					super.onFling(null, null, 1600, 0);
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (getWidth() < getHeight())
					super.onFling(null, null, -1200, 0);
				else
					super.onFling(null, null, -1600, 0);
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				if (showContextMenuForChild(getSelectedImageView()))
					return true;
				else
					return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
					|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_MENU) {
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		super.onLongPress(e);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Log.e(TAG, "NOTHING SELECTED");
		if (listener != null)
			listener.onNothingSelected(parent);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (mScaleDetector.isInProgress()) {
			return true;
		}
		checkWidth();
		if (distanceX < 0 && !atLeftEdge && !isScrollingLeft) {
			getSelectedImageView().translate(-distanceX, -distanceY);
			isScrollingLeft = isScrollingRight = false;
			// Log.e(TAG, "" + 1);
			return true;
		} else if (distanceX > 0 && !atRightEdge && !isScrollingRight) {
			getSelectedImageView().translate(-distanceX, -distanceY);
			isScrollingLeft = isScrollingRight = false;
			// Log.e(TAG, "" + 2);
			return true;
		} else {
			if (distanceX < 0 && !isScrollingLeft) {
				isScrollingRight = true;
			} else if (distanceX > 0 && !isScrollingRight) {
				isScrollingLeft = true;
			}
			if (isScrollingRight && distanceX > 0) {
				if (atRightEdge) {
					getSelectedImageView().translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					return true;
				} else if (getChildAt(0) != null && !getChildAt(0).isSelected()) {
					getSelectedImageView().translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					// Log.e(TAG, "positionRIGHT: ");
					return true;
				} else {
					getSelectedImageView().translate(-distanceX, -distanceY);
					return true;
				}
			} else if (isScrollingLeft && distanceX < 0) {
				if (atLeftEdge) {
					getSelectedImageView().translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					return true;
				} else if (getChildAt(0 + 1) != null && !getChildAt(0 + 1).isSelected()) {
					getSelectedImageView().translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					// Log.e(TAG, "positionLEFT: ");
					return true;
				} else {
					getSelectedImageView().translate(-distanceX, -distanceY);
					return true;
				}
			} else {
				getSelectedImageView().translate(0, -distanceY);
				super.onScroll(e1, e2, distanceX, distanceY);
				return true;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// setAnimationDuration(250);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			isTouching = true;
		} else if (event.getAction() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
			isTouching = false;
			isScrollingRight = false;
			isScrollingLeft = false;
			if (parent != null && arg1 != null && position != -1 && id != -1)
				listener.onItemSelected(parent, arg1, position, id);
			// TODO behövs denna center?
			getSelectedImageView().center(true, true, true);
		}
		mScaleDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.listener = listener;
		super.setOnItemSelectedListener(this);
	}
}
