package org.junit.extensions.cpsuite;

import org.junit.extensions.cpsuite.ClasspathSuite.IncludeManifest;
import org.junit.runner.RunWith;

@RunWith(ClasspathSuite.class)
@IncludeManifest(true)
public class AllTestRunner {
}
