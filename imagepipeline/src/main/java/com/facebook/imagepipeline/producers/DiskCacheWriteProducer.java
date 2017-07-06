/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.imagepipeline.producers;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.internal.VisibleForTesting;
import com.facebook.imagepipeline.cache.BufferedDiskCache;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.request.ImageRequest;

/**
 * Disk cache write producer.
 *
 * <p>This producer passes through to the next producer in the sequence, as long as the permitted
 * request level reaches beyond the disk cache. Otherwise this is a passive producer.
 *
 * <p>The final result passed to the consumer put into the disk cache as well as being passed on.
 *
 * <p>This implementation delegates disk cache requests to BufferedDiskCache.
 *
 * <p>This producer is currently used only if the media variations experiment is turned on, to
 * enable another producer to sit between cache read and write.
 */
public class DiskCacheWriteProducer implements Producer<EncodedImage> {
  @VisibleForTesting static final String PRODUCER_NAME = "DiskCacheProducer";

  private final BufferedDiskCache mDefaultBufferedDiskCache;
  private final BufferedDiskCache mSmallImageBufferedDiskCache;
  private final CacheKeyFactory mCacheKeyFactory;
  private final Producer<EncodedImage> mInputProducer;

  public DiskCacheWriteProducer(
      BufferedDiskCache defaultBufferedDiskCache,
      BufferedDiskCache smallImageBufferedDiskCache,
      CacheKeyFactory cacheKeyFactory,
      Producer<EncodedImage> inputProducer) {
    mDefaultBufferedDiskCache = defaultBufferedDiskCache;
    mSmallImageBufferedDiskCache = smallImageBufferedDiskCache;
    mCacheKeyFactory = cacheKeyFactory;
    mInputProducer = inputProducer;
  }

  public void produceResults(
      final Consumer<EncodedImage> consumer,
      final ProducerContext producerContext) {
    maybeStartInputProducer(consumer, producerContext);
  }

  private void maybeStartInputProducer(
      Consumer<EncodedImage> consumerOfDiskCacheWriteProducer,
      ProducerContext producerContext) {
    if (producerContext.getLowestPermittedRequestLevel().getValue() >=
        ImageRequest.RequestLevel.DISK_CACHE.getValue()) {
      consumerOfDiskCacheWriteProducer.onNewResult(null, Consumer.IS_LAST);
    } else {
      Consumer<EncodedImage> consumer;
      if (producerContext.getImageRequest().isDiskCacheEnabled()) {
        consumer = new DiskCacheWriteConsumer(
            consumerOfDiskCacheWriteProducer,
            producerContext,
            mDefaultBufferedDiskCache,
            mSmallImageBufferedDiskCache,
            mCacheKeyFactory
        );
      } else {
        consumer = consumerOfDiskCacheWriteProducer;
      }

      mInputProducer.produceResults(consumer, producerContext);
    }
  }

  /**
   * Consumer that consumes results from next producer in the sequence.
   *
   * <p>The consumer puts the last result received into disk cache, and passes all results (success
   * or failure) down to the next consumer.
   */
  private static class DiskCacheWriteConsumer
      extends DelegatingConsumer<EncodedImage, EncodedImage> {

    private final ProducerContext mProducerContext;
    private final BufferedDiskCache mDefaultBufferedDiskCache;
    private final BufferedDiskCache mSmallImageBufferedDiskCache;
    private final CacheKeyFactory mCacheKeyFactory;

    private DiskCacheWriteConsumer(
        final Consumer<EncodedImage> consumer,
        final ProducerContext producerContext,
        final BufferedDiskCache defaultBufferedDiskCache,
        final BufferedDiskCache smallImageBufferedDiskCache,
        final CacheKeyFactory cacheKeyFactory) {
      super(consumer);
      mProducerContext = producerContext;
      mDefaultBufferedDiskCache = defaultBufferedDiskCache;
      mSmallImageBufferedDiskCache = smallImageBufferedDiskCache;
      mCacheKeyFactory = cacheKeyFactory;
    }

    @Override
    public void onNewResultImpl(EncodedImage newResult, @Status int status) {
      // intermediate, null or uncacheable results are not cached, so we just forward them
      if (isNotLast(status) || newResult == null ||
          statusHasAnyFlag(status, DO_NOT_CACHE_ENCODED | IS_PARTIAL_RESULT)) {
        getConsumer().onNewResult(newResult, status);
        return;
      }

      final ImageRequest imageRequest = mProducerContext.getImageRequest();
      final CacheKey cacheKey =
          mCacheKeyFactory.getEncodedCacheKey(imageRequest, mProducerContext.getCallerContext());

      if (imageRequest.getCacheChoice() == ImageRequest.CacheChoice.SMALL) {
        mSmallImageBufferedDiskCache.put(cacheKey, newResult);
      } else {
        mDefaultBufferedDiskCache.put(cacheKey, newResult);
      }

      getConsumer().onNewResult(newResult, status);
    }
  }
}
