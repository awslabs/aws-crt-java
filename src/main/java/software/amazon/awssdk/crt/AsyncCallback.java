/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt;

import java.util.concurrent.CompletableFuture;

/**
 * Async io completion abstraction used by the native mqtt layer.  We moved to using futures directly but
 * that might have been a mistake and we should consider moving back to this for our other async
 * operations that cross the managed/native boundary
 */
public interface AsyncCallback {

    static <T> AsyncCallback wrapFuture(CompletableFuture<T> future, T value) {
        return new AsyncCallback() {
            @Override
            public void onSuccess() {
                future.complete(value);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(Object val) {
                future.complete((T)(val));
            }

            @Override
            public void onFailure(Throwable reason) {
                future.completeExceptionally(reason);
            }
        };
    }

    void onSuccess();
    void onSuccess(Object value);
    void onFailure(Throwable reason);
}
