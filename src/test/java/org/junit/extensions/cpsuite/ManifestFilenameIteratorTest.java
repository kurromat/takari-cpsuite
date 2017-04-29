package org.junit.extensions.cpsuite;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class ManifestFilenameIteratorTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder temporaryJarFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder temporaryClasspathFolder = new TemporaryFolder();

	private ManifestFilenameIterator iterator;

	@Test
	public void hasEntries() throws Exception {
		createDummyFiles(asList("dummy.class", "temp.class"));
		File classpathJar = createClasspathJar(temporaryFolder.getRoot().toURI().toString());

		ManifestFilenameIterator iterator = new ManifestFilenameIterator(classpathJar, false);

		assertThat(iterator, contains("dummy.class", "temp.class"));
	}

	@Test
	public void hasEntriesWithoutJarsWhenJarsIgnored() throws Exception {
		createDummyFiles(asList("dummy.class", "temp.class"));
		File jarWithClasses = createJarWithClasses(asList("classInJar1.class", "classInJar2.class"));
		String classPath = temporaryFolder.getRoot().toURI().toString() + " " + jarWithClasses.toURI();
		File classpathJar = createClasspathJar(classPath);

		ManifestFilenameIterator iterator = new ManifestFilenameIterator(classpathJar, false);
		assertThat(iterator, contains("dummy.class", "temp.class"));
	}

	@Test
	public void hasEntriesWithJars() throws Exception {
		createDummyFiles(asList("dummy.class", "temp.class"));
		File jarWithClasses = createJarWithClasses(asList("classInJar1.class", "classInJar2.class"));
		String classPath = temporaryFolder.getRoot().toURI().toString() + " " + jarWithClasses.toURI();
		File classpathJar = createClasspathJar(classPath);

		ManifestFilenameIterator iterator = new ManifestFilenameIterator(classpathJar, true);

		assertThat(iterator, contains("dummy.class", "temp.class", "org/classInJar1.class", "org/classInJar2.class"));
	}

	@Test
	public void handlesEmptyClasspathInJar() throws Exception {
		File classpathJar = createClasspathJar("");
		ManifestFilenameIterator iterator = new ManifestFilenameIterator(classpathJar, false);
		assertThat(iterator, is(Matchers.<String>emptyIterable()));
	}

	@Test
	public void handlesMissingClasspathInJar() throws Exception {
		File classpathJar = createClasspathJar(null);
		ManifestFilenameIterator iterator = new ManifestFilenameIterator(classpathJar, false);
		assertThat(iterator, is(Matchers.<String>emptyIterable()));
	}


	@Test
	public void recursiveFilenameIterator_doesNotIgnoreJarFiles() throws Exception {
		RecursiveFilenameIterator resources = new RecursiveFilenameIterator(Paths.get(this.getClass().getClassLoader().getResource("testfolder").toURI()).toFile());
		assertThat(resources, contains("classpath.jar"));
	}

	private File createClasspathJar(String classPath) throws IOException {
		Path jarPath = Paths.get(temporaryClasspathFolder.getRoot().toString(), "testjar.jar");
		try (FileSystem jarFS = FileSystems.newFileSystem(URI.create("jar:" + jarPath.toUri()), Collections.singletonMap("create", "true"), null)) {
			Files.createDirectories(jarFS.getPath("META-INF/"));
			try (OutputStream outputStream = Files.newOutputStream(jarFS.getPath("META-INF/MANIFEST.MF"))) {
				Manifest mf = new Manifest();
				mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
				if (classPath != null) {
					mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, classPath);
				}
				mf.write(outputStream);
			}
		}
		return jarPath.toFile();
	}

	private void createDummyFiles(List<String> dummyFiles) throws IOException {
		for (String dummyFile : dummyFiles) {
			temporaryFolder.newFile(dummyFile);
		}
	}

	private File createJarWithClasses(List<String> files) throws IOException {
		Path jarPath = Paths.get(temporaryJarFolder.getRoot().toString(), "content.jar");
		try (FileSystem jarFS = FileSystems.newFileSystem(URI.create("jar:" + jarPath.toUri()), Collections.singletonMap("create", "true"), null)) {
			Files.createDirectories(jarFS.getPath("org/"));
			for (String file : files) {
				Files.createFile(jarFS.getPath("org/", file));
			}
		}
		return jarPath.toFile();
	}

}