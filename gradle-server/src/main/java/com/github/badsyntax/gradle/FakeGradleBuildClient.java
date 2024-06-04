package com.github.badsyntax.gradle;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.DidChangeBuildTarget;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.ShowMessageParams;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskProgressParams;
import ch.epfl.scala.bsp4j.TaskStartParams;

public class FakeGradleBuildClient implements BuildClient {

	public FakeGradleBuildClient() {
	}

	@Override
	public void onBuildLogMessage(LogMessageParams params) {
		throw new UnsupportedOperationException("Unimplemented method 'onBuildLogMessage'");

	}

	@Override
	public void onBuildPublishDiagnostics(PublishDiagnosticsParams arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onBuildPublishDiagnostics'");
	}

	@Override
	public void onBuildShowMessage(ShowMessageParams arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onBuildShowMessage'");
	}

	@Override
	public void onBuildTargetDidChange(DidChangeBuildTarget params) {
		throw new UnsupportedOperationException("Unimplemented method 'onBuildTargetDidChange'");
	}

	@Override
	public void onBuildTaskStart(TaskStartParams params) {
		throw new UnsupportedOperationException("Unimplemented method 'onBuildTaskStart'");
	}

	@Override
	public void onBuildTaskProgress(TaskProgressParams params) {
		throw new UnsupportedOperationException("Unimplemented method 'onBuildTaskProgress'");
	}

	@Override
	public void onBuildTaskFinish(TaskFinishParams params) {
		throw new UnsupportedOperationException("Unimplemented method 'onBuildTaskFinish'");
	}
}
