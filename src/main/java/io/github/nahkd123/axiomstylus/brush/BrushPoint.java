package io.github.nahkd123.axiomstylus.brush;

import io.github.nahkd123.axiomstylus.input.InputReport;
import net.minecraft.util.math.Vec3d;

/**
 * <p>
 * Represent a data point of a brush. The data point is calculated from
 * {@link InputReport} and some values in environment. The specific
 * implementation doesn't really matter to API consumer anyways.
 * </p>
 */
public interface BrushPoint {
	/**
	 * <p>
	 * Timestamp of this brush point in nanoseconds, relative to stroke's start
	 * timestamp. This will be used to calculate brush speed.
	 * </p>
	 */
	long timestampNano();

	/**
	 * <p>
	 * The absolute position of this point in world.
	 * </p>
	 */
	Vec3d position();

	/**
	 * <p>
	 * The normal pressure of input device. In the case of graphics tablet, this is
	 * the pressure being applied to pressure sensor along pen's axis.
	 * </p>
	 */
	float normalPressure();

	/**
	 * <p>
	 * A random number applied on each individual brush point.
	 * </p>
	 */
	float jitter();

	float tiltPlaneX();

	float tiltPlaneY();
}
