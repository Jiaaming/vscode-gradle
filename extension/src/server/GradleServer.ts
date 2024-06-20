import * as vscode from "vscode";
import * as path from "path";
import * as cp from "child_process";
//import * as getPort from "get-port";
import * as kill from "tree-kill";
import { getGradleServerCommand, getGradleServerEnv } from "./serverUtil";
import { isDebuggingServer } from "../util";
import { Logger } from "../logger/index";
import { NO_JAVA_EXECUTABLE } from "../constant";
//import { generateRandomPipeName } from 'vscode-languageclient/node';
import { getRedHatJavaExecutablePath } from "../util/config";
const SERVER_LOGLEVEL_REGEX = /^\[([A-Z]+)\](.*)$/;
const DOWNLOAD_PROGRESS_CHAR = ".";
//export const GET_BUILD_SERVER_PORT = "gradle.getBuildServerPort";

export interface ServerOptions {
    host: string;
}

export class GradleServer {
    private readonly _onDidStart: vscode.EventEmitter<null> = new vscode.EventEmitter<null>();
    private readonly _onDidStop: vscode.EventEmitter<null> = new vscode.EventEmitter<null>();
    private ready = false;
    private gradleServerPort: number | undefined;
    private buildServerPipeName: string | undefined;
    private restarting = false;

    public readonly onDidStart: vscode.Event<null> = this._onDidStart.event;
    public readonly onDidStop: vscode.Event<null> = this._onDidStop.event;
    private process?: cp.ChildProcessWithoutNullStreams;

    constructor(
        private readonly opts: ServerOptions,
        private readonly context: vscode.ExtensionContext,
        private readonly logger: Logger,
    ) {}
    // private async registerGetBuildServerPipeNameCommand(): Promise<void> {
    //     this.context.subscriptions.push(
    //         vscode.commands.registerCommand(GET_BUILD_SERVER_PORT, async (testNum: number) => {
    //             const res = [testNum];
    //             return await Promise.resolve(res);
    //         })
    //     );
    // }
    public async start(): Promise<void> {
        if (isDebuggingServer()) {
            this.fireOnStart();
        } else {
            //this.buildServerPipeName = generateRandomPipeName();
            this.buildServerPipeName = "/tmp/example.sock";
            this.gradleServerPort = 8827;
            this.logger.info("gradleServerPort: ", this.gradleServerPort.toString());

            //this.registerGetBuildServerPipeNameCommand();
            const cwd = this.context.asAbsolutePath("lib");
            const cmd = path.join(cwd, getGradleServerCommand());
            const env = await getGradleServerEnv();
            const bundleDirectory = await this.getBundleDirectory();
            if (!env) {
                await vscode.window.showErrorMessage(NO_JAVA_EXECUTABLE);
                //return;
            }
            let javaExecPath = await getRedHatJavaExecutablePath();
            if (!javaExecPath) {
                javaExecPath = "/Users/liujiaming/.vscode/extensions/redhat.java-1.30.0-darwin-x64/jre/17.0.10-macosx-x86_64/bin/java"
            }
            const args = [String(this.gradleServerPort), this.buildServerPipeName, bundleDirectory, javaExecPath];
            for (const arg of args) {
                this.logger.info(`arg: ${arg}`);
            }
            this.logger.debug(`Gradle Server cmd: ${cmd} ${args.join(" ")}`);
            this.process = cp.spawn(`"${cmd}"`, args, {
                cwd,
                env,
                shell: true,
            });
            this.process.stdout.on("data", this.logOutput);
            this.process.stderr.on("data", this.logOutput);
            this.process
                .on("error", (err: Error) => this.logger.error(err.message))
                .on("exit", async (code) => {
                    this.logger.warn("Gradle server stopped");
                    this._onDidStop.fire(null);
                    this.ready = false;
                    this.process?.removeAllListeners();
                    if (this.restarting) {
                        this.restarting = false;
                        await this.start();
                    } else if (code !== 0) {
                        await this.handleServerStartError();
                    }
                });

            this.fireOnStart();
        }
    }

    public isReady(): boolean {
        return this.ready;
    }

    public async getBundleDirectory(): Promise<string> {
        const extensionPath = await vscode.commands.executeCommand<string>("gradle.getExtensionPath");
        return path.join(extensionPath, 'server');
    }

    public async showRestartMessage(): Promise<void> {
        const OPT_RESTART = "Restart Server";
        const input = await vscode.window.showErrorMessage(
            "No connection to gradle server. Try restarting the server.",
            OPT_RESTART
        );
        if (input === OPT_RESTART) {
            await this.start();
        }
    }

    public async restart(): Promise<void> {
        this.logger.info("Restarting gradle server");
        this.restarting = true;
        this.killProcess();
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private logOutput = (data: any): void => {
        const str = data.toString().trim();
        if (!str || str === DOWNLOAD_PROGRESS_CHAR) {
            return;
        }
        const logLevelMatches = str.match(SERVER_LOGLEVEL_REGEX);
        if (logLevelMatches && logLevelMatches.length) {
            const [, serverLogLevel, serverLogMessage] = logLevelMatches;
            const logLevel = serverLogLevel.toLowerCase() as "debug" | "info" | "warn" | "error";
            this.logger[logLevel](serverLogMessage.trim());
        } else {
            this.logger.info(str);
        }
    };

    private async killProcess(): Promise<void> {
        if (this.process) {
            return new Promise((resolve, _reject) => {
                if (this.process?.pid) {
                    kill(this.process.pid, () => resolve);
                }
            });
        }
    }

    private async handleServerStartError(): Promise<void> {
        await this.showRestartMessage();
    }

    private fireOnStart(): void {
        this.ready = true;
        this._onDidStart.fire(null);
    }

    public async asyncDispose(): Promise<void> {
        this.process?.removeAllListeners();
        await this.killProcess();
        this.ready = false;
        this._onDidStart.dispose();
        this._onDidStop.dispose();
    }

    public getPort(): number | undefined {
        return this.gradleServerPort;
    }

    public getOpts(): ServerOptions {
        return this.opts;
    }
}
