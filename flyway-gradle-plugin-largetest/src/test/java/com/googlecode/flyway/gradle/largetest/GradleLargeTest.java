/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.gradle.largetest;

import org.junit.Test;
import com.googlecode.flyway.core.util.FileCopyUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Large Test for the Flyway Gradle Plugin.
 */
@SuppressWarnings({"JavaDoc"})
public class GradleLargeTest {
    private String installDir = System.getProperty("installDir", "flyway-gradle-plugin-largetest/target");

    @Test
    public void regular() throws Exception {
        String stdOut = runGradle(0, "regular", "clean", "flywayMigrate", "-Pflyway.placeholders.name=James");
        assertTrue(stdOut.contains("Successfully applied 2 migrations"));
        assertFalse(stdOut.contains("deprecated"));
    }

    /**
     * Runs Gradle in this directory with these extra arguments.
     *
     * @param expectedReturnCode The expected return code for this invocation.
     * @param dir                The directory below src/test/resources to run Gradle in.
     * @param extraArgs          The extra arguments (if any) for Gradle.
     * @return The standard output.
     * @throws Exception When the execution failed.
     */
    private String runGradle(int expectedReturnCode, String dir, String... extraArgs) throws Exception {
        String flywayVersion = System.getProperty("flywayVersion", getPomVersion());
        String root = new File(installDir + "/test-classes/" + dir).getAbsolutePath();

        String extension = "";
        if (System.getProperty("os.name").startsWith("Windows")) {
            extension = ".bat";
        }

        List<String> args = new ArrayList<String>();
        //addShellIfNeeded(args);
        args.add(root + "/gradlew" + extension);
        args.add("-PflywayVersion=" + flywayVersion);
        args.add("--info");
        args.add("--stacktrace");
        args.addAll(Arrays.asList(extraArgs));

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(new File(root));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileCopyUtils.copyToString(new InputStreamReader(process.getInputStream(), "UTF-8"));
        int returnCode = process.waitFor();

        System.out.print(stdOut);

        assertEquals("Unexpected return code", expectedReturnCode, returnCode);

        return stdOut;
    }

    private void addShellIfNeeded(List<String> args) {
        if (!isWindowsOs()) {
            args.add("sh");
        }
    }

    /**
     * Check if we are running on Windows OS
     *
     * @return true for windows, otherwise false
     */
    private boolean isWindowsOs() {
        return System.getProperty("os.name", "generic").toLowerCase().startsWith("windows");
    }

    /**
     * Retrieves the version embedded in the project pom. Useful for running these tests in IntelliJ.
     *
     * @return The POM version.
     */
    private String getPomVersion() {
        try {
            File pom = new File("pom.xml");
            if (!pom.exists()) {
                return "unknown";
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(pom);
            return xPath.evaluate("/project/version", document);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read POM version", e);
        }
    }
}
