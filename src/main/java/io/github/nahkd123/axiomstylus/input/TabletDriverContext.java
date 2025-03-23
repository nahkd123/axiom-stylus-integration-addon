package io.github.nahkd123.axiomstylus.input;

import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * Represent graphics tablet driver context. Use this context to signal device
 * state changes (connect or disconnect) and emit inputs to applications (in
 * this case, it is a Minecraft mod).
 * </p>
 */
public interface TabletDriverContext {
	/**
	 * <p>
	 * Get GLFW window handle. This is not the same as Windows's HWND!
	 * </p>
	 */
	long getGlfwHandle();

	/**
	 * <p>
	 * Report user input to tablet manager.
	 * </p>
	 * 
	 * @param report The report. Use {@code null} to indicate the stylus is no
	 *               longer in detection range.
	 */
	void reportInput(@Nullable InputReport report);
}
