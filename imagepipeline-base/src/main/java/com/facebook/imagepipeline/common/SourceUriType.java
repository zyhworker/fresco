/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.imagepipeline.common;

import java.lang.annotation.Retention;

import android.support.annotation.IntDef;

import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_DATA;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_QUALIFIED_RESOURCE;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_LOCAL_ASSET;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_LOCAL_CONTENT;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_LOCAL_FILE;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_LOCAL_IMAGE_FILE;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_LOCAL_RESOURCE;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_LOCAL_VIDEO_FILE;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_NETWORK;
import static com.facebook.imagepipeline.common.SourceUriType.SOURCE_TYPE_UNKNOWN;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * This is the interface we use in order to define different types of Uri an ImageRequest
 * can have.
 */
@Retention(SOURCE)
@IntDef({
    SOURCE_TYPE_UNKNOWN,
    SOURCE_TYPE_NETWORK,
    SOURCE_TYPE_LOCAL_FILE,
    SOURCE_TYPE_LOCAL_VIDEO_FILE,
    SOURCE_TYPE_LOCAL_IMAGE_FILE,
    SOURCE_TYPE_LOCAL_CONTENT,
    SOURCE_TYPE_LOCAL_ASSET,
    SOURCE_TYPE_LOCAL_RESOURCE,
    SOURCE_TYPE_DATA,
    SOURCE_TYPE_QUALIFIED_RESOURCE
})
public @interface SourceUriType {

  int SOURCE_TYPE_UNKNOWN = -1;
  int SOURCE_TYPE_NETWORK = 0;
  int SOURCE_TYPE_LOCAL_FILE = 1;
  int SOURCE_TYPE_LOCAL_VIDEO_FILE = 2;
  int SOURCE_TYPE_LOCAL_IMAGE_FILE = 3;
  int SOURCE_TYPE_LOCAL_CONTENT = 4;
  int SOURCE_TYPE_LOCAL_ASSET = 5;
  int SOURCE_TYPE_LOCAL_RESOURCE = 6;
  int SOURCE_TYPE_DATA = 7;
  int SOURCE_TYPE_QUALIFIED_RESOURCE = 8;
}
