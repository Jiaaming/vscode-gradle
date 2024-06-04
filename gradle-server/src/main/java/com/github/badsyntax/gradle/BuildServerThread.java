package com.github.badsyntax.gradle;

import ch.epfl.scala.bsp4j.BuildServer;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.jsonrpc.Launcher;

public class BuildServerThread implements Runnable {

	private static final String bundleDirectory = "/Users/liujiaming/.vscode/extensions/redhat.java-1.31.0-darwin-x64/server/../../../../Documents/24Summer/vscode-gradle/extension/server";

	private final int port;
	public BuildServerThread(int port) {
		this.port = port;
	}
	@Override
	public void run() {
		try {
			String javaExecutablePath = getJavaExecutablePath();
			String[] classpaths = getBuildServerClasspath();

			String pluginPath = getBuildServerPluginPath();

			List<String> command = new ArrayList<>();
			command.add(javaExecutablePath);
			if (Boolean.parseBoolean(System.getenv("DEBUG_GRADLE_BUILD_SERVER"))) {
				command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8989");
			}
			command.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
			command.add("--add-opens=java.base/java.io=ALL-UNNAMED");
			command.add("--add-opens=java.base/java.util=ALL-UNNAMED");
			command.add("-Dplugin.dir=" + pluginPath);
			command.add("-cp");
			command.add(String.join(getClasspathSeparator(), classpaths));
			command.add("com.microsoft.java.bs.core.Launcher");

			ProcessBuilder build = new ProcessBuilder(command);
			build.start();

			try (Socket socket = new Socket("localhost", this.port)) {
				FakeGradleBuildClient client = new FakeGradleBuildClient();

				Launcher<BuildServer> launcher = Launcher.createLauncher(client, BuildServer.class,
						socket.getInputStream(), socket.getOutputStream());
				launcher.startListening();

				// Do nothing.
				BuildServer server = launcher.getRemoteProxy();
				client.onConnectWithServer(server);
			}

			// BuildClient client = new FakeGradleBuildClient();
			// Launcher<BuildServerConnection> launcher = new
			// Launcher.Builder<BuildServerConnection>()
			// .setOutput(process.getOutputStream()) //change to socket
			// .setInput(process.getInputStream())
			// .setLocalService(client)
			// .setExecutorService(Executors.newCachedThreadPool())
			// .setRemoteInterface(BuildServerConnection.class)
			// .create();

			// launcher.startListening();
			// BuildServerConnection server = launcher.getRemoteProxy();
			// client.onConnectWithServer(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getJavaExecutablePath() {
		// Optional<String> command = ProcessHandle.current().info().command();
		return "/Users/liujiaming/.vscode/extensions/redhat.java-1.30.0-darwin-x64/jre/17.0.10-macosx-x86_64/bin/java";
	}

	private String[] getBuildServerClasspath() {
		return new String[]{Paths.get(bundleDirectory, "server.jar").toString(),
				Paths.get(bundleDirectory, "runtime").toString() + File.separatorChar + "*"};
	}

	private String getBuildServerPluginPath() {
		return Paths.get(bundleDirectory, "plugins").toString();
	}

	private String getClasspathSeparator() {
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("win")) {
			return ";";
		}

		return ":"; // Linux or Mac
	}
}
