package io.github.turtleisaac.pokeditor.framework;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * {@link Directory#delete()} is a boolean-returning operation, so its contract is entirely about
 * that boolean: it is an answer to "did I remove this directory?", and callers branch on it.
 * A method which returns {@code true} unconditionally is not a weaker version of that contract,
 * it is the absence of one - every caller then believes a failed cleanup succeeded.
 */
class DirectoryTest
{
    @TempDir
    Path temp;

    /**
     * Success is reported as success, and success means the tree is actually gone - including
     * the nested content, which a non-recursive delete could never remove.
     */
    @Test
    @DisplayName("deleting a populated tree removes every file in it and reports true")
    void deletingAPopulatedTreeSucceedsAndEmptiesIt() throws IOException
    {
        Path root = temp.resolve("root");
        Files.createDirectories(root.resolve("nested/deeper"));
        Files.writeString(root.resolve("top.txt"), "top");
        Files.writeString(root.resolve("nested/middle.txt"), "middle");
        Files.writeString(root.resolve("nested/deeper/bottom.txt"), "bottom");

        boolean deleted = new Directory(root.toString()).delete();

        assertThat(deleted).as("return value of delete() on a tree it was able to remove").isTrue();
        assertThat(root).as("the tree after a delete which reported success").doesNotExist();
    }

    @Test
    @DisplayName("deleting an empty directory removes it and reports true")
    void deletingAnEmptyDirectorySucceeds() throws IOException
    {
        Path root = Files.createDirectory(temp.resolve("empty"));

        boolean deleted = new Directory(root.toString()).delete();

        assertThat(deleted).isTrue();
        assertThat(root).doesNotExist();
    }

    /**
     * Nothing was deleted, so the answer is no. A caller that logs "cleanup failed" on false has
     * to be told the truth here, and it must find that out through a return value rather than an
     * exception - the callers of this run during shutdown paths.
     */
    @Test
    @DisplayName("a directory that does not exist is reported as not deleted rather than throwing")
    void absentDirectoryIsReportedNotThrown()
    {
        Directory absent = new Directory(temp.resolve("never-created").toString());

        assertThatCode(absent::delete).doesNotThrowAnyException();
        assertThat(absent.delete()).as("return value of delete() on a path that was never there").isFalse();
    }

    /**
     * A path which is not a directory at all cannot be cleared as one. Reporting true here would
     * tell the caller a directory was emptied when the path on disk is untouched.
     */
    @Test
    @DisplayName("a path that is a plain file is reported as not deleted and is left on disk")
    void plainFileIsReportedNotDeleted() throws IOException
    {
        Path file = Files.writeString(temp.resolve("not-a-directory.txt"), "contents");

        boolean deleted = new Directory(file.toString()).delete();

        assertThat(deleted).as("return value of delete() on a path which is not a directory").isFalse();
        assertThat(file).as("a file which delete() declined to remove").exists();
    }

    /**
     * The return value has to be sensitive to a failure buried anywhere in the tree, not just at
     * the top. Where the platform lets us make one child undeletable, a delete which cannot
     * remove everything must not claim it did.
     * <p>
     * Denying write permission on the parent is what stops a child being unlinked on POSIX; it
     * has no effect for a superuser, so where the check cannot be set up the assertion about the
     * top-level directory still stands.
     */
    @Test
    @DisplayName("a delete which could not remove everything does not report success")
    void partialFailureIsNotReportedAsSuccess() throws IOException
    {
        Path root = Files.createDirectory(temp.resolve("locked-root"));
        Path locked = Files.createDirectory(root.resolve("locked"));
        Path trapped = Files.writeString(locked.resolve("trapped.txt"), "cannot go");

        File lockedFile = locked.toFile();
        boolean readOnlyApplied = lockedFile.setWritable(false, false);

        boolean deleted = new Directory(root.toString()).delete();

        // restore permissions first so the temp dir can be torn down whatever the outcome
        lockedFile.setWritable(true, false);

        if (readOnlyApplied && Files.exists(trapped))
        {
            assertThat(deleted).as("return value when a file inside the tree survived the delete").isFalse();
            assertThat(root).as("the tree the delete could not finish").exists();
        }
        else
        {
            // the platform (or a superuser) let the delete through - then it really did succeed
            assertThat(deleted).as("return value when the whole tree was in fact removed").isTrue();
            assertThat(root).doesNotExist();
        }
    }
}
