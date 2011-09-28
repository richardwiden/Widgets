package com.retorikdevelopment.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.app.Activity;

public class ImageSpinner extends Gallery implements android.widget.AdapterView.OnItemSelectedListener {
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor = detector.getScaleFactor();
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
			if (!isGif)
				((SpecialImageView) getSelectedView()).scale(mScaleFactor, detector.getFocusX(), detector.getFocusY());
			return true;
		}
	}

	private static final String TAG = "RetorikWidgets";
	private static float densityModulator;
	private OnItemSelectedListener listener;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor;
	boolean isScrollingRight, isScrollingLeft, atLeftEdge, atRightEdge, isTouching;
	private int position = -1;
	private View view = null;
	private AdapterView<?> parent = null;
	private long id = -1;
	private RectF rect;
	private boolean isGif;

	public ImageSpinner(Context context) {
		this(context, null);
	}

	public ImageSpinner(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setCallbackDuringFling(false);
		rect = new RectF();
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		switch (metrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			densityModulator = 0.8f;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			densityModulator = 1.0f;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			densityModulator = 1.2f;
			break;
		}
	}

	private void checkWidth() {
		if (isGif) {
			atLeftEdge = atRightEdge = true;
			return;
		}
		ImageView imgView = (ImageView) getSelectedView();
		Rect r = imgView.getDrawable().getBounds();
		rect.set(0, 0, r.right, r.bottom);
		imgView.getImageMatrix().mapRect(rect);
		if (rect.left >= -1)
			atLeftEdge = true;
		else
			atLeftEdge = false;
		if (rect.right <= getWidth() + 1)
			atRightEdge = true;
		else
			atRightEdge = false;
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
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		this.position = position;
		this.view = view;
		this.parent = parent;
		this.id = id;
		if (view.getClass().getSimpleName().equals("SpecialWebView")) {
			this.isGif = true;
		} else
			this.isGif = false;
		if (isTouching)
			return;
		if (listener != null) {
			listener.onItemSelected(parent, view, position, id);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int width = getWidth();
		float modifier = 1.1f;
		if (width <= 240)
			modifier = 2.8f;
		if (width <= 320)
			modifier = 2.4f;
		else if (width <= 480)
			modifier = 1.95f;
		else if (width <= 540)
			modifier = 1.83f;
		else if (width <= 600)
			modifier = 1.75f;
		else if (width <= 800)
			modifier = 1.41f;
		else if (width <= 854)
			modifier = 1.39f;
		else if (width <= 1024)
			modifier = 1.38f;
		else if (width <= 1280)
			modifier = 1.2f;
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				super.onFling(null, null, (float) (getWidth() * modifier * densityModulator), 0);
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				super.onFling(null, null, (float) (getWidth() * modifier * densityModulator * -1), 0);
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				if (showContextMenuForChild(getSelectedView()))
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
	public void onNothingSelected(AdapterView<?> parent) {
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
			((SpecialImageView) getSelectedView()).translate(-distanceX, -distanceY);
			isScrollingLeft = isScrollingRight = false;
			return true;
		} else if (distanceX > 0 && !atRightEdge && !isScrollingRight) {
			((SpecialImageView) getSelectedView()).translate(-distanceX, -distanceY);
			isScrollingLeft = isScrollingRight = false;
			return true;
		} else {
			if (distanceX < 0 && !isScrollingLeft) {
				isScrollingRight = true;
			} else if (distanceX > 0 && !isScrollingRight) {
				isScrollingLeft = true;
			}
			if (isScrollingRight && distanceX > 0) {
				if (atRightEdge) {
					if (!isGif)
						((SpecialImageView) getSelectedView()).translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					return true;
				} else if (getChildAt(0) != null && !getChildAt(0).isSelected()) {
					if (!isGif)
						((SpecialImageView) getSelectedView()).translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					return true;
				} else {
					if (!isGif)
						((SpecialImageView) getSelectedView()).translate(-distanceX, -distanceY);
					return true;
				}
			} else if (isScrollingLeft && distanceX < 0) {
				if (atLeftEdge) {
					if (!isGif)
						((SpecialImageView) getSelectedView()).translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					return true;
				} else if (getChildAt(0 + 1) != null && !getChildAt(0 + 1).isSelected()) {
					if (!isGif)
						((SpecialImageView) getSelectedView()).translate(0, -distanceY);
					super.onScroll(e1, e2, distanceX, distanceY);
					return true;
				} else {
					if (!isGif)
						((SpecialImageView) getSelectedView()).translate(-distanceX, -distanceY);
					return true;
				}
			} else {
				if (!isGif)
					((SpecialImageView) getSelectedView()).translate(0, -distanceY);
				super.onScroll(e1, e2, distanceX, distanceY);
				return true;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			isTouching = true;
			if (isGif){
				((WebView) getSelectedView()).pauseTimers();				
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
			isTouching = false;
			isScrollingRight = false;
			isScrollingLeft = false;
			if (parent != null && view != null && position != -1 && id != -1)
				listener.onItemSelected(parent, view, position, id);
			if (!isGif)
				((SpecialImageView) getSelectedView()).center(true, true, true);
			else
				((WebView) getSelectedView()).resumeTimers();
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
