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
package com.facebook.fresco.samples.showcase.imagepipeline;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.fresco.samples.showcase.BaseShowcaseFragment;
import com.facebook.fresco.samples.showcase.R;
import com.facebook.imagepipeline.request.ImageRequest;

/**
 * Fragment that illustrates how to prefetch images to disk cache so that they load faster when
 * finally displayed.
 */
public class ImagePipelinePrefetchFragment extends BaseShowcaseFragment {

  private static final String[] URLS = {
      "http://frescolib.org/static/sample-images/animal_a.png",
      "http://frescolib.org/static/sample-images/animal_c.png",
  };

  private Button mPrefetchButton;
  private TextView mPrefetchStatus;
  private ViewGroup mDraweesHolder;

  @Override
  public int getTitleId() {
    return R.string.imagepipeline_prefetch_title;
  }

  private class PrefetchSubscriber extends BaseDataSubscriber<Void> {

    private int mSuccessful = 0;
    private int mFailed = 0;

    @Override
    protected void onNewResultImpl(DataSource<Void> dataSource) {
      mSuccessful++;
      updateDisplay();
    }

    @Override
    protected void onFailureImpl(DataSource<Void> dataSource) {
      mFailed++;
      updateDisplay();
    }

    private void updateDisplay() {
      if (mSuccessful + mFailed == URLS.length) {
        mPrefetchButton.setEnabled(true);
      }
      mPrefetchStatus.setText(
          getString(R.string.prefetch_status, mSuccessful, URLS.length, mFailed));
    }
  }

  public ImagePipelinePrefetchFragment() {
    // Required empty public constructor
  }

  @Override
  public @Nullable View onCreateView(
      LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_imagepipeline_prefetch, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final Button clearCacheButton = (Button) view.findViewById(R.id.clear_cache);
    clearCacheButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (String url : URLS) {
          Fresco.getImagePipeline().evictFromCache(Uri.parse(url));
        }
      }
    });

    mPrefetchStatus = (TextView) view.findViewById(R.id.prefetch_status);
    mPrefetchButton = (Button) view.findViewById(R.id.prefetch_now);
    mPrefetchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mPrefetchButton.setEnabled(false);
        final PrefetchSubscriber subscriber = new PrefetchSubscriber();
        for (String url : URLS) {
          final DataSource<Void> ds =
              Fresco.getImagePipeline().prefetchToDiskCache(ImageRequest.fromUri(url), null);
          ds.subscribe(subscriber, UiThreadImmediateExecutorService.getInstance());
        }
      }
    });

    mDraweesHolder = (ViewGroup) view.findViewById(R.id.drawees);
    Button toggleImages = (Button) view.findViewById(R.id.toggle_images);
    toggleImages.setOnClickListener(new View.OnClickListener() {
      private boolean mShowing = false;
      @Override
      public void onClick(View v) {
        if (!mShowing) {
          for (int i = 0; i < mDraweesHolder.getChildCount(); i++) {
            ((SimpleDraweeView) mDraweesHolder.getChildAt(i)).setImageURI(URLS[i]);
          }
        } else {
          for (int i = 0; i < mDraweesHolder.getChildCount(); i++) {
            ((SimpleDraweeView) mDraweesHolder.getChildAt(i)).setController(null);
          }
        }
        mShowing = !mShowing;
      }
    });
  }
}
