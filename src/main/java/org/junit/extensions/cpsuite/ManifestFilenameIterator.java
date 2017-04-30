package org.junit.extensions.cpsuite;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class ManifestFilenameIterator extends FilenameIterator {


	private List<FilenameIterator> iterators;
	private int index = 0;
	private IteratorCreator iteratorCreator;

	ManifestFilenameIterator(File file, IteratorCreator iteratorCreator) {
		this.iteratorCreator = iteratorCreator;
		iterators = new ArrayList<>();
		parseClasspathJar(file);

	}

	private void parseClasspathJar(File file) {
		try {
			Attributes mainAttributes = new JarFile(file).getManifest().getMainAttributes();
			if (mainAttributes.containsKey(Attributes.Name.CLASS_PATH)) {
				String[] classPathParts = mainAttributes.getValue(Attributes.Name.CLASS_PATH).split(" ");
				List<File> files = convertToFiles(classPathParts);
				for (File fileInClassPath : files) {
					iterators.add(iteratorCreator.createFor(fileInClassPath));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<File> convertToFiles(String[] classPathParts) {
		List<File> files = new ArrayList<>();
		for (String classPathPart : classPathParts) {
			if (!classPathPart.isEmpty()) {
				files.add(Paths.get(URI.create(classPathPart)).toFile());
			}
		}
		return files;
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	public boolean hasNext() {
		if (index >= iterators.size()) {
			return false;
		}
		if (currentIterator().hasNext()) {
			return true;
		}
		index++;
		return hasNext();
	}

	private Iterator<String> currentIterator() {
		return iterators.get(index);
	}

	@Override
	public String next() {
		if (hasNext()) {
			return currentIterator().next();
		}
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new RuntimeException("not implemented");
	}
}
