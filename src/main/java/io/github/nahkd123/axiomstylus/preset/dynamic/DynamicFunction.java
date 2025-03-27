package io.github.nahkd123.axiomstylus.preset.dynamic;

import static imgui.flag.ImGuiSliderFlags.Logarithmic;
import static io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunctionInternal.CLASS_TO_ID;
import static io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunctionInternal.CLASS_TO_INDICES;
import static io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunctionInternal.ID_TO_CODEC;
import static io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunctionInternal.INDICES_TO_DEFAULT;
import static io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunctionInternal.imGuiLabels;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import imgui.ImGui;
import imgui.type.ImBoolean;
import io.github.nahkd123.axiomstylus.preset.PresetConfigurator;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;

/**
 * <p>
 * Map value from sensor to value that can be controlled by user.
 * </p>
 */
public interface DynamicFunction extends FloatUnaryOperator {
	default String getName() { return CLASS_TO_ID.get(this.getClass()); }

	default String getDescription() { return "A dynamic function that maps input to output"; }

	default PresetConfigurator<? extends DynamicFunction> createConfigurator() {
		return PresetConfigurator.empty();
	}

	static MapCodec<DynamicFunction> CODEC = Codec.STRING.dispatchMap(
		"type",
		func -> CLASS_TO_ID.get(func.getClass()),
		ID_TO_CODEC::get);

	@SuppressWarnings("unchecked")
	static <T extends DynamicFunction> void registerMapCodec(String[] keys, MapCodec<T> codec, T def) {
		if (CLASS_TO_ID.putIfAbsent(def.getClass(), keys[0]) != null)
			throw new IllegalArgumentException("%s already registered".formatted(def.getClass()));
		for (String key : keys) ID_TO_CODEC.put(key, codec.xmap(Function.identity(), v -> (T) v));

		String[] oldLabels = imGuiLabels;
		imGuiLabels = new String[imGuiLabels.length + 1];
		System.arraycopy(oldLabels, 0, imGuiLabels, 0, oldLabels.length);
		imGuiLabels[oldLabels.length] = def.getName();
		CLASS_TO_INDICES.put(def.getClass(), oldLabels.length);
		INDICES_TO_DEFAULT.add(def);
	}

	static <T extends DynamicFunction> void registerValueCodec(String[] keys, Codec<T> codec, T def) {
		registerMapCodec(keys, codec.optionalFieldOf("value", def), def);
	}

	static void initialize() {
		registerValueCodec(new String[] { "identity" }, Codec.unit(Simple.IDENTITY), Simple.IDENTITY);
		registerMapCodec(
			new String[] { "parametric", "exponent" },
			Parametric.CODEC,
			new Parametric(0f, 0f, 1f, 1f, false, true, true));
	}

	static PresetConfigurator<DynamicFunction> createAllConfigurator(DynamicFunction function) {
		class ConfiguratorImpl implements PresetConfigurator<DynamicFunction> {
			int typeIndex = CLASS_TO_INDICES.get(function.getClass());
			PresetConfigurator<? extends DynamicFunction> child = function.createConfigurator();

			@Override
			public void renderImGui(Consumer<DynamicFunction> applyCallback) {
				if (ImGui.beginCombo("Type", INDICES_TO_DEFAULT.get(typeIndex).getName())) {
					for (int i = 0; i < INDICES_TO_DEFAULT.size(); i++) {
						DynamicFunction def = INDICES_TO_DEFAULT.get(i);

						if (ImGui.selectable(def.getName())) {
							typeIndex = i;
							child = def.createConfigurator();
							applyCallback.accept(def);
						}

						if (ImGui.isItemHovered()) ImGui.setTooltip(def.getDescription());
					}

					ImGui.endCombo();
				}

				child.renderImGui(c -> applyCallback.accept(c));
			}
		}

		return new ConfiguratorImpl();
	}

	enum Simple implements DynamicFunction {
		IDENTITY;

		@Override
		public String getName() { return "Identity"; }

		@Override
		public String getDescription() { return "Pass value from source as-is"; }

		@Override
		public float apply(float x) {
			return x;
		}
	}

