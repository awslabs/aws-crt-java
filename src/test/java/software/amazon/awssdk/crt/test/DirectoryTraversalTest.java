package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.io.DirectoryEntry;
import software.amazon.awssdk.crt.io.DirectoryTraversal;
import software.amazon.awssdk.crt.io.DirectoryTraversalHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class DirectoryTraversalTest extends CrtTestFixture {

    private static final String FILE_CONTENT = "TestContent";
    private static final int FILE_COUNT = 100;
    private static final int DIRECTORY_COUNT = 10;

    class DirectoryStructureHelper implements AutoCloseable {

        private Path rootDir;
        private List<Path> directories = new ArrayList<>();
        private Set<String> files = new HashSet<>();

        /**
         * Creates a sample directory structure used to test directory traversal.
         * Calling close() deletes the contents created by this object.
         */
        public DirectoryStructureHelper() throws IOException {
            // creates a temporary directory with a number of files
            rootDir = Files.createTempDirectory("DirectoryTraversalTest").toRealPath();

            for (int i = 0; i < DIRECTORY_COUNT; i++) {
                final Path subDirectory = createDirectory(rootDir, "SubDir_" + i);
                createFiles(subDirectory);
            }
        }

        public List<Path> getDirectories() {
            return directories;
        }

        public Set<String> getFiles() {
            return files;
        }

        public String getRootDirectory() {
            return rootDir.toFile().getAbsolutePath();
        }

        private Path createDirectory(final Path parent, final String directoryName) {
            final File directory = new File(parent.toFile(), directoryName);
            directory.mkdirs();
            final Path path = directory.toPath();
            directories.add(path);
            return path;
        }

        private void createFiles(final Path parent) throws IOException {
            for (int i = 0; i < FILE_COUNT; i++) {
                final File file = new File(parent.toFile(), "File_" + i);
                files.add(file.getAbsolutePath());
                assertTrue(file.createNewFile());
                try (final FileWriter writer = new FileWriter(file)) {
                    writer.write(FILE_CONTENT);
                }
            }
        }

        /**
         * Deletes the files and directories created by this object.
         */
        @Override
        public void close() throws Exception {
            // deletes all files created by this test
            for (final String file : files) {
                Files.delete(Paths.get(file));
            }

            // deletes the directories
            for (final Path directory : directories) {
                Files.delete(directory);
            }

            // deletes the root temporary dir
            Files.delete(rootDir);

            directories.clear();
            files.clear();
        }
    }

    @Test
    public void testTraverseDirectoryFlat() throws Exception {

        try (final DirectoryStructureHelper directoryStructure = new DirectoryStructureHelper()) {
            // traverse dir using CRT
            Set<String> entries = new HashSet<>();

            DirectoryTraversal.traverse(directoryStructure.getRootDirectory(), false, new DirectoryTraversalHandler() {
                @Override
                public boolean onDirectoryEntry(final DirectoryEntry directoryEntry) {
                    entries.add(directoryEntry.getPath());
                    assertTrue(directoryEntry.isDirectory());
                    assertTrue(!directoryEntry.isSymLink());
                    assertTrue(!directoryEntry.isFile());

                    return true;
                }
            });

            assertEquals(directoryStructure.getDirectories().size(), entries.size());
            for (final Path directory : directoryStructure.getDirectories()) {
                assertTrue(entries.contains(directory.toFile().getAbsolutePath()));
            }

            // leaving this try() scope causes the close() method of directoryStructure helper to be called,
            // which cleans up the contents created by this test.
        }
    }

    @Test
    public void testTraverseDirectoryRecursive() throws Exception {

        try (final DirectoryStructureHelper directoryStructure = new DirectoryStructureHelper()) {
            // traverse dir using CRT
            Set<String> directoryEntries = new HashSet<>();
            Set<String> fileEntries = new HashSet<>();

            DirectoryTraversal.traverse(directoryStructure.getRootDirectory(), true, new DirectoryTraversalHandler() {
                @Override
                public boolean onDirectoryEntry(final DirectoryEntry directoryEntry) {
                    if (directoryEntry.isDirectory()) {
                        directoryEntries.add(directoryEntry.getPath());
                        assertTrue(!directoryEntry.isFile());
                    } else {
                        fileEntries.add(directoryEntry.getPath());
                        assertTrue(directoryEntry.isFile());
                        assertEquals(FILE_CONTENT.length(), directoryEntry.getFileSize());
                    }

                    return true;
                }
            });

            assertEquals(directoryStructure.getDirectories().size(), directoryEntries.size());
            for (final Path directory : directoryStructure.getDirectories()) {
                assertTrue(directoryEntries.contains(directory.toFile().getAbsolutePath()));
            }

            assertEquals(directoryStructure.getFiles().size(), fileEntries.size());
            for (final String file : directoryStructure.getFiles()) {
                assertTrue(fileEntries.contains(file));
            }
        }
    }

    @Test
    public void testTraverseDirectoryCancellation() throws Exception {

        try (final DirectoryStructureHelper directoryStructure = new DirectoryStructureHelper()) {
            // traverse dir using CRT
            Set<String> entries = new HashSet<>();

            try {
                DirectoryTraversal.traverse(directoryStructure.getRootDirectory(), false, new DirectoryTraversalHandler() {
                    @Override
                    public boolean onDirectoryEntry(final DirectoryEntry directoryEntry) {
                        entries.add(directoryEntry.getPath());
                        assertTrue(directoryEntry.isDirectory());
                        assertTrue(!directoryEntry.isSymLink());
                        assertTrue(!directoryEntry.isFile());

                        return false;
                    }
                });

                assertTrue("Cancellation should have caused an exception", false);
            } catch (final RuntimeException ex) {
                // should return only one entry, because the callback returned false asking the traversal to be cancelled.
                assertEquals(1, entries.size());
            }
        }
    }

    @Test
    public void testTraverseDirectoryError() throws Exception {

        Set<String> entries = new HashSet<>();

        try {

            DirectoryTraversal.traverse("-", false, new DirectoryTraversalHandler() {
                @Override
                public boolean onDirectoryEntry(final DirectoryEntry directoryEntry) {
                    entries.add(directoryEntry.getPath());
                    return true;
                }
            });
            assertTrue("Cancellation should have caused an exception", false);
        } catch (final RuntimeException ex) {
            assertEquals(0, entries.size());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraverseDirectoryNullPath() {

        DirectoryTraversal.traverse(null, false, new DirectoryTraversalHandler() {
            @Override
            public boolean onDirectoryEntry(final DirectoryEntry directoryEntry) {
                return true;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraverseDirectoryNullHandler() {

        DirectoryTraversal.traverse("-", false, null);
    }

    public void testTraverseDirectoryCallbackThrowingException() throws Exception {

        final String exceptionMessage = "THROWN BY TEST CALLBACK";

        try (final DirectoryStructureHelper directoryStructure = new DirectoryStructureHelper()) {
            // traverse dir using CRT
            Set<String> entries = new HashSet<>();

            try {
                DirectoryTraversal.traverse(directoryStructure.getRootDirectory(), false, new DirectoryTraversalHandler() {
                    @Override
                    public boolean onDirectoryEntry(final DirectoryEntry directoryEntry) {
                        throw new RuntimeException(exceptionMessage);
                    }
                });

                fail("Exception is expected to propagate through callback");
            } catch (final RuntimeException ex) {
                // exception thrown by DirectoryTraversal.traverse() is expected,
                // in order to notify user about incomplete results due to traversal cancellation.
                assertEquals(exceptionMessage, ex.getMessage());
            }
        }
    }
}
