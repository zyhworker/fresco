---
docid: configure-image-pipeline
title: Configuring the Image Pipeline
layout: docs
permalink: /docs/configure-image-pipeline.html
prev: intro-image-pipeline.html
next: caching.html
---

Most apps can initialize Fresco completely by the simple command:

```java
Fresco.initialize(context);
```

For those apps that need more advanced customization, we offer it using the [ImagePipelineConfig](../javadoc/reference/com/facebook/imagepipeline/core/ImagePipelineConfig.html) class.

Here is a maximal example. Rare is the app that actually needs all of these settings, but here they are for reference.


```java
ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
    .setBitmapMemoryCacheParamsSupplier(bitmapCacheParamsSupplier)
    .setCacheKeyFactory(cacheKeyFactory)
    .setDownsampleEnabled(true)
    .setWebpSupportEnabled(true)
    .setEncodedMemoryCacheParamsSupplier(encodedCacheParamsSupplier)
    .setExecutorSupplier(executorSupplier)
    .setImageCacheStatsTracker(imageCacheStatsTracker)
    .setMainDiskCacheConfig(mainDiskCacheConfig)
    .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
    .setNetworkFetchProducer(networkFetchProducer)
    .setPoolFactory(poolFactory)
    .setProgressiveJpegConfig(progressiveJpegConfig)
    .setRequestListeners(requestListeners)
    .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
    .build();
Fresco.initialize(context, config);
```

Be sure to pass your `ImagePipelineConfig` object to `Fresco.initialize!` Otherwise, Fresco will use a default configuration instead of the one you built.

### Understanding Suppliers

Several of the configuration builder's methods take arguments of a [Supplier](../javadoc/reference/com/facebook/common/internal/Supplier.html) of an instance rather than an instance itself. This is a little more complex to create, but allows you to change behaviors while your app is running. Memory caches, for one, check their Supplier every five minutes.

If you don't need to dynamically change the params, use a Supplier that returns the same object each time:

```java
Supplier<X> xSupplier = new Supplier<X>() {
  private X mX = new X(xparam1, xparam2...);
  public X get() {
    return mX;
  }
);
// when creating image pipeline
.setXSupplier(xSupplier);
```

### Thread pools

By default, the image pipeline uses three thread pools:

1. Three threads for network downloads
2. Two threads for all disk operations - local file reads, and the disk cache
3. Two threads for all CPU-bound operations - decodes, transforms, and background operations, such as postprocessing.

You can customize networking behavior by [setting your own network layer](using-other-network-layers.html).

To change the behavior for all other operations, pass in an instance of [ExecutorSupplier](../javadoc/reference/com/facebook/imagepipeline/core/ExecutorSupplier.html).

### Using a MemoryTrimmableRegistry

If your application listens to system memory events, it can pass them over to Fresco to trim memory caches.

The easiest way for most apps to listen to events is to override [Activity.onTrimMemory](http://developer.android.com/reference/android/app/Activity.html#onTrimMemory(int)). You can also use any subclass of [ComponentCallbacks2](http://developer.android.com/reference/android/content/ComponentCallbacks2.html).

You should have an implementation of [MemoryTrimmableRegistry](http://frescolib.org/javadoc/reference/com/facebook/common/memory/MemoryTrimmableRegistry.html). This object should keep a collection of [MemoryTrimmable](http://frescolib.org/javadoc/reference/com/facebook/common/memory/MemoryTrimmable.html) objects - Fresco's caches will be among them. When getting a system memory event, you call the appropriate `MemoryTrimmable` method on each of the trimmables.

### Configuring the memory caches

The bitmap cache and the encoded memory cache are configured by a Supplier of a [MemoryCacheParams](../javadoc/reference/com/facebook/imagepipeline/cache/MemoryCacheParams.html#MemoryCacheParams\(int, int, int, int, int\)) object.

### Configuring the disk cache

You use the builder pattern to create a [DiskCacheConfig](../javadoc/reference/com/facebook/cache/disk/DiskCacheConfig.Builder.html) object:

```java
DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
   .set....
   .set....
   .build()

// when building ImagePipelineConfig
.setMainDiskCacheConfig(diskCacheConfig)
```

### Keeping cache stats

If you want to keep track of metrics like the cache hit rate, you can implement the [ImageCacheStatsTracker](../javadoc/reference/com/facebook/imagepipeline/cache/ImageCacheStatsTracker.html) class. This provides callbacks for every cache event that you can use to keep your own statistics.
