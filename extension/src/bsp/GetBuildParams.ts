import { GradleConfig } from "./GradleConfig";
export interface GetBuildParams {
    projectDir: string;
    cancellationKey: string;
    gradleConfig: GradleConfig;
    showOutputColors: boolean;
}
