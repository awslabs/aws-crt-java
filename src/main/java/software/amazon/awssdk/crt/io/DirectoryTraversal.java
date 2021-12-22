package software.amazon.awssdk.crt.io;

/**
 * This class wraps the directory traversal implementation provided by the CRT.
 */
public final class DirectoryTraversal {

    public static native void traverse(final String path, boolean recursive, final DirectoryTraversalHandler handler);

}
