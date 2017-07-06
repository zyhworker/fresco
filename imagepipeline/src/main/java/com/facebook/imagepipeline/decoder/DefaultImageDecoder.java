/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.imagepipeline.decoder;

import javax.annotation.Nullable;

import java.io.InputStream;
import java.util.Map;

import android.graphics.Bitmap;

import com.facebook.common.internal.Closeables;
import com.facebook.common.references.CloseableReference;
import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imageformat.ImageFormatChecker;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.platform.PlatformDecoder;

/**
 * Decodes images.
 *
 * <p> ImageDecoder implements image type recognition and passes decode requests to
 * specialized methods implemented by subclasses.
 *
 * On dalvik, it produces 'pinned' purgeable bitmaps.
 *
 * <p> Pinned purgeables behave as specified in
 * {@link android.graphics.BitmapFactory.Options#inPurgeable} with one modification. The bitmap is
 * 'pinned' so is never purged.
 *
 * <p> For API 21 and higher, this class produces standard Bitmaps, as purgeability is not supported
 * on the most recent versions of Android.
 */
public class DefaultImageDecoder implements ImageDecoder {

  private final ImageDecoder mAnimatedGifDecoder;
  private final ImageDecoder mAnimatedWebPDecoder;
  private final PlatformDecoder mPlatformDecoder;

  private final ImageDecoder mDefaultDecoder = new ImageDecoder() {
    @Override
    public CloseableImage decode(
        EncodedImage encodedImage,
        int length,
        QualityInfo qualityInfo,
        ImageDecodeOptions options) {
      ImageFormat imageFormat = encodedImage.getImageFormat();
      if (imageFormat == DefaultImageFormats.JPEG) {
        return decodeJpeg(encodedImage, length, qualityInfo, options);
      } else if (imageFormat == DefaultImageFormats.GIF) {
        return decodeGif(encodedImage, length, qualityInfo, options);
      } else if (imageFormat == DefaultImageFormats.WEBP_ANIMATED) {
        return decodeAnimatedWebp(encodedImage, length, qualityInfo, options);
      } else if (imageFormat == ImageFormat.UNKNOWN) {
        throw new IllegalArgumentException("unknown image format");
      }
      return decodeStaticImage(encodedImage, options);
    }
  };

  @Nullable
  private final Map<ImageFormat, ImageDecoder> mCustomDecoders;

  public DefaultImageDecoder(
      final ImageDecoder animatedGifDecoder,
      final ImageDecoder animatedWebPDecoder,
      final PlatformDecoder platformDecoder) {
    this(animatedGifDecoder, animatedWebPDecoder, platformDecoder, null);
  }

  public DefaultImageDecoder(
      final ImageDecoder animatedGifDecoder,
      final ImageDecoder animatedWebPDecoder,
      final PlatformDecoder platformDecoder,
      @Nullable Map<ImageFormat, ImageDecoder> customDecoders) {
    mAnimatedGifDecoder = animatedGifDecoder;
    mAnimatedWebPDecoder = animatedWebPDecoder;
    mPlatformDecoder = platformDecoder;
    mCustomDecoders = customDecoders;
  }

  /**
   * Decodes image.
   *
   * @param encodedImage input image (encoded bytes plus meta data)
   * @param length if image type supports decoding incomplete image then determines where
   *   the image data should be cut for decoding.
   * @param qualityInfo quality information for the image
   * @param options options that cange decode behavior
   */
  @Override
  public CloseableImage decode(
      final EncodedImage encodedImage,
      final int length,
      final QualityInfo qualityInfo,
      final ImageDecodeOptions options) {
    if (options.customImageDecoder != null) {
      return options.customImageDecoder.decode(encodedImage, length, qualityInfo, options);
    }
    ImageFormat imageFormat = encodedImage.getImageFormat();
    if (imageFormat == null || imageFormat == ImageFormat.UNKNOWN) {
      imageFormat = ImageFormatChecker.getImageFormat_WrapIOException(
          encodedImage.getInputStream());
      encodedImage.setImageFormat(imageFormat);
    }
    if (mCustomDecoders != null) {
      ImageDecoder decoder = mCustomDecoders.get(imageFormat);
      if (decoder != null) {
        return decoder.decode(encodedImage, length, qualityInfo, options);
      }
    }
    return mDefaultDecoder.decode(encodedImage, length, qualityInfo, options);
  }

  /**
   * Decodes gif into CloseableImage.
   *
   * @param encodedImage input image (encoded bytes plus meta data)
   * @return a CloseableImage
   */
  public CloseableImage decodeGif(
      final EncodedImage encodedImage,
      final int length,
      final QualityInfo qualityInfo,
      final ImageDecodeOptions options) {
    InputStream is = encodedImage.getInputStream();
    if (is == null) {
      return null;
    }
    try {
      if (!options.forceStaticImage
          && mAnimatedGifDecoder != null) {
        return mAnimatedGifDecoder.decode(encodedImage, length, qualityInfo, options);
      }
      return decodeStaticImage(encodedImage, options);
    } finally {
      Closeables.closeQuietly(is);
    }
  }

  /**
   * @param encodedImage input image (encoded bytes plus meta data)
   * @return a CloseableStaticBitmap
   */
  public CloseableStaticBitmap decodeStaticImage(
      final EncodedImage encodedImage,
      ImageDecodeOptions options) {
    CloseableReference<Bitmap> bitmapReference =
        mPlatformDecoder.decodeFromEncodedImage(encodedImage, options.bitmapConfig);
    try {
      return new CloseableStaticBitmap(
          bitmapReference,
          ImmutableQualityInfo.FULL_QUALITY,
          encodedImage.getRotationAngle());
    } finally {
      bitmapReference.close();
    }
  }

  /**
   * Decodes a partial jpeg.
   *
   * @param encodedImage input image (encoded bytes plus meta data)
   * @param length amount of currently available data in bytes
   * @param qualityInfo quality info for the image
   * @return a CloseableStaticBitmap
   */
  public CloseableStaticBitmap decodeJpeg(
      final EncodedImage encodedImage,
      int length,
      QualityInfo qualityInfo,
      ImageDecodeOptions options) {
    CloseableReference<Bitmap> bitmapReference =
        mPlatformDecoder.decodeJPEGFromEncodedImage(encodedImage, options.bitmapConfig, length);
    try {
      return new CloseableStaticBitmap(
          bitmapReference,
          qualityInfo,
          encodedImage.getRotationAngle());
    } finally {
      bitmapReference.close();
    }
  }

  /**
   * Decode a webp animated image into a CloseableImage.
   *
   * <p> The image is decoded into a 'pinned' purgeable bitmap.
   *
   * @param encodedImage input image (encoded bytes plus meta data)
   * @param options
   * @return a {@link CloseableImage}
   */
  public CloseableImage decodeAnimatedWebp(
      final EncodedImage encodedImage,
      final int length,
      final QualityInfo qualityInfo,
      final ImageDecodeOptions options) {
    return mAnimatedWebPDecoder.decode(encodedImage, length, qualityInfo, options);
  }
}
