package com.github.markusbernhardt.xmldoclet;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.DocumentationTool;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markusbernhardt.xmldoclet.xjc.Root;

/**
 * Base class for all tests.
 * 
 * @author markus
 */
public class AbstractTestParent {

	private final static Logger log = LoggerFactory.getLogger(AbstractTestParent.class);

	public Root executeJavadoc(String extendedClassPath, String[] sourcePaths, String[] packages, String[] sourceFiles,
							   String[] subPackages, String[] additionalArguments) {
		try {
			final DocumentationTool documentationTool = ToolProvider.getSystemDocumentationTool();
			final StandardJavaFileManager fileManager = documentationTool.getStandardFileManager(null, null, null);
			final ArrayList<JavaFileObject> compilationUnits = new ArrayList<>();

			// sourcePaths
			if (sourcePaths != null) {
				fileManager.setLocation(
						StandardLocation.SOURCE_PATH,
						Stream.of(sourcePaths).map(File::new).collect(Collectors.toList())
				);
			}

			// subPackages
			if (subPackages != null) {
				for (String pkg : subPackages) {
					final Iterable<JavaFileObject> javaFileObjects = fileManager.list(
							StandardLocation.SOURCE_PATH,
							pkg,
							Collections.singleton(JavaFileObject.Kind.SOURCE),
							true
					);
					javaFileObjects.forEach(compilationUnits::add);
				}
			}

			// sourceFiles
			if (sourceFiles != null) {
				fileManager.getJavaFileObjects(sourceFiles).forEach(compilationUnits::add);
			}

			// additionalArguments
			final List<String> options = Stream
					.concat(
							Stream.of("-private"),
							Stream.of(additionalArguments != null ? additionalArguments : new String[]{})
					)
					.collect(Collectors.toList());

			final DocumentationTool.DocumentationTask task = documentationTool.getTask(null, fileManager, null, XmlDoclet.class, options, compilationUnits);
			task.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// for debugging
//		System.out.println(marshalJAXB(NewXmlDoclet.root));
		return XmlDoclet.root;
	}

	private static String marshalJAXB(Object jaxbObject) {
		final StringWriter writer = new StringWriter();
		JAXB.marshal(jaxbObject, writer);
		return writer.toString();
	}

	/**
	 * Helper method to concat strings.
	 * 
	 * @param glue
	 *            the seperator.
	 * @param strings
	 *            the strings to concat.
	 * @return concated string
	 */
	public static String join(String glue, String[] strings) {
		return join(glue, Arrays.asList(strings));
	}

	/**
	 * Helper method to concat strings.
	 * 
	 * @param glue
	 *            the seperator.
	 * @param strings
	 *            the strings to concat.
	 * @return concated string
	 */
	public static String join(String glue, Iterable<String> strings) {
		if (strings == null) {
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();
		String verkett = "";
		for (String string : strings) {
			stringBuilder.append(verkett);
			stringBuilder.append(string);
			verkett = glue;
		}
		return stringBuilder.toString();
	}

}
