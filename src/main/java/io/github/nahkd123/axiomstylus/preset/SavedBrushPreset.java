package io.github.nahkd123.axiomstylus.preset;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamic;
import io.github.nahkd123.axiomstylus.preset.dynamic.Matrix4fDynamicTarget;

public record SavedBrushPreset(TipShape shape, BrushSpacing spacing, Palette palette, List<BrushDynamic<Matrix4f>> shapeDynamics) implements BrushPreset {

	public static final MapCodec<SavedBrushPreset> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		TipShape.CODEC.fieldOf("shape").forGetter(SavedBrushPreset::shape),
		BrushSpacing.CODEC.fieldOf("spacing").forGetter(SavedBrushPreset::spacing),
		Palette.CODEC.fieldOf("palette").forGetter(SavedBrushPreset::palette),
		BrushDynamic.createCodec(Matrix4fDynamicTarget.CODEC.xmap(t -> t, t -> (Matrix4fDynamicTarget) t))
			.listOf()
			.fieldOf("shapeDynamics").forGetter(SavedBrushPreset::shapeDynamics))
		.apply(i, SavedBrushPreset::new));

	public static SavedBrushPreset savedPresetOf(BrushPreset preset) {
		if (preset instanceof SavedBrushPreset saved) return saved;
		TipShape shape = preset.shape();
		BrushSpacing spacing = preset.spacing();
		Palette palette = preset.palette();
		List<BrushDynamic<Matrix4f>> shapeDynamics = List.copyOf(preset.shapeDynamics());
		return new SavedBrushPreset(shape, spacing, palette, shapeDynamics);
	}
}
