/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JavaTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

/**
 * @author DIOGO
 */
public class FileUtils {

    static public boolean accept(File f) {

        if (f != null) {
            if (!f.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    static public String getExtension(File f) {
        String fileName = f.getAbsolutePath();
        int mid = fileName.lastIndexOf(".");
        return fileName.substring(mid + 1, fileName.length());
    }

    static public List<File> getFileListing(
            File aStartingDir) throws FileNotFoundException {
        validateDirectory(aStartingDir);
        List<File> result = getFileListingNoSort(aStartingDir);
        Collections.sort(result);
        return result;
    }

    static public List<File> getFileListingNoSort(
            File aStartingDir) {
        List<File> result = new ArrayList<>();
        File[] filesAndDirs = aStartingDir.listFiles();
        List<File> filesDirs = Arrays.asList(filesAndDirs);
        for (File file : filesDirs) {
            result.add(file); //always add, even if directory
            if (!file.isFile()) {
                //must be a directory
                //recursive call!
                List<File> deeperList = getFileListingNoSort(file);
                result.addAll(deeperList);
            }
        }
        return result;
    }

    static private void validateDirectory(
            File aDirectory
    ) throws FileNotFoundException {
        if (aDirectory == null) {
            throw new IllegalArgumentException("Directory should not be null.");
        }
        if (!aDirectory.exists()) {
            throw new FileNotFoundException("Directory does not exist: " + aDirectory);
        }
        if (!aDirectory.isDirectory()) {
            throw new IllegalArgumentException("Is not a directory: " + aDirectory);
        }
        if (!aDirectory.canRead()) {
            throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
        }
    }

    /**
     * Copies a directory.
     * NOTE: This method is not thread-safe.
     *
     * @param source the directory to copy from
     * @param target the directory to copy into
     * @throws java.io.IOException if an I/O error occurs
     */
    public static void copyDirectory(final Path source, final Path target) throws IOException {
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes sourceBasic)
                            throws IOException {
                        Path targetDir = Files.createDirectories(target.resolve(source.relativize(dir)));
                        AclFileAttributeView acl = Files.getFileAttributeView(dir, AclFileAttributeView.class);
                        if (acl != null) {
                            Files.getFileAttributeView(targetDir, AclFileAttributeView.class).setAcl(acl.getAcl());
                        }

                        DosFileAttributeView dosAttrs = Files.getFileAttributeView(dir,
                                DosFileAttributeView.class);
                        if (dosAttrs != null) {
                            DosFileAttributes sourceDosAttrs = dosAttrs.readAttributes();
                            DosFileAttributeView targetDosAttrs = Files.getFileAttributeView(targetDir,
                                    DosFileAttributeView.class);
                            targetDosAttrs.setArchive(sourceDosAttrs.isArchive());
                            targetDosAttrs.setHidden(sourceDosAttrs.isHidden());
                            targetDosAttrs.setReadOnly(sourceDosAttrs.isReadOnly());
                            targetDosAttrs.setSystem(sourceDosAttrs.isSystem());
                        }

                        FileOwnerAttributeView ownerAttrs = Files.getFileAttributeView(dir,
                                FileOwnerAttributeView.class);
                        if (ownerAttrs != null) {
                            FileOwnerAttributeView targetOwner = Files.getFileAttributeView(targetDir,
                                    FileOwnerAttributeView.class);
                            targetOwner.setOwner(ownerAttrs.getOwner());
                        }

                        PosixFileAttributeView posixAttrs = Files.getFileAttributeView(dir,
                                PosixFileAttributeView.class);
                        if (posixAttrs != null) {
                            PosixFileAttributes sourcePosix = posixAttrs.readAttributes();
                            PosixFileAttributeView targetPosix = Files.getFileAttributeView(targetDir,
                                    PosixFileAttributeView.class);
                            targetPosix.setPermissions(sourcePosix.permissions());
                            targetPosix.setGroup(sourcePosix.group());
                        }

                        UserDefinedFileAttributeView userAttrs = Files.getFileAttributeView(dir,
                                UserDefinedFileAttributeView.class);
                        if (userAttrs != null) {
                            UserDefinedFileAttributeView targetUser = Files.getFileAttributeView(targetDir,
                                    UserDefinedFileAttributeView.class);
                            for (String key : userAttrs.list()) {
                                ByteBuffer buffer = ByteBuffer.allocate(userAttrs.size(key));
                                userAttrs.read(key, buffer);
                                buffer.flip();
                                targetUser.write(key, buffer);
                            }
                        }

                        // Must be done last, otherwise last-modified time may be wrong
                        BasicFileAttributeView targetBasic = Files.getFileAttributeView(targetDir,
                                BasicFileAttributeView.class);
                        targetBasic.setTimes(sourceBasic.lastModifiedTime(), sourceBasic.lastAccessTime(),
                                sourceBasic.creationTime());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, target.resolve(source.relativize(file)),
                                StandardCopyOption.COPY_ATTRIBUTES);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                        throw e;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                        if (e != null) {
                            throw e;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    /**
     * Deletes a folder's content recursively.
     *
     * @param BEInterfaceDir The folder whose contents must the deleted, it must be a folder.
     */
    public static void deleteContents(File BEInterfaceDir) {
        if (BEInterfaceDir.isDirectory()) {

            File[] files = BEInterfaceDir.listFiles();

            for (File f : files) {
                if (f.isDirectory()) {
                    deleteContents(f);
                }

                f.delete();
            }
        }
    }
}
