package com.github.badsyntax.gradle;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GradleServer {
	private static final Logger logger = LoggerFactory.getLogger(GradleServer.class.getName());

	private final int port;
	private final Server server;

	public GradleServer(int port) {
		this(ServerBuilder.forPort(port), port);
	}

	public GradleServer(ServerBuilder<?> serverBuilder, int port) {
		this.port = port;
		server = serverBuilder.addService(new GradleService()).build();
	}

	@SuppressWarnings("java:S106")
	public void start() throws IOException {
		server.start();
		logger.info("Gradle Server started, listening on {}", port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutting down gRPC server since JVM is shutting down");
				try {
					GradleServer.this.stop();
				} catch (InterruptedException e) {
					e.printStackTrace(System.err);
					Thread.currentThread().interrupt();
				}
				logger.info("Server shut down");
			}
		});
	}

	public void stop() throws InterruptedException {
		if (server != null) {
			server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static void main(String[] args) throws Exception {
		int gradleServerPort = 8887;
		int buildServerPort = 8817;
		if (args.length > 0) {
			gradleServerPort = Integer.parseInt(args[0]);
			buildServerPort = Integer.parseInt(args[1]);
		}
		GradleServer server = new GradleServer(gradleServerPort);
		Thread gradleServerThread = new Thread(() -> {
			try {
				server.start();
				server.blockUntilShutdown();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});

		BuildServerThread buildServerConnectionThread = new BuildServerThread(buildServerPort);
		Thread buildServerThread = new Thread(buildServerConnectionThread);

		gradleServerThread.start();
		buildServerThread.start();

		gradleServerThread.join();
		buildServerThread.join();
	}
}
