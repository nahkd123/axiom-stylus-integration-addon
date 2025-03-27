package io.github.nahkd123.axiomstylus.brush;

import net.minecraft.util.math.Vec3d;

public record ImmutableBrushPoint(long timestampNano, Vec3d position, float normalPressure, float jitter, float tiltPlaneX, float tiltPlaneY) implements BrushPoint {
	public static ImmutableBrushPoint lerp(BrushPoint a, BrushPoint b, float p) {
		long timestampNano = (long) (a.timestampNano() * (1d - p) + b.timestampNano() * (double) p);
		Vec3d position = a.position().lerp(b.position(), p);
		float normalPressure = a.normalPressure() * (1f - p) + b.normalPressure() * p;
		float jitter = (float) Math.random() * 2 - 1; // TODO use random source
		float tiltPlaneX = a.tiltPlaneX() * (1f - p) + b.tiltPlaneX();
		float tiltPlaneY = a.tiltPlaneY() * (1f - p) + b.tiltPlaneY();
		return new ImmutableBrushPoint(timestampNano, position, normalPressure, jitter, tiltPlaneX, tiltPlaneY);
	}
}
