package io.github.nahkd123.axiomstylus.input;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;

import io.github.nahkd123.axiomstylus.input.windows.WindowsTabletDriver;
import net.minecraft.text.Text;

public interface TabletDriver extends Closeable {
	Text getName();

	/**
	 * <p>
	 * Check whether the current platform is supported. Some drivers are
	 * platform-specific, like Windows Tablet PC driver for example.
	 * </p>
	 * 
	 * @return Whether the current platform is supported by this driver.
	 */
	boolean isSupported();

	/**
	 * <p>
	 * Initialize this graphics tablet driver.
	 * </p>
	 * 
	 * @param context The driver context.
	 */
	void initialize(TabletDriverContext context);

	/**
	 * <p>
	 * Close this tablet driver, releasing resources that are being used to pull
	 * inputs from graphic tablets.
	 * </p>
	 */
	@Override
	void close();

	static Collection<TabletDriver> getAllSupportedDrivers() {
		// TODO Linux
		// TODO MacOS
		return List.<TabletDriver>of(new WindowsTabletDriver())
			.stream()
			.filter(TabletDriver::isSupported)
			.toList();
	}
}
