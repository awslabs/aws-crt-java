/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.auth.credentials;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface to allow for dynamic sourcing (i.e. per fetch-credentials request submitted to Cognito) of Cognito login
 * token pairs.  It is *critical* to follow the guidance given in the documentation for `startLoginTokenFetch`
 */
public interface CognitoLoginTokenSource {

    /**
     * Method that a Cognito credentials provider will invoke before sending a fetch credentials
     * request to Cognito.  The CognitoLoginTokenPairs that the future gets completed with are joined
     * with the (static) CognitoLoginTokenPairs that were specified in the credential provider configuration
     * on construction.  The merged set of CognitoLoginTokenPairs are added to the HTTP request sent
     * to Cognito that sources credentials.
     *
     * You must follow several guidelines to properly use this feature; not following these guidelines can result
     * in deadlocks, poor performance, or other undesirable behavior.
     *
     * 1. If you use this feature, you must complete the future or the underlying connection attempt will hang forever.
     * Credentials sourcing is halted until the future gets completed.  If something goes wrong during
     * login token sourcing, complete the future exceptionally.
     *
     * 2. You must not block or wait for asynchronous operations in this function.  This function is invoked from a CRT
     * event loop thread, and the event loop is halted until this function is returned from.  If you need to perform
     * an asynchronous or non-trivial operation in order to source the necessary login token pairs, then you must
     * ensure that sourcing task executes on another thread.  The easiest way to do this would be to pass the future
     * to a sourcing task that runs on an external executor.
     *
     * 3. No attempt is made to de-duplicate login keys.  If the final, unioned set of login token pairs contains
     * multiple pairs with the same key, then which one of the duplicates gets used is not well-defined.  For correct
     * behavior, you must ensure there can be no duplicates.
     */
    void startLoginTokenFetch(CompletableFuture<List<CognitoCredentialsProvider.CognitoLoginTokenPair>> tokenFuture);

}
