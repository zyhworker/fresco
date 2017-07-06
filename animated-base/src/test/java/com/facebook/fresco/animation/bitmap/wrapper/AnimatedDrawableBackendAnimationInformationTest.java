/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.fresco.animation.bitmap.wrapper;

import com.facebook.fresco.animation.backend.AnimationBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link AnimatedDrawableBackendAnimationInformation}.
 */
public class AnimatedDrawableBackendAnimationInformationTest {

  private AnimatedDrawableBackend mAnimatedDrawableBackend;
  private AnimatedDrawableBackendAnimationInformation mAnimatedDrawableBackendAnimationInformation;

  @Before
  public void setup() {
    mAnimatedDrawableBackend = mock(AnimatedDrawableBackend.class);
    mAnimatedDrawableBackendAnimationInformation =
        new AnimatedDrawableBackendAnimationInformation(mAnimatedDrawableBackend);
  }

  @Test
  public void testGetFrameCount() throws Exception {
    when(mAnimatedDrawableBackend.getFrameCount()).thenReturn(123);

    assertThat(mAnimatedDrawableBackendAnimationInformation.getFrameCount()).isEqualTo(123);
  }

  @Test
  public void testGetFrameDurationMs() throws Exception {
    when(mAnimatedDrawableBackend.getDurationMsForFrame(1)).thenReturn(123);
    when(mAnimatedDrawableBackend.getDurationMsForFrame(2)).thenReturn(200);

    assertThat(mAnimatedDrawableBackendAnimationInformation.getFrameDurationMs(1)).isEqualTo(123);
    assertThat(mAnimatedDrawableBackendAnimationInformation.getFrameDurationMs(2)).isEqualTo(200);
  }

  @Test
  public void testGetLoopCount() throws Exception {
    when(mAnimatedDrawableBackend.getLoopCount()).thenReturn(123);

    assertThat(mAnimatedDrawableBackendAnimationInformation.getLoopCount()).isEqualTo(123);
  }

  @Test
  public void testGetLoopCountInfinite() throws Exception {
    when(mAnimatedDrawableBackend.getLoopCount()).thenReturn(AnimationBackend.LOOP_COUNT_INFINITE);

    assertThat(mAnimatedDrawableBackendAnimationInformation.getLoopCount())
        .isEqualTo(AnimationBackend.LOOP_COUNT_INFINITE);
  }
}
