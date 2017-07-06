/*
 * This file provided by Facebook is for non-commercial testing and evaluation
 * purposes only.  Facebook reserves all rights not expressly granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * FACEBOOK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.facebook.fresco.samples.showcase.postprocessor;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.facebook.fresco.samples.showcase.imagepipeline.DurationCallback;

/**
 * Adds a watermark at random positions to the bitmap using {@link Canvas}.
 */
public class WatermarkPostprocessor extends BasePostprocessorWithDurationCallback {

  private static final int TEXT_COLOR = 0xBBFFFFFF;
  private static final int FONT_SIZE = 80;

  final protected int mCount;
  final protected String mWatermarkText;
  private final Random mRandom = new Random();
  private final Paint mPaint = new Paint();

  public WatermarkPostprocessor(
      DurationCallback durationCallback,
      int count,
      String watermarkText) {
    super(durationCallback);
    mCount = count;
    mWatermarkText = watermarkText;
  }

  @Override
  public void process(Bitmap bitmap) {
    final long startNs = System.nanoTime();

    final int w = bitmap.getWidth();
    final int h = bitmap.getHeight();

    final Canvas canvas = new Canvas(bitmap);

    mPaint.setAntiAlias(true);
    mPaint.setColor(TEXT_COLOR);
    mPaint.setFakeBoldText(true);
    mPaint.setTextSize(FONT_SIZE);

    for (int c = 0; c < mCount; c++) {
      final int x = mRandom.nextInt(w);
      final int y = mRandom.nextInt(h);
      canvas.drawText(mWatermarkText, x, y, mPaint);
    }

    showDuration(System.nanoTime() - startNs);
  }
}
