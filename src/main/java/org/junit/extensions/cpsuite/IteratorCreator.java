package org.junit.extensions.cpsuite;

import java.io.File;
import java.io.IOException;

public class IteratorCreator {
	private boolean searchInJars;
	private boolean parseManifest;

	public IteratorCreator(boolean searchInJars, boolean parseManifest) {
		this.searchInJars = searchInJars;
		this.parseManifest = parseManifest;
	}

	public FilenameIterator createFor(File fileInClassPath) {
		if (parseManifest && isClasspathJarFile(fileInClassPath)) {
			return new ManifestFilenameIterator(fileInClassPath, iteratorCreatorWithoutManifestSearching());
		} else if (searchInJars && isJarFile(fileInClassPath)) {
			try {
				return new JarFilenameIterator(fileInClassPath);
			} catch (IOException e) {
				// Don't iterate unavailable jar files
				e.printStackTrace();
			}

		} else if (fileInClassPath.isDirectory()) {
			return new RecursiveFilenameIterator(fileInClassPath);
		}
		return new NullIterator();
	}

	private IteratorCreator iteratorCreatorWithoutManifestSearching() {
		return new IteratorCreator(searchInJars, false);
	}

	private boolean isClasspathJarFile(File classRoot) {
		return classRoot.getName().equals("classpath.jar");
	}

	private boolean isJarFile(File classRoot) {
		return classRoot.getName().endsWith(".jar") || classRoot.getName().endsWith(".JAR");
	}
}
