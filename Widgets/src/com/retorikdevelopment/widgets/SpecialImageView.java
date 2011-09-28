package com.retorikdevelopment.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class SpecialImageView extends ImageView {
	private Bitmap not_foundBitmap;
	protected static final String TAG = "RetorikWidgets";
	private boolean newImageNeedsLayout;
	private RectF rect;

	public SpecialImageView(Context context) {
		this(context, null);
	}

	public SpecialImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SpecialImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		newImageNeedsLayout = true;
		rect = new RectF();
	}

	public void center(boolean horizontal, boolean vertical, boolean animation) {
		Drawable drawable = this.getDrawable();
		if (drawable == null)
			return;
		Rect r = drawable.getBounds();
		rect.set(0, 0, r.right, r.bottom);
		final Matrix matrix = new Matrix(this.getImageMatrix());
		matrix.mapRect(rect);
		float height = rect.height(), width = rect.width();
		float deltaX = 0;
		float deltaY = 0;
		if (vertical) {
			int viewHeight = getHeight();
			if (height < viewHeight) {
				deltaY = (viewHeight - height) / 2f - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < viewHeight) {
				deltaY = getHeight() - rect.bottom;
			}
		}
		if (horizontal) {
			int viewWidth = getWidth();
			if (width < viewWidth) {
				deltaX = (viewWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < viewWidth) {
				deltaX = viewWidth - rect.right;
			}
		}
		if (animation) {
			setVisibility(INVISIBLE);
			matrix.postTranslate(deltaX, deltaY);
			this.setImageMatrixInternal(matrix);
			TranslateAnimation trans = new TranslateAnimation(-deltaX, 0, -deltaY, 0);
			trans.setDuration(100);
			trans.setInterpolator(new AccelerateInterpolator(2.0f));
			this.startAnimation(trans);
		} else {
			matrix.postTranslate(deltaX, deltaY);
			this.setImageMatrixInternal(matrix);
		}
	}

	protected Bitmap getNotFoundBitmap() {
		if (not_foundBitmap == null) {
			Resources res = getContext().getResources();
			BitmapDrawable drw = (BitmapDrawable) res.getDrawable(R.drawable.placeholder);
			not_foundBitmap = drw.getBitmap();
		}
		return not_foundBitmap;
	}

	public boolean isNewImageNeedsLayout() {
		return newImageNeedsLayout;
	}

	@Override
	protected void onAnimationEnd() {
		super.onAnimationEnd();
		setVisibility(VISIBLE);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (newImageNeedsLayout) {
			Rect r = this.getDrawable().getBounds();
			rect.set(0, 0, r.right, r.bottom);
			getImageMatrix().mapRect(rect);
			float imageWidth = rect.width();
			float imageHeight = rect.height();
			setZoomLevel(imageWidth, imageHeight, getWidth(), getHeight());
			newImageNeedsLayout = false;
		}
		center(true, true, false);
	}

	public void scale(float scaleFactor, float focusX, float focusY) {
		Rect r = this.getDrawable().getBounds();
		rect.set(0, 0, r.right, r.bottom);
		Matrix matrix = new Matrix(this.getImageMatrix());
		matrix.mapRect(rect);
		float imageWidth = rect.width();
		float imageHeight = rect.height();
		if (imageWidth * scaleFactor <= getWidth() && imageHeight * scaleFactor <= getHeight() && scaleFactor <= 1f) {
			setZoomLevel(imageWidth, imageHeight, getWidth(), getHeight()); // Image
																			// too
																			// small
			center(true, true, false);
			return;
		}
		matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
		this.setImageMatrixInternal(matrix);
		center(true, true, false);
	}

	@Override
	public void setImageMatrix(Matrix matrix) {
		throw new RuntimeException("Invalid method use helpermethods instead (setImageMatrixInternal)");
	}

	protected void setImageMatrixInternal(Matrix matrix) {
		super.setImageMatrix(matrix);
	}

	public void setNewImageNeedsLayout(boolean newImageNeedsLayout) {
		this.newImageNeedsLayout = newImageNeedsLayout;
	}

	public void setZoomLevel(float imgWidth, float imgHeight, float vWidth, float vHeight) {
		if (vWidth != 0 && vHeight != 0) {
			Matrix matrix = new Matrix(this.getImageMatrix());
			float widthScale = vWidth / imgWidth;
			float heightScale = vHeight / imgHeight;
			float scale = 1;
			if (widthScale < heightScale) {
				scale = widthScale;
			} else {
				scale = heightScale;
			}
			matrix.postScale(scale, scale);
			this.setImageMatrixInternal(matrix);
		}
	}

	public void translate(float distanceX, float distanceY) {
		Matrix matrix = new Matrix(this.getImageMatrix());
		matrix.postTranslate(distanceX, distanceY);
		this.setImageMatrixInternal(matrix);
	}
}
