/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.fresco.animation.bitmap;

import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.facebook.fresco.animation.backend.AnimationBackend;

/**
 * Bitmap frame renderer used by {@link BitmapAnimationBackend} to render
 * animated images (e.g. GIFs or animated WebPs).
 */
public interface BitmapFrameRenderer {

  /**
   * Render the frame for the given frame number to the target bitmap.
   *
   * @param frameNumber the frame number to render
   * @param targetBitmap the bitmap to render the frame in
   * @return true if successful
   */
  boolean renderFrame(int frameNumber, Bitmap targetBitmap);

  /**
   * Set the parent drawable bounds to be used for frame rendering.
   *
   * @param bounds the bounds to use
   */
  void setBounds(@Nullable Rect bounds);

  /**
   * Return the intrinsic width of bitmap frames.
   * Return {@link AnimationBackend#INTRINSIC_DIMENSION_UNSET} if no specific width is set.
   *
   * @return the intrinsic width
   */
  int getIntrinsicWidth();

  /**
   * Return the intrinsic height of bitmap frames.
   * Return {@link AnimationBackend#INTRINSIC_DIMENSION_UNSET} if no specific height is set.
   *
   * @return the intrinsic height
   */
  int getIntrinsicHeight();
}
