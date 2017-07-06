/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.drawee.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.facebook.drawee.view.DraweeHolder;
import com.facebook.widget.text.span.BetterImageSpan;

/**
 * Span that contains a Drawee.
 *
 * <p>The containing view must also call {@link #onDetach()} from its
 * {@link View#onStartTemporaryDetach()} and {@link View#onDetachedFromWindow()}
 * methods.
 * Similarly, it must call {@link #onAttach} from its
 * {@link View#onFinishTemporaryDetach()} and {@link View#onAttachedToWindow()}
 * methods.
 *
 * {@see DraweeHolder}
 */
public class DraweeSpan extends BetterImageSpan {

  private final DraweeHolder mDraweeHolder;

  public DraweeSpan(
      DraweeHolder draweeHolder,
      @BetterImageSpan.BetterImageSpanAlignment int verticalAlignment) {
    super(draweeHolder.getTopLevelDrawable(), verticalAlignment);
    mDraweeHolder = draweeHolder;
  }

  @Override
  public void draw(
      Canvas canvas,
      CharSequence text,
      int start,
      int end,
      float x,
      int top,
      int y,
      int bottom,
      Paint paint) {
    if (mDraweeHolder.isAttached()) {
      super.draw(canvas, text, start, end, x, top, y, bottom, paint);
    }
  }

  /**
   * Gets the drawee span ready to display the image.
   *
   * <p>The containing view must call this method from both {@link View#onFinishTemporaryDetach()}
   * and {@link View#onAttachedToWindow()}.
   */
  public void onAttach() {
    mDraweeHolder.onAttach();
  }

  /**
   * Checks whether the view that uses this holder is currently attached to a window.
   *
   * {@see #onAttach()}
   * {@see #onDetach()}
   *
   * @return true if the holder is currently attached
   */
  public boolean isAttached() {
    return mDraweeHolder.isAttached();
  }

  /**
   * Releases resources used to display the image.
   *
   * <p>The containing view must call this method from both {@link View#onStartTemporaryDetach()}
   * and {@link View#onDetachedFromWindow()}.
   */
  public void onDetach() {
    mDraweeHolder.onDetach();
  }

  /**
   * Get the underlying DraweeHolder.
   *
   * @return the DraweeHolder
   */
  public DraweeHolder getDraweeHolder() {
    return mDraweeHolder;
  }
}
