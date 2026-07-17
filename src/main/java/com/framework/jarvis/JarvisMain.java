package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  JARVIS MAIN                                              ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Entry point — text chat loop                            ║
// ║  Type commands, Jarvis executes and responds             ║
// ╚══════════════════════════════════════════════════════════╝

import java.util.Scanner;

public class JarvisMain {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════╗");
        System.out.println("║   JARVIS — TEST AUTOMATION   ║");
        System.out.println("║   ASSISTANT — ONLINE         ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.println();
        System.out.println("Try commands like:");
        System.out.println("  - run smoke tests on android");
        System.out.println("  - what failed last run");
        System.out.println("  - open the allure report");
        System.out.println("  - how many tests passed");
        System.out.println("  - exit");
        System.out.println();

        JarvisExecutor executor = new JarvisExecutor();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")
                    || input.equalsIgnoreCase("quit")) {
                System.out.println(
                        "Jarvis: Goodbye. Shutting down.");
                break;
            }

            if (input.isEmpty()) {
                continue;
            }

            System.out.println("Jarvis: Working on it...");
            String result = executor.execute(input);
            System.out.println("Jarvis: " + result);
            System.out.println();
        }

        scanner.close();
    }
}