package io.github.nahkd123.axiomstylus.preset.dynamic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;

/**
 * <p>
 * Map value from sensor to value that can be controlled by user.
 * </p>
 */
public interface DynamicFunction extends FloatUnaryOperator {
	default void renderImGui() {}

	static Codec<DynamicFunction> CODEC = Codec.STRING.dispatch(
		"type",
		func -> switch (func) {
		case Parametric _ -> "exponent";
		default -> throw new IllegalArgumentException("Unexpected value: " + func);
		},
		name -> switch (name) {
		case "exponent" -> Parametric.CODEC;
		default -> throw new IllegalArgumentException("Unexpected value: " + name);
		});

	class Parametric implements DynamicFunction {
		public static final MapCodec<Parametric> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.floatRange(-1f, 1f).optionalFieldOf("vShift", 0f).forGetter(Parametric::getVerticalShift),
			Codec.floatRange(-1f, 1f).optionalFieldOf("hShift", 0f).forGetter(Parametric::getHorizontalShift),
			Codec.floatRange(0f, 10f).optionalFieldOf("gain", 1f).forGetter(Parametric::getGain),
			Codec.floatRange(0f, 10f).optionalFieldOf("exponent", 1f).forGetter(Parametric::getExponent),
			Codec.BOOL.optionalFieldOf("flip", false).forGetter(Parametric::isFlip))
			.apply(instance, Parametric::new));

		private float[] vShift = { 0f, -1f };
		private float[] hShift = { 0f, -1f };
		private float[] gain = { 1f, -1f };
		private float[] exponent = { 1f, -1f };
		private boolean flip = false;
		private float[] imguiPlot = new float[128];

		public Parametric(float vShift, float hShift, float gain, float exponent, boolean flip) {
			this.vShift[0] = vShift;
			this.hShift[0] = hShift;
			this.gain[0] = gain;
			this.vShift[0] = vShift;
			this.flip = flip;
		}

		public Parametric() {
			this(0f, 0f, 1f, 1f, false);
		}

		@Override
		public float apply(float operand) {
			float in = Math.clamp(operand - hShift[0], 0f, 1f);
			float v = (float) Math.clamp(
				vShift[0] + Math.pow(in, exponent[0]) * gain[0],
				0f, 1f);
			if (Float.isNaN(v)) v = 0f;
			return flip ? (1f - v) : v;
		}

		public float getVerticalShift() { return vShift[0]; }

		public float getHorizontalShift() { return hShift[0]; }

		public float getGain() { return gain[0]; }

		public float getExponent() { return exponent[0]; }

		public boolean isFlip() { return flip; }

		public void setVerticalShift(float vShift) { this.vShift[0] = vShift; }

		public void setHorizontalShift(float hShift) { this.hShift[0] = hShift; }

		public void setGain(float gain) { this.gain[0] = gain; }

		public void setExponent(float exponent) { this.exponent[0] = exponent; }

		public void setFlip(boolean flip) { this.flip = flip; }

		@Override
		public void renderImGui() {
			boolean recalcGraph = false
				|| vShift[0] != vShift[1]
				|| hShift[0] != hShift[1]
				|| gain[0] != gain[1]
				|| exponent[0] != exponent[1];

			if (ImGui.checkbox("Flip graph", flip)) {
				flip = !flip;
				recalcGraph = true;
			}
			ImGui.sliderFloat("Shift X", hShift, -1f, 1f, "%.02f");
			ImGui.sliderFloat("Shift Y", vShift, -1f, 1f, "%.02f");
			ImGui.sliderFloat("Gain", gain, 0.01f, 10f, "%.02f", ImGuiSliderFlags.Logarithmic);
			ImGui.sliderFloat("Exponent", exponent, 0.01f, 10f, "%.02f", ImGuiSliderFlags.Logarithmic);

			if (recalcGraph) {
				for (int i = 0; i < imguiPlot.length; i++) {
					float p = i / (float) (imguiPlot.length - 1);
					imguiPlot[i] = apply(p);
				}

				vShift[1] = vShift[0];
				hShift[1] = hShift[0];
				gain[1] = gain[0];
				exponent[1] = exponent[0];
			}

			ImGui.plotLines(
				"Graph", imguiPlot, imguiPlot.length, 0, "", 0f, 1f,
				ImGui.calcItemWidth(), ImGui.calcItemWidth());
		}
	}
}
