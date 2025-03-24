package io.github.nahkd123.axiomstylus.palette;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.random.Random;

/**
 * <p>
 * A palette is an <em>immutable</em> sequence of {@link BlockState}.
 * </p>
 */
public interface Palette {
	int size();

	BlockState get(int index);

	default BlockState getGradient(float p) {
		return get(Math.round(p * (size() - 1)));
	}

	default BlockState getRandom(Random rng) {
		return get(rng.nextBetweenExclusive(0, size()));
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
