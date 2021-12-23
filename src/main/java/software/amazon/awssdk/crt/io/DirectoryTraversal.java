package software.amazon.awssdk.crt.io;

/**
 * This class wraps the directory traversal implementation provided by the CRT.
 */
public final class DirectoryTraversal {

    /**
     * Traverse a directory starting at the path provided.
     * If you want the traversal to recurse the entire directory, pass recursive as true. Passing false for this parameter
     * will only iterate the contents of the directory, but will not descend into any directories it encounters.
     *
     * If recursive is set to true, the traversal is performed post-order, depth-first
     * (for practical reasons such as deleting a directory that contains subdirectories or files).
     *
     * @param path directory to traverse.
     * @param recursive true to recurse the entire directory, false will only iterate the path specified
     * @param handler callback to invoke for each file or directory found during the traversal.
     */
    public static void traverse(final String path, boolean recursive, final DirectoryTraversalHandler handler) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }

        crtTraverse(path, recursive, handler);
    }

    private static native void crtTraverse(final String path, boolean recursive, final DirectoryTraversalHandler handler);
}
