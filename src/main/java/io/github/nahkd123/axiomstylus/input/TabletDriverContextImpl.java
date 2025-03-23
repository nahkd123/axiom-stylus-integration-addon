package io.github.nahkd123.axiomstylus.input;

import org.jetbrains.annotations.Nullable;

class TabletDriverContextImpl implements TabletDriverContext {
	private TabletManagerImpl manager;

	public TabletDriverContextImpl(TabletManagerImpl manager) {
		this.manager = manager;
	}

	@Override
	public long getGlfwHandle() { return manager.glfwWindowHandle; }

	@Override
	public void reportInput(@Nullable InputReport report) {
		manager.lastReport = report;
		if (report != null) TabletManager.ON_PEN_INPUT.invoker().onInput(report);
		else TabletManager.ON_PEN_AWAY.invoker().run();
	}
}
