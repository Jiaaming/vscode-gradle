export class GradleConfig {
    private gradleHome: string;
    private userHome: string;
    private wrapperEnabled: boolean;
    private version: string;
    private jvmArguments: string;
    private javaExtensionVersion: string;

    constructor() {
        this.wrapperEnabled = false; // default value
        this.jvmArguments = ""; // default value
        this.javaExtensionVersion = ""; // default value
        this.gradleHome = "";
        this.userHome = "";

    }

    public getGradleHome(): string {
        return this.gradleHome;
    }

    public setGradleHome(gradleHome: string): void {
        this.gradleHome = gradleHome;
    }

    public getUserHome(): string {
        return this.userHome;
    }

    public setUserHome(userHome: string): void {
        this.userHome = userHome;
    }

    public getWrapperEnabled(): boolean {
        return this.wrapperEnabled;
    }

    public setWrapperEnabled(wrapperEnabled: boolean): void {
        this.wrapperEnabled = wrapperEnabled;
    }

    public getVersion(): string {
        return this.version;
    }

    public setVersion(version: string): void {
        this.version = version;
    }

    public getJvmArguments(): string {
        return this.jvmArguments;
    }

    public setJvmArguments(jvmArguments: string): void {
        this.jvmArguments = jvmArguments;
    }

    public getJavaExtensionVersion(): string {
        return this.javaExtensionVersion;
    }

    public setJavaExtensionVersion(javaExtensionVersion: string): void {
        this.javaExtensionVersion = javaExtensionVersion;
    }
}
