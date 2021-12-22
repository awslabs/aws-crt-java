package software.amazon.awssdk.crt.io;

/**
 * Handler invoked during calls to DirectoryTraversal.traverse() as each entry is encountered.
 */
public interface DirectoryTraversalHandler {

    /**
     * Invoked during calls to DirectoryTraversal.traverse() as each entry is encountered.
     * Return true to continue the traversal, or alternatively, if you have a reason to abort then
     * return false.
     *
     * @param path Absolute path to the entry from the current process root.
     * @param relativePath Path to the entry relative to the current working directory.
     * @param isDirectory entry corresponds to a directory.
     * @param isSymLink entry corresponds to a symlink.
     * @param isFile entry corresponds to a file.
     * @param fileSize size of the file.
     * @return
     */
    boolean onDirectoryEntry(final String path,
                             final String relativePath,
                             boolean isDirectory,
                             boolean isSymLink,
                             boolean isFile,
                             long fileSize);

}
