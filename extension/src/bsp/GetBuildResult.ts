export interface GetBuildResult {
    kind: BuildKind;
    progress?: {
        message: string;
    };
    output?: {
        outputType: "STDOUT" | "STDERR";
        outputBytes: string;
    };
    cancelled?: {
        message: string;
        projectDir: string;
    };
    getBuildResult?: {
        build: {
            project: {
                isRoot: boolean;
                tasks: Task[];
                projects: Project[];
                projectPath: string;
                dependencyItem: DependencyItem;
                plugins: Plugin[];
                pluginClosures: PluginClosure[];
                scriptClasspaths: string[];
            };
        };
    };
    environment?: {
        gradleEnvironment: {
            gradleUserHome: string;
            gradleVersion: string;
        };
        javaEnvironment: {
            javaHome: string;
            jvmArgs: string[];
        };
    };
    compatibilityCheckError?: string;
}

export type BuildKind =
    | "progress"
    | "output"
    | "cancelled"
    | "getBuildResult"
    | "environment"
    | "compatibilityCheckError";

export interface Task {
    // Define the structure of Task
}

export interface Project {
    // Define the structure of Project
}

export interface DependencyItem {
    // Define the structure of DependencyItem
}

export interface Plugin {
    // Define the structure of Plugin
}

export interface PluginClosure {
    // Define the structure of PluginClosure
}
