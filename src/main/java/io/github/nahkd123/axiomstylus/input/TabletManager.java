package io.github.nahkd123.axiomstylus.input;

import java.io.Closeable;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import io.github.nahkd123.axiomstylus.bridge.WindowBridge;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface TabletManager extends Closeable {
	/**
	 * <p>
	 * Get last input report. The return value may be {@code null} if user haven't
	 * used graphics tablet before method invocation.
	 * </p>
	 * 
	 * @return Last input report.
	 */
	@Nullable
	InputReport getLastReport();

	/**
	 * <p>
	 * Get a collection of initialized tablet drivers. In the future, user will have
	 * ability to configure the drivers, hence the need to get a collection of
	 * drivers, but for now, we just ask user to configure it in their actual tablet
	 * driver.
	 * </p>
	 */
	Collection<TabletDriver> getDrivers();

	@Override
	void close();

	Event<OnInput> ON_PEN_INPUT = EventFactory.createArrayBacked(OnInput.class, callbacks -> input -> {
		for (OnInput callback : callbacks) callback.onInput(input);
	});

	Event<Runnable> ON_PEN_AWAY = EventFactory.createArrayBacked(Runnable.class, callbacks -> () -> {
		for (Runnable callback : callbacks) callback.run();
	});

	static TabletManager createTabletManager(long glfwWindow) {
		return new TabletManagerImpl(glfwWindow);
	}

	static TabletManager get(Window window) {
		return ((WindowBridge) (Object) window).getTabletManager();
	}

	static TabletManager get(MinecraftClient client) {
		return get(client.getWindow());
	}

	@FunctionalInterface
	interface OnInput {
		void onInput(InputReport input);
	}
}
