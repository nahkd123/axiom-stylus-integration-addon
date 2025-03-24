package io.github.nahkd123.axiomstylus.preset;

import java.util.List;

import org.joml.Matrix4f;

import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamic;

public record SavedBrushPreset(TipShape shape, BrushSpacing spacing, Palette palette, List<BrushDynamic<Matrix4f>> shapeDynamics) implements BrushPreset {
	public static SavedBrushPreset savedPresetOf(BrushPreset preset) {
		if (preset instanceof SavedBrushPreset saved) return saved;
		TipShape shape = preset.shape();
		BrushSpacing spacing = preset.spacing();
		Palette palette = preset.palette();
		List<BrushDynamic<Matrix4f>> shapeDynamics = List.copyOf(preset.shapeDynamics());
		return new SavedBrushPreset(shape, spacing, palette, shapeDynamics);
	}
}
