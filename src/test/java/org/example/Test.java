package org.example;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        executeWithProcessBuilder();
    }

    private static void executeWithProcessBuilder() throws IOException, InterruptedException {
        System.out.println("\n使用 ProcessBuilder 执行...");
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "python");

        // 可选：设置工作目录
        // pb.directory(new File("C:\\"));

        // 启动进程
        Process process = pb.start();

        // 等待命令执行
        int exitCode = process.waitFor();
        System.out.println("ProcessBuilder 退出码: " + exitCode);
    }
}
