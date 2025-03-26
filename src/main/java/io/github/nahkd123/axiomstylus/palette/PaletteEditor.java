package io.github.nahkd123.axiomstylus.palette;

import java.util.function.Supplier;

import imgui.ImGui;
import imgui.type.ImInt;

public abstract class PaletteEditor {
	private Palette palette;

	@SuppressWarnings("unchecked")
	private Supplier<Palette>[] allSuppliers = new Supplier[] {
		() -> SpecialPalette.CURRENT_BLOCK
	};
	private String[] allTypes = {
		"Current block"
	};
	private ImInt typeIndex;

	public PaletteEditor(Palette palette) {
		this.palette = palette;
		this.typeIndex = new ImInt(switch (palette) {
		case SpecialPalette s -> switch (s) {
			case CURRENT_BLOCK -> 0;
		};
		default -> throw new IllegalArgumentException("Unexpected value: " + palette);
		});
	}

	protected abstract void onPaletteChanged(Palette oldPalette, Palette newPalette);

	public void renderImGui() {
		if (ImGui.combo("Type", typeIndex, allTypes)) {
			Palette old = palette;
			palette = allSuppliers[typeIndex.get()].get();
			onPaletteChanged(old, palette);
		}
	}
}
