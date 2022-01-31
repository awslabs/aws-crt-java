package software.amazon.awssdk.crt.io;

/**
 * Supplied during calls to DirectoryTraversal.traverse() as each entry is encountered.
 */
public class DirectoryEntry {

    private String path;
    private String relativePath;
    private boolean isDirectory;
    private boolean isSymLink;
    private boolean isFile;
    private long fileSize;

    /**
     * Sets the absolute path of this entry
     * @param path path
     * @return this entry object
     */
    public DirectoryEntry withPath(final String path) {
        this.path = path;
        return this;
    }

    /**
     * @return the absolute path of this entry
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Sets the path relative to the current working directory
     * @param relativePath relative path
     * @return this entry object
     */
    public DirectoryEntry withRelativePath(final String relativePath) {
        this.relativePath = relativePath;
        return this;
    }

    /**
     * @return the path relative to the current working directory
     */
    public String getRelativePath() {
        return this.relativePath;
    }

    /**
     * Sets the isDirectory flag, meaning this entry corresponds to a directory
     * @param isDirectory isDirectory
     * @return this entry object
     */
    public DirectoryEntry withIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
        return this;
    }

    /**
     * @return true if this entry corresponds to a directory
     */
    public boolean isDirectory() {
        return this.isDirectory;
    }

    /**
     * Sets the isSymLink flag, meaning this entry corresponds to a symbolic link
     * @param isSymLink isSymLink
     * @return this entry object
     */
    public DirectoryEntry withIsSymLink(boolean isSymLink) {
        this.isSymLink = isSymLink;
        return this;
    }

    /**
     * @return true if this entry corresponds to a symbolic link.
     */
    public boolean isSymLink() {
        return this.isSymLink;
    }

    /**
     * Sets the isFile flag, meaning this entry corresponds to a file
     * @param isFile isFile
     * @return this entry object
     */
    public DirectoryEntry withIsFile(boolean isFile) {
        this.isFile = isFile;
        return this;
    }

    /**
     * @return true if this entry corresponds to a file
     */
    public boolean isFile() {
        return this.isFile;
    }

    /**
     * Sets the file size corresponding to this entry
     * @param fileSize file size in bytes
     * @return this entry object
     */
    public DirectoryEntry withFileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    /**
     * @return the size of the file
     */
    public long getFileSize() {
        return this.fileSize;
    }
}
