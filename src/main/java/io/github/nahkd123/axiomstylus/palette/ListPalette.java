package io.github.nahkd123.axiomstylus.palette;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;

public record ListPalette(List<BlockState> states) implements Palette {
	public static final Codec<ListPalette> CODEC = BlockState.CODEC
		.listOf(1, Integer.MAX_VALUE)
		.xmap(ListPalette::new, ListPalette::states);

	@Override
	public int size() {
		return states.size();
	}

	@Override
	public BlockState get(int index) {
		return states.get(index);
	}
}
