package io.github.nahkd123.axiomstylus.preset;

import java.util.List;

import org.joml.Matrix4f;

import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamic;

public interface BrushPreset {
	TipShape shape();

	BrushSpacing spacing();

	Palette palette();

	/**
	 * <p>
	 * Get a list of brush dynamics that applies on the shape of the brush tip.
	 * </p>
	 */
	List<BrushDynamic<Matrix4f>> shapeDynamics();
}
