package io.github.nahkd123.axiomstylus.input;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.nahkd123.axiomstylus.AxiomStylusIntegrationAddon;

class TabletManagerImpl implements TabletManager {
	private Collection<TabletDriver> drivers = TabletDriver.getAllSupportedDrivers();
	InputReport lastReport = null;
	long glfwWindowHandle;

	public TabletManagerImpl(long glfwWindowHandle) {
		// TODO allow swapping window handle
		this.glfwWindowHandle = glfwWindowHandle;

		for (TabletDriver driver : drivers) {
			AxiomStylusIntegrationAddon.LOGGER.info("Initializing tablet driver: {}",
				driver.getClass().getSimpleName());
			driver.initialize(new TabletDriverContextImpl(this));
		}
	}

	@Override
	public @Nullable
	InputReport getLastReport() { return lastReport; }

	@Override
	public Collection<TabletDriver> getDrivers() { return drivers; }

	@Override
	public void close() {
		for (TabletDriver driver : drivers) driver.close();
		drivers = List.of();
	}
}
