package io.github.nahkd123.axiomstylus.preset.dynamic;

import com.mojang.serialization.Codec;

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
		case ExponentDynamicFunction _ -> "exponent";
		default -> throw new IllegalArgumentException("Unexpected value: " + func);
		},
		name -> switch (name) {
		case "exponent" -> ExponentDynamicFunction.CODEC.fieldOf("exponent");
		default -> throw new IllegalArgumentException("Unexpected value: " + name);
		});

	class ExponentDynamicFunction implements DynamicFunction {
		public static final Codec<ExponentDynamicFunction> CODEC = Codec.FLOAT.xmap(
			ExponentDynamicFunction::new,
			ExponentDynamicFunction::getExponent);

		private float[] exponent = { 1f, -1f };
		private float[] imguiPlot = new float[128];

		public ExponentDynamicFunction(float exponent) {
			this.exponent[0] = exponent;
		}

		public ExponentDynamicFunction() {
			this(1f);
		}

		@Override
		public float apply(float operand) {
			return (float) Math.pow(operand, exponent[0]);
		}

		public float getExponent() { return exponent[0]; }

		public void setExponent(float exponent) { this.exponent[0] = exponent; }

		@Override
		public void renderImGui() {
			ImGui.sliderFloat("Exponent", exponent, 0.01f, 10f, "%.02f", ImGuiSliderFlags.Logarithmic);

			if (exponent[0] != exponent[1]) {
				for (int i = 0; i < imguiPlot.length; i++) {
					float p = i / (float) (imguiPlot.length - 1);
					imguiPlot[i] = apply(p);
				}
			}

			ImGui.plotLines(
				"Graph", imguiPlot, imguiPlot.length, 0, "", 0f, 1f,
				ImGui.calcItemWidth(), ImGui.calcItemWidth());
		}
	}
}