	record Parametric(float vShift, float hShift, float gain, float exponent, boolean flip, boolean clampIn, boolean clampOut) implements DynamicFunction {

		public static final MapCodec<Parametric> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.floatRange(-1f, 1f).optionalFieldOf("vShift", 0f).forGetter(Parametric::vShift),
			Codec.floatRange(-1f, 1f).optionalFieldOf("hShift", 0f).forGetter(Parametric::hShift),
			Codec.floatRange(0f, 10f).optionalFieldOf("gain", 1f).forGetter(Parametric::gain),
			Codec.floatRange(0f, 10f).optionalFieldOf("exponent", 1f).forGetter(Parametric::exponent),
			Codec.BOOL.optionalFieldOf("flip", false).forGetter(Parametric::flip),
			Codec.BOOL.optionalFieldOf("clampIn", true).forGetter(Parametric::clampIn),
			Codec.BOOL.optionalFieldOf("clampOut", true).forGetter(Parametric::clampOut))
			.apply(instance, Parametric::new));

		@Override
		public String getName() { return "Parametric"; }

		@Override
		public String getDescription() { return "Control input with parametric curve. Useful for pen pressure."; }

		public static float applyParametric(float operand, float vShift, float hShift, float gain, float exponent, boolean flip, boolean clampIn, boolean clampOut) {
			float in = clampIn ? Math.clamp(operand - hShift, 0f, 1f) : operand - hShift;
			float v = (float) (vShift + Math.pow(in, exponent) * gain);
			if (Float.isNaN(v)) v = 0f;
			float output = flip ? (1f - v) : v;
			return clampOut ? Math.clamp(output, 0f, 1f) : output;
		}

		@Override
		public float apply(float operand) {
			return applyParametric(operand, vShift, hShift, gain, exponent, flip, clampIn, clampOut);
		}

		@Override
		public PresetConfigurator<Parametric> createConfigurator() {
			float[] vShift = { this.vShift };
			float[] hShift = { this.hShift };
			float[] gain = { this.gain };
			float[] exponent = { this.exponent };
			float[] imguiPlot = new float[128];
			ImBoolean flip = new ImBoolean(this.flip);
			ImBoolean clampIn = new ImBoolean(this.clampIn);
			ImBoolean clampOut = new ImBoolean(this.clampOut);

			for (int i = 0; i < imguiPlot.length; i++) {
				float p = i / (float) (imguiPlot.length - 1);
				imguiPlot[i] = applyParametric(p,
					vShift[0], hShift[0], gain[0], exponent[0],
					flip.get(), clampIn.get(), clampOut.get());
			}

			return applyCallback -> {
				boolean changed = false;
				changed |= ImGui.checkbox("Flip graph", flip);
				changed |= ImGui.checkbox("Clamp input", clampIn);
				changed |= ImGui.checkbox("Clamp output", clampOut);
				changed |= ImGui.sliderFloat("Shift X", hShift, -1f, 1f, "%.02f");
				changed |= ImGui.sliderFloat("Shift Y", vShift, -1f, 1f, "%.02f");
				changed |= ImGui.sliderFloat("Gain", gain, 0.01f, 10f, "%.02f", Logarithmic);
				changed |= ImGui.sliderFloat("Exponent", exponent, 0.01f, 10f, "%.02f", Logarithmic);

				if (changed) {
					for (int i = 0; i < imguiPlot.length; i++) {
						float p = i / (float) (imguiPlot.length - 1);
						imguiPlot[i] = applyParametric(p,
							vShift[0], hShift[0], gain[0], exponent[0],
							flip.get(), clampIn.get(), clampOut.get());
					}

					// @formatter:off
					applyCallback.accept(new Parametric(vShift[0], hShift[0], gain[0], exponent[0], flip.get(), clampIn.get(), clampOut.get()));
					// @formatter:on
				}

				ImGui.plotLines(
					"Graph", imguiPlot, imguiPlot.length, 0, "", 0f, 1f,
					ImGui.calcItemWidth(), ImGui.calcItemWidth());
			};
		}
	}
}
