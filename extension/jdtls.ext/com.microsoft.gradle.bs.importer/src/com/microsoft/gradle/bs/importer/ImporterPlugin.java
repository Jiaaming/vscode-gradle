package com.microsoft.gradle.bs.importer;

import java.io.File;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
//import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.managers.DigestStore;


import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.osgi.framework.BundleContext;

import com.microsoft.java.builder.BuildStateManager;

import ch.epfl.scala.bsp4j.BuildClient;

public class ImporterPlugin extends Plugin {

    public static final String PLUGIN_ID = "com.microsoft.gradle.buildServer.importer";

    private Map<IPath, Pair<BuildServerConnection, BuildClient>> buildServers = new ConcurrentHashMap<>();

    private static ImporterPlugin instance;

    /**
     * Digest store for the gradle configuration files.
     */
    private DigestStore digestStore;

    private static String bundleVersion = "";

    @Override
    public void start(BundleContext context) throws Exception {
        BuildStateManager.getBuildStateManager().startup();
        ImporterPlugin.instance = this;
        bundleVersion = context.getBundle().getVersion().toString();
        digestStore = new DigestStore(getStateLocation().toFile());
        Optional<File> bundleFile = FileLocator.getBundleFileLocation(context.getBundle());
        if (!bundleFile.isPresent()) {
           throw new IllegalStateException("Failed to get bundle location.");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (Pair<BuildServerConnection, BuildClient> pair : buildServers.values()) {
            pair.getLeft().buildShutdown();
            pair.getLeft().onBuildExit();
        }
    }

    public static ImporterPlugin getInstance() {
        return ImporterPlugin.instance;
    }

    public static String getBundleVersion() {
        return bundleVersion;
    }

    public static DigestStore getDigestStore() {
        return instance.digestStore;
    }

	// public static Object getBuildServerPort() {
	// 	return JavaLanguageServerPlugin.getInstance().getClientConnection()
	// 		.executeClientCommand("gradle.getBuildServerPort");

	// }

    public static BuildServerConnection getBuildServerConnection(IPath rootPath) throws CoreException {
        Pair<BuildServerConnection, BuildClient> pair = instance.buildServers.get(rootPath);
        if (pair != null) {
            return pair.getLeft();
        }
		try {
			ClientNamedPipeStream pipeStream = new ClientNamedPipeStream();

			BuildClient client = new GradleBuildClient();
			Launcher<BuildServerConnection> launcher = new Launcher.Builder<BuildServerConnection>()
					.setOutput(pipeStream.getOutputStream())
					.setInput(pipeStream.getInputStream())
					.setLocalService(client)
					.setExecutorService(Executors.newCachedThreadPool())
					.setRemoteInterface(BuildServerConnection.class)
					.create();

			launcher.startListening();
			BuildServerConnection server = launcher.getRemoteProxy();
			client.onConnectWithServer(server);

			instance.buildServers.put(rootPath, Pair.of(server, client));
			return server;
    	} catch (Exception e) {
			e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Failed to start build server.", e));
		}
	}

}
