package org.codehaus.plexus.util;

/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Base class for testcases doing tests with files.
 *
 * @author Dan T. Tran
 * @version $Id: $Id
 * @since 3.4.0
 */
class DirectoryScannerTest extends FileBasedTestCase {
    private static final String testDir = getTestDirectory().getPath();

    /**
     * <p>setUp.</p>
     */
    @BeforeEach
    void setUp() {
        try {
            FileUtils.deleteDirectory(testDir);
        } catch (IOException e) {
            fail("Could not delete directory " + testDir);
        }
    }

    /**
     * <p>testCrossPlatformIncludesString.</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.net.URISyntaxException if any.
     */
    @Test
    void crossPlatformIncludesString() throws IOException, URISyntaxException {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getTestResourcesDir() + File.separator + "directory-scanner").getCanonicalFile());

        String fs;
        if (File.separatorChar == '/') {
            fs = "\\";
        } else {
            fs = "/";
        }

        ds.setIncludes(new String[] {"foo" + fs});
        ds.addDefaultExcludes();
        ds.scan();

        String[] files = ds.getIncludedFiles();
        assertEquals(1, files.length);
    }

    /**
     * <p>testCrossPlatformExcludesString.</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.net.URISyntaxException if any.
     */
    @Test
    void crossPlatformExcludesString() throws IOException, URISyntaxException {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getTestResourcesDir() + File.separator + "directory-scanner").getCanonicalFile());
        ds.setIncludes(new String[] {"**"});

        String fs;
        if (File.separatorChar == '/') {
            fs = "\\";
        } else {
            fs = "/";
        }

        ds.setExcludes(new String[] {"foo" + fs});
        ds.addDefaultExcludes();
        ds.scan();

        String[] files = ds.getIncludedFiles();
        assertEquals(0, files.length);
    }

    private String getTestResourcesDir() throws URISyntaxException {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        URL resource = cloader.getResource("test.txt");
        if (resource == null) {
            fail("Cannot locate test-resources directory containing 'test.txt' in the classloader.");
        }

        File file = new File(new URI(resource.toExternalForm()).normalize().getPath());

        return file.getParent();
    }

    private void createTestFiles() throws IOException {
        FileUtils.mkdir(testDir);
        this.createFile(new File(testDir + "/scanner1.dat"), 0);
        this.createFile(new File(testDir + "/scanner2.dat"), 0);
        this.createFile(new File(testDir + "/scanner3.dat"), 0);
        this.createFile(new File(testDir + "/scanner4.dat"), 0);
        this.createFile(new File(testDir + "/scanner5.dat"), 0);
    }

    /**
     * Check if 'src/test/resources/symlinks/src/sym*' test files (start with 'sym') exist and are symlinks.<br>
     * On some OS (like Windows 10), the 'git clone' requires to be executed with admin permissions and the
     * 'core.symlinks=true' git option.
     *
     * @return true If files here and symlinks, false otherwise
     */
    private boolean checkTestFilesSymlinks() {
        File symlinksDirectory = new File("src/test/resources/symlinks/src");
        try {
            List<String> symlinks =
                    FileUtils.getFileAndDirectoryNames(symlinksDirectory, "sym*", null, true, true, true, true);
            if (symlinks.isEmpty()) {
                throw new IOException("Symlinks files/directories are not present");
            }
            for (String symLink : symlinks) {
                if (!Files.isSymbolicLink(Paths.get(symLink))) {
                    throw new IOException(String.format("Path is not a symlink: %s", symLink));
                }
            }
            return true;
        } catch (IOException e) {
            System.err.printf(
                    "The unit test '%s.%s' will be skipped, reason: %s%n",
                    this.getClass().getSimpleName(), getTestMethodName(), e.getMessage());
            System.out.printf("This test requires symlinks files in '%s' directory.%n", symlinksDirectory.getPath());
            System.out.println("On some OS (like Windows 10), files are present only if the clone/checkout is done"
                    + " in administrator mode, and correct (symlinks and not flat file/directory)"
                    + " if symlinks option are used (for git: git clone -c core.symlinks=true [url])");
            return false;
        }
    }

    /**
     * <p>testGeneral.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void general() throws IOException {
        this.createTestFiles();

        String includes = "scanner1.dat,scanner2.dat,scanner3.dat,scanner4.dat,scanner5.dat";
        String excludes = "scanner1.dat,scanner2.dat";

        List<File> fileNames = FileUtils.getFiles(new File(testDir), includes, excludes, false);

        assertEquals(3, fileNames.size(), "Wrong number of results.");
        assertTrue(fileNames.contains(new File("scanner3.dat")), "3 not found.");
        assertTrue(fileNames.contains(new File("scanner4.dat")), "4 not found.");
        assertTrue(fileNames.contains(new File("scanner5.dat")), "5 not found.");
    }

    /**
     * <p>testIncludesExcludesWithWhiteSpaces.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void includesExcludesWithWhiteSpaces() throws IOException {
        this.createTestFiles();

        String includes = "scanner1.dat,\n  \n,scanner2.dat  \n\r, scanner3.dat\n, \tscanner4.dat,scanner5.dat\n,";

        String excludes = "scanner1.dat,\n  \n,scanner2.dat  \n\r,,";

        List<File> fileNames = FileUtils.getFiles(new File(testDir), includes, excludes, false);

        assertEquals(3, fileNames.size(), "Wrong number of results.");
        assertTrue(fileNames.contains(new File("scanner3.dat")), "3 not found.");
        assertTrue(fileNames.contains(new File("scanner4.dat")), "4 not found.");
        assertTrue(fileNames.contains(new File("scanner5.dat")), "5 not found.");
    }

    /**
     * <p>testFollowSymlinksFalse.</p>
     */
    @Test
    void followSymlinksFalse() {
        assumeTrue(checkTestFilesSymlinks());

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File("src/test/resources/symlinks/src/"));
        ds.setFollowSymlinks(false);
        ds.scan();
        List<String> included = Arrays.asList(ds.getIncludedFiles());
        assertAlwaysIncluded(included);
        assertEquals(9, included.size());
        List<String> includedDirs = Arrays.asList(ds.getIncludedDirectories());
        assertTrue(includedDirs.contains("")); // w00t !
        assertTrue(includedDirs.contains("aRegularDir"));
        assertTrue(includedDirs.contains("symDir"));
        assertTrue(includedDirs.contains("symLinkToDirOnTheOutside"));
        assertTrue(includedDirs.contains("targetDir"));
        assertEquals(5, includedDirs.size());
    }

    private void assertAlwaysIncluded(List<String> included) {
        assertTrue(included.contains("aRegularDir" + File.separator + "aRegularFile.txt"));
        assertTrue(included.contains("targetDir" + File.separator + "targetFile.txt"));
        assertTrue(included.contains("fileR.txt"));
        assertTrue(included.contains("fileW.txt"));
        assertTrue(included.contains("fileX.txt"));
        assertTrue(included.contains("symR"));
        assertTrue(included.contains("symW"));
        assertTrue(included.contains("symX"));
        assertTrue(included.contains("symLinkToFileOnTheOutside"));
    }

    /**
     * <p>testFollowSymlinks.</p>
     */
    @Test
    void followSymlinks() {
        assumeTrue(checkTestFilesSymlinks());

        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File("src/test/resources/symlinks/src/"));
        ds.setFollowSymlinks(true);
        ds.scan();
        List<String> included = Arrays.asList(ds.getIncludedFiles());
        assertAlwaysIncluded(included);
        assertTrue(included.contains("symDir" + File.separator + "targetFile.txt"));
        assertTrue(included.contains("symLinkToDirOnTheOutside" + File.separator + "FileInDirOnTheOutside.txt"));
        assertEquals(11, included.size());

        List<String> includedDirs = Arrays.asList(ds.getIncludedDirectories());
        assertTrue(includedDirs.contains("")); // w00t !
        assertTrue(includedDirs.contains("aRegularDir"));
        assertTrue(includedDirs.contains("symDir"));
        assertTrue(includedDirs.contains("symLinkToDirOnTheOutside"));
        assertTrue(includedDirs.contains("targetDir"));
        assertEquals(5, includedDirs.size());
    }

    private void createTestDirectories() throws IOException {
        FileUtils.mkdir(testDir + File.separator + "directoryTest");
        FileUtils.mkdir(testDir + File.separator + "directoryTest" + File.separator + "testDir123");
        FileUtils.mkdir(testDir + File.separator + "directoryTest" + File.separator + "test_dir_123");
        FileUtils.mkdir(testDir + File.separator + "directoryTest" + File.separator + "test-dir-123");
        this.createFile(
                new File(testDir + File.separator + "directoryTest" + File.separator + "testDir123" + File.separator
                        + "file1.dat"),
                0);
        this.createFile(
                new File(testDir + File.separator + "directoryTest" + File.separator + "test_dir_123" + File.separator
                        + "file1.dat"),
                0);
        this.createFile(
                new File(testDir + File.separator + "directoryTest" + File.separator + "test-dir-123" + File.separator
                        + "file1.dat"),
                0);
    }

    /**
     * <p>testDirectoriesWithHyphens.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void directoriesWithHyphens() throws IOException {
        this.createTestDirectories();

        DirectoryScanner ds = new DirectoryScanner();
        String[] includes = {"**/*.dat"};
        String[] excludes = {""};
        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.setBasedir(new File(testDir + File.separator + "directoryTest"));
        ds.setCaseSensitive(true);
        ds.scan();

        String[] files = ds.getIncludedFiles();
        assertEquals(3, files.length, "Wrong number of results.");
    }

    /**
     * <p>testAntExcludesOverrideIncludes.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void antExcludesOverrideIncludes() throws IOException {
        printTestHeader();

        File dir = new File(testDir, "regex-dir");
        dir.mkdirs();

        String[] excludedPaths = {"target/foo.txt"};

        createFiles(dir, excludedPaths);

        String[] includedPaths = {"src/main/resources/project/target/foo.txt"};

        createFiles(dir, includedPaths);

        DirectoryScanner ds = new DirectoryScanner();

        String[] includes = {"**/target/*"};
        String[] excludes = {"target/*"};

        // This doesn't work, since excluded patterns refine included ones, meaning they operate on
        // the list of paths that passed the included patterns, and can override them.
        // String[] includes = {"**src/**/target/**/*" };
        // String[] excludes = { "**/target/**/*" };

        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.setBasedir(dir);
        ds.scan();

        assertInclusionsAndExclusions(ds.getIncludedFiles(), excludedPaths, includedPaths);
    }

    /**
     * <p>testAntExcludesOverrideIncludesWithExplicitAntPrefix.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void antExcludesOverrideIncludesWithExplicitAntPrefix() throws IOException {
        printTestHeader();

        File dir = new File(testDir, "regex-dir");
        dir.mkdirs();

        String[] excludedPaths = {"target/foo.txt"};

        createFiles(dir, excludedPaths);

        String[] includedPaths = {"src/main/resources/project/target/foo.txt"};

        createFiles(dir, includedPaths);

        DirectoryScanner ds = new DirectoryScanner();

        String[] includes = {SelectorUtils.ANT_HANDLER_PREFIX + "**/target/**/*" + SelectorUtils.PATTERN_HANDLER_SUFFIX
        };
        String[] excludes = {SelectorUtils.ANT_HANDLER_PREFIX + "target/**/*" + SelectorUtils.PATTERN_HANDLER_SUFFIX};

        // This doesn't work, since excluded patterns refine included ones, meaning they operate on
        // the list of paths that passed the included patterns, and can override them.
        // String[] includes = {"**src/**/target/**/*" };
        // String[] excludes = { "**/target/**/*" };

        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.setBasedir(dir);
        ds.scan();

        assertInclusionsAndExclusions(ds.getIncludedFiles(), excludedPaths, includedPaths);
    }

    /**
     * <p>testRegexIncludeWithExcludedPrefixDirs.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void regexIncludeWithExcludedPrefixDirs() throws IOException {
        printTestHeader();

        File dir = new File(testDir, "regex-dir");
        dir.mkdirs();

        String[] excludedPaths = {"src/main/foo.txt"};

        createFiles(dir, excludedPaths);

        String[] includedPaths = {"src/main/resources/project/target/foo.txt"};

        createFiles(dir, includedPaths);

        String regex = ".+/target.*";

        DirectoryScanner ds = new DirectoryScanner();

        String includeExpr = SelectorUtils.REGEX_HANDLER_PREFIX + regex + SelectorUtils.PATTERN_HANDLER_SUFFIX;

        String[] includes = {includeExpr};
        ds.setIncludes(includes);
        ds.setBasedir(dir);
        ds.scan();

        assertInclusionsAndExclusions(ds.getIncludedFiles(), excludedPaths, includedPaths);
    }

    /**
     * <p>testRegexExcludeWithNegativeLookahead.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void regexExcludeWithNegativeLookahead() throws IOException {
        printTestHeader();

        File dir = new File(testDir, "regex-dir");
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException ignored) {
        }

        dir.mkdirs();

        String[] excludedPaths = {"target/foo.txt"};

        createFiles(dir, excludedPaths);

        String[] includedPaths = {"src/main/resources/project/target/foo.txt"};

        createFiles(dir, includedPaths);

        String regex = "(?!.*src/).*target.*";

        DirectoryScanner ds = new DirectoryScanner();

        String excludeExpr = SelectorUtils.REGEX_HANDLER_PREFIX + regex + SelectorUtils.PATTERN_HANDLER_SUFFIX;

        String[] excludes = {excludeExpr};
        ds.setExcludes(excludes);
        ds.setBasedir(dir);
        ds.scan();

        assertInclusionsAndExclusions(ds.getIncludedFiles(), excludedPaths, includedPaths);
    }

    /**
     * <p>testRegexWithSlashInsideCharacterClass.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void regexWithSlashInsideCharacterClass() throws IOException {
        printTestHeader();

        File dir = new File(testDir, "regex-dir");
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException ignored) {
        }

        dir.mkdirs();

        String[] excludedPaths = {"target/foo.txt", "target/src/main/target/foo.txt"};

        createFiles(dir, excludedPaths);

        String[] includedPaths = {"module/src/main/target/foo.txt"};

        createFiles(dir, includedPaths);

        // NOTE: The portion "[^/]" is the interesting part of this pattern.
        String regex = "(?!((?!target/)[^/]+/)*src/).*target.*";

        DirectoryScanner ds = new DirectoryScanner();

        String excludeExpr = SelectorUtils.REGEX_HANDLER_PREFIX + regex + SelectorUtils.PATTERN_HANDLER_SUFFIX;

        String[] excludes = {excludeExpr};
        ds.setExcludes(excludes);
        ds.setBasedir(dir);
        ds.scan();

        assertInclusionsAndExclusions(ds.getIncludedFiles(), excludedPaths, includedPaths);
    }

    /**
     * Test that the directory scanning does not enter into not matching directories.
     *
     * @see <a href="https://github.com/codehaus-plexus/plexus-utils/issues/63">Issue #63</a>
     * @throws java.io.IOException if occurs an I/O error.
     */
    @Test
    void doNotScanUnnecesaryDirectories() throws IOException {
        createTestDirectories();

        // create additional directories 'anotherDir1', 'anotherDir2' and 'anotherDir3' with a 'file1.dat' file
        FileUtils.mkdir(testDir + File.separator + "directoryTest" + File.separator + "testDir123" + File.separator
                + "anotherDir1");
        FileUtils.mkdir(testDir + File.separator + "directoryTest" + File.separator + "test_dir_123" + File.separator
                + "anotherDir2");
        FileUtils.mkdir(testDir + File.separator + "directoryTest" + File.separator + "test-dir-123" + File.separator
                + "anotherDir3");

        this.createFile(
                new File(testDir + File.separator + "directoryTest" + File.separator + "testDir123" + File.separator
                        + "anotherDir1" + File.separator + "file1.dat"),
                0);
        this.createFile(
                new File(testDir + File.separator + "directoryTest" + File.separator + "test_dir_123" + File.separator
                        + "anotherDir2" + File.separator + "file1.dat"),
                0);
        this.createFile(
                new File(testDir + File.separator + "directoryTest" + File.separator + "test-dir-123" + File.separator
                        + "anotherDir3" + File.separator + "file1.dat"),
                0);

        String[] excludedPaths = {
            "directoryTest" + File.separator + "testDir123" + File.separator + "anotherDir1" + File.separator
                    + "file1.dat",
            "directoryTest" + File.separator + "test_dir_123" + File.separator + "anotherDir2" + File.separator
                    + "file1.dat",
            "directoryTest" + File.separator + "test-dir-123" + File.separator + "anotherDir3" + File.separator
                    + "file1.dat"
        };

        String[] includedPaths = {
            "directoryTest" + File.separator + "testDir123" + File.separator + "file1.dat",
            "directoryTest" + File.separator + "test_dir_123" + File.separator + "file1.dat",
            "directoryTest" + File.separator + "test-dir-123" + File.separator + "file1.dat"
        };

        final Set<String> scannedDirSet = new HashSet<>();

        DirectoryScanner ds = new DirectoryScanner() {
            @Override
            protected void scandir(File dir, String vpath, boolean fast) {
                scannedDirSet.add(dir.getName());
                super.scandir(dir, vpath, fast);
            }
        };

        // one '*' matches only ONE directory level
        String[] includes = {"directoryTest" + File.separator + "*" + File.separator + "file1.dat"};
        ds.setIncludes(includes);
        ds.setBasedir(new File(testDir));
        ds.scan();

        assertInclusionsAndExclusions(ds.getIncludedFiles(), excludedPaths, includedPaths);

        Set<String> expectedScannedDirSet =
                new HashSet<>(Arrays.asList("io", "directoryTest", "testDir123", "test_dir_123", "test-dir-123"));

        assertEquals(expectedScannedDirSet, scannedDirSet);
    }

    /**
     * <p>testIsSymbolicLink.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void isSymbolicLink() throws IOException {
        assumeTrue(checkTestFilesSymlinks());

        final File directory = new File("src/test/resources/symlinks/src");
        DirectoryScanner ds = new DirectoryScanner();
        assertTrue(ds.isSymbolicLink(directory, "symR"));
        assertTrue(ds.isSymbolicLink(directory, "symDir"));
        assertFalse(ds.isSymbolicLink(directory, "fileR.txt"));
        assertFalse(ds.isSymbolicLink(directory, "aRegularDir"));
    }

    /**
     * <p>testIsParentSymbolicLink.</p>
     *
     * @throws java.io.IOException if any.
     */
    @Test
    void isParentSymbolicLink() throws IOException {
        assumeTrue(checkTestFilesSymlinks());

        final File directory = new File("src/test/resources/symlinks/src");
        DirectoryScanner ds = new DirectoryScanner();
        assertFalse(ds.isParentSymbolicLink(directory, "symR"));
        assertFalse(ds.isParentSymbolicLink(directory, "symDir"));
        assertFalse(ds.isParentSymbolicLink(directory, "fileR.txt"));
        assertFalse(ds.isParentSymbolicLink(directory, "aRegularDir"));
        assertFalse(ds.isParentSymbolicLink(new File(directory, "aRegularDir"), "aRegulatFile.txt"));
        assertTrue(ds.isParentSymbolicLink(new File(directory, "symDir"), "targetFile.txt"));
        assertTrue(
                ds.isParentSymbolicLink(new File(directory, "symLinkToDirOnTheOutside"), "FileInDirOnTheOutside.txt"));
    }

    private void printTestHeader() {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        System.out.println("Test: " + ste.getMethodName());
    }

    private void assertInclusionsAndExclusions(String[] files, String[] excludedPaths, String... includedPaths) {
        Arrays.sort(files);

        System.out.println("Included files: ");
        for (String file : files) {
            System.out.println(file);
        }

        List<String> failedToExclude = new ArrayList<>();
        for (String excludedPath : excludedPaths) {
            String alt = excludedPath.replace('/', '\\');
            System.out.println("Searching for exclusion as: " + excludedPath + "\nor: " + alt);
            if (Arrays.binarySearch(files, excludedPath) > -1 || Arrays.binarySearch(files, alt) > -1) {
                failedToExclude.add(excludedPath);
            }
        }

        List<String> failedToInclude = new ArrayList<>();
        for (String includedPath : includedPaths) {
            String alt = includedPath.replace('/', '\\');
            System.out.println("Searching for inclusion as: " + includedPath + "\nor: " + alt);
            if (Arrays.binarySearch(files, includedPath) < 0 && Arrays.binarySearch(files, alt) < 0) {
                failedToInclude.add(includedPath);
            }
        }

        StringBuilder buffer = new StringBuilder();
        if (!failedToExclude.isEmpty()) {
            buffer.append("Should NOT have included:\n").append(StringUtils.join(failedToExclude.iterator(), "\n\t- "));
        }

        if (!failedToInclude.isEmpty()) {
            if (buffer.length() > 0) {
                buffer.append("\n\n");
            }

            buffer.append("Should have included:\n").append(StringUtils.join(failedToInclude.iterator(), "\n\t- "));
        }

        if (buffer.length() > 0) {
            fail(buffer.toString());
        }
    }

    private void createFiles(File dir, String... paths) throws IOException {
        for (String path1 : paths) {
            String path = path1.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            File file = new File(dir, path);

            if (path.endsWith(File.separator)) {
                file.mkdirs();
            } else {
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }

                createFile(file, 0);
            }
        }
    }
}
