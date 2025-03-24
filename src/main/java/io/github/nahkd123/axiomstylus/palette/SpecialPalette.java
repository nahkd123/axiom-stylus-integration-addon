package io.github.nahkd123.axiomstylus.palette;

import com.mojang.serialization.Codec;

import io.github.nahkd123.axiomstylus.AxiomStylusAddon;
import net.minecraft.block.BlockState;

public enum SpecialPalette implements Palette {
	/**
	 * <p>
	 * The block that user is currently selecting in Axiom editor.
	 * </p>
	 */
	CURRENT_BLOCK {
		@Override
		public int size() {
			return 1;
		}

		@Override
		public BlockState get(int index) {
			if (index != 0) throw new IllegalArgumentException("Index must be 0");
			return AxiomStylusAddon.TOOL_SERVICE.getActiveBlock();
		}
	};

	public static final Codec<SpecialPalette> CODEC = Codec.stringResolver(
		SpecialPalette::toString,
		SpecialPalette::valueOf);
}
