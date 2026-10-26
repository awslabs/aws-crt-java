/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

/**
 * Handler invoked during calls to DirectoryTraversal.traverse() as each entry is encountered.
 */
public interface DirectoryTraversalHandler {

    /**
     * Invoked during calls to DirectoryTraversal.traverse() as each entry is encountered.
     *
     * @param directoryEntry Information about the directory entry encountered
     * @return true to continue the traversal, or false to abort it
     */
    boolean onDirectoryEntry(final DirectoryEntry directoryEntry);
}
