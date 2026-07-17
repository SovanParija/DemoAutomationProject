package com.framework.ai;

public class ClaudeTest {
    public static void main(String[] args) {
        ClaudeAIService claude = new ClaudeAIService();
        String response = claude.ask(
                "Say 'Jarvis is online' and nothing else");
        System.out.println("Claude says: " + response);
    }
}