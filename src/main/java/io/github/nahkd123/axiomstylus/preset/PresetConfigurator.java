package io.github.nahkd123.axiomstylus.preset;

import java.util.function.Consumer;

@FunctionalInterface
public interface PresetConfigurator<T> {
	void renderImGui(Consumer<T> applyCallback);

	static <T> PresetConfigurator<T> empty() {
		return _ -> {};
	}
}
