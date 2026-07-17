package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  JARVIS COMPILER                                          ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Verifies Claude-generated code actually compiles        ║
// ║  BEFORE reporting success back to the user.               ║
// ║                                                            ║
// ║  This is the safety net — Claude can write syntactically ║
// ║  plausible but broken Java. Never trust generated code    ║
// ║  without compiling it first.                              ║
// ╚══════════════════════════════════════════════════════════╝

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JarvisCompiler {

    private static final Logger log =
            LogManager.getLogger(JarvisCompiler.class);

    private final MavenExecutor mavenExecutor;

    public JarvisCompiler() {
        this.mavenExecutor = new MavenExecutor();
    }

    // Returns true only if the whole project — including
    // the newly written file — compiles cleanly.
    // A single bad generated file fails the WHOLE compile,
    // which is exactly the signal we want: never leave a
    // broken file sitting in the codebase silently.
    public boolean verify() {
        try {
            boolean success = mavenExecutor.compile();
            if (success) {
                log.info("Generated code compiles cleanly");
            } else {
                log.error("Generated code FAILED to compile "
                        + "— file will need manual review");
            }
            return success;
        } catch (Exception e) {
            log.error("Compile verification error: {}",
                    e.getMessage());
            return false;
        }
    }
}