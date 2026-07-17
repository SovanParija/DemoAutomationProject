package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  JARVIS FILE WRITER                                      ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Saves Claude-generated Java code to correct location   ║
// ╚══════════════════════════════════════════════════════════╝

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JarvisFileWriter {

    private static final Logger log =
            LogManager.getLogger(JarvisFileWriter.class);

    // Save generated test class
    // className example: "MDA_1239_LogoutTest"
    public String saveTestClass(String className,
                                String javaCode) throws IOException {

        String path = JarvisConfig.PROJECT_ROOT + "/"
                + JarvisConfig.TEST_PACKAGE_PATH + "/"
                + className + ".java";

        return writeFile(path, javaCode);
    }

    // Save generated page object
    // className example: "CartPage"
    public String savePageObject(String className,
                                 String javaCode) throws IOException {

        String path = JarvisConfig.PROJECT_ROOT + "/"
                + JarvisConfig.PAGE_PACKAGE_PATH + "/"
                + className + ".java";

        return writeFile(path, javaCode);
    }

    // Generic file write with directory creation
    private String writeFile(String path, String content)
            throws IOException {

        File file = new File(path);
        file.getParentFile().mkdirs();

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }

        log.info("File saved: {}", path);
        return path;
    }
}