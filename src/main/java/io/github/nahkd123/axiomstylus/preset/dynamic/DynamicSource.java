package io.github.nahkd123.axiomstylus.preset.dynamic;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import imgui.ImGui;
import io.github.nahkd123.axiomstylus.brush.BrushPoint;
import io.github.nahkd123.axiomstylus.brush.BrushStroke;
import io.github.nahkd123.axiomstylus.preset.PresetConfigurator;

/**
 * <p>
 * Source value for brush dynamic. Limited to brush sensors for now.
 * </p>
 */
public enum DynamicSource {
	CONSTANT("Constant", "Always 1.00", 0f, 1f) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			return 1f;
		}
	},
	DISTANCE("Distance", "Total travelled distance of brush tip (blocks)", 0f, null) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			return stroke.totalDistance();
		}
	},
	SPEED("Speed", "Movement speed of brush tip (blocks per second)", 0f, null) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			if (previous == null) return 0f;
			long deltaNs = next.timestampNano() - previous.timestampNano();
			double movement = next.position().distanceTo(previous.position());
			return (float) (movement * 1e9d / deltaNs);
		}
	},
	NORMAL_PRESSURE("Pressure", "Normal pressure of the brush", 0f, 1f) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			return next.normalPressure();
		}
	},
	TILT_PLANE_X("Tilt Plane X", "Brush tilt around Y axis of input device's plane (in degrees)", -90f, 90f) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			return next.tiltPlaneX();
		}
	},
	TILT_PLANE_Y("Tilt Plane Y", "Brush tilt around X axis of input device's plane (in degrees)", -90f, 90f) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			return next.tiltPlaneY();
		}
	},
	JITTER_STORKE("Jitter (Stroke)", "Random value on each stroke (from -1.00 to 1.00)", -1f, 1f) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			return stroke.jitter();
		}
	},
	JITTER_DAB("Jitter (Dab)", "Random value on each dab (from -1.00 to 1.00)", -1f, 1f) {
		@Override
		public float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next) {
			return next.jitter();
		}
	};

	public static final Codec<DynamicSource> CODEC = Codec.stringResolver(
		DynamicSource::toString,
		DynamicSource::valueOf);

	private String name;
	private String description;
	private Float min;
	private Float max;

	private DynamicSource(String name, String description, Float min, Float max) {
		this.name = name;
		this.description = description;
		this.min = min;
		this.max = max;
	}

	public String getName() { return name; }

	public String getDescription() { return description; }

	public Float getMin() { return min; }

	public Float getMax() { return max; }

	public abstract float calculate(BrushStroke stroke, @Nullable BrushPoint previous, BrushPoint next);

	public PresetConfigurator<DynamicSource> createConfigurator(String label) {
		class ConfiguratorImpl implements PresetConfigurator<DynamicSource> {
			DynamicSource[] sources = values();
			int index = ordinal();

			@Override
			public void renderImGui(Consumer<DynamicSource> applyCallback) {
				if (ImGui.beginCombo(label, sources[index].getName())) {
					for (int i = 0; i < sources.length; i++) {
						DynamicSource entry = sources[i];

						if (ImGui.selectable(entry.name, i == index)) {
							index = i;
							applyCallback.accept(entry);
						}

						if (ImGui.isItemHovered()) ImGui.setTooltip(entry.description);
					}

					ImGui.endCombo();
				}
			}
		}

		return new ConfiguratorImpl();
	}
}