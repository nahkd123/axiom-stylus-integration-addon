package io.github.nahkd123.axiomstylus.input;

/**
 * <p>
 * Coordinates are in screen pixels, not UI pixels.
 * </p>
 */
public record InputReport(int status, int buttons, float x, float y, float pressure, float tiltX, float tiltY) {

	public static final int STATUS_TIP_TOUCHING = 0b00000001;
	public static final int STATUS_INVERT = 0b00000010;

	public boolean tipTouching() {
		return (status() & STATUS_TIP_TOUCHING) != 0;
	}

	public boolean invert() {
		return (status() & STATUS_INVERT) != 0;
	}

	public boolean button(int index) {
		return ((1 << index) & buttons()) != 0;
	}

	public static InputReport lerp(InputReport a, InputReport b, float progress) {
		int status = progress < 0.5f ? a.status : b.status;
		int buttons = progress < 0.5f ? a.buttons : b.buttons;
		float x = a.x * (1f - progress) + b.x * progress;
		float y = a.y * (1f - progress) + b.y * progress;
		float p = a.pressure * (1f - progress) + b.pressure * progress;
		float tx = a.tiltX * (1f - progress) + b.tiltX * progress;
		float ty = a.tiltY * (1f - progress) + b.tiltY * progress;
		return new InputReport(status, buttons, x, y, p, tx, ty);
	}
}
