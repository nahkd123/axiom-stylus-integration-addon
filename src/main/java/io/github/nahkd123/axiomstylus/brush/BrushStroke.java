package io.github.nahkd123.axiomstylus.brush;

public interface BrushStroke {
	/**
	 * <p>
	 * The total travel distance, counting in blocks.
	 * </p>
	 */
	float totalDistance();

	float jitter();
}
