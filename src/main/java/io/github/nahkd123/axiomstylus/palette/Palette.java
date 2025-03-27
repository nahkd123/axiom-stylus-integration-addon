package io.github.nahkd123.axiomstylus.palette;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import imgui.ImGui;
import io.github.nahkd123.axiomstylus.preset.PresetConfigurator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.random.Random;

/**
 * <p>
 * A palette is an <em>immutable</em> sequence of {@link BlockState}.
 * </p>
 */
public interface Palette {
	String getName();

	String getDescription();

	int size();

	BlockState get(int index);

	default BlockState getGradient(float p) {
		return get(Math.round(p * (size() - 1)));
	}

	default BlockState getRandom(Random rng) {
		return get(rng.nextBetweenExclusive(0, size()));
	}

	default PresetConfigurator<? extends Palette> createConfigurator() {
		return PresetConfigurator.empty();
	}

	static PresetConfigurator<Palette> createAllConfigurator(Palette palette) {
		List<Palette> defaults = List.of(SpecialPalette.CURRENT_BLOCK);

		class ConfiguratorImpl implements PresetConfigurator<Palette> {
			int index = switch (palette) {
			case SpecialPalette special -> switch (special) {
				case CURRENT_BLOCK -> 0;
			};
			default -> throw new IllegalArgumentException("Unexpected value: " + palette);
			};
			PresetConfigurator<? extends Palette> child = palette.createConfigurator();

			@Override
			public void renderImGui(Consumer<Palette> applyCallback) {
				if (ImGui.beginCombo("Type", defaults.get(index).getName())) {
					for (int i = 0; i < defaults.size(); i++) {
						Palette def = defaults.get(i);

						if (ImGui.selectable(def.getName())) {
							index = i;
							applyCallback.accept(def);
							child = def.createConfigurator();
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

	static Codec<Palette> CODEC = Codec.STRING.dispatch(
		"type",
		palette -> switch (palette) {
		case SpecialPalette _ -> "special";
		default -> throw new IllegalArgumentException("Unexpected value: " + palette);
		},
		key -> switch (key) {
		case "special" -> SpecialPalette.CODEC.fieldOf("special");
		default -> throw new IllegalArgumentException("Unexpected value: " + key);
		});
}
