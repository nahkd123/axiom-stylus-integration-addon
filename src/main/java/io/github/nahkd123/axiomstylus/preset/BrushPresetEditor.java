package io.github.nahkd123.axiomstylus.preset;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import imgui.ImGui;
import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.palette.PaletteEditor;
import io.github.nahkd123.axiomstylus.palette.SpecialPalette;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamic;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamicListWidget;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunction;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicSource;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicTarget;
import io.github.nahkd123.axiomstylus.preset.dynamic.Matrix4fDynamicTarget;
import io.github.nahkd123.axiomstylus.utils.AsImGui;

public class BrushPresetEditor implements BrushPreset {
	private static final List<DynamicTarget<Matrix4f>> BRUSH_TIP_TARGETS = List.of(Matrix4fDynamicTarget.values());

	private TipShape shape = TipShape.sphere(5d);
	private BrushSpacing spacing = BrushSpacing.even(0.5);
	private Palette palette = SpecialPalette.CURRENT_BLOCK;
	private List<BrushDynamic<Matrix4f>> shapeDynamics = new ArrayList<>();

	private PaletteEditor paletteEditor;
	private BrushDynamicListWidget dynamicList = new BrushDynamicListWidget();

	public BrushPresetEditor() {
		// @formatter:off
		shapeDynamics.add(new BrushDynamic<>(
			DynamicSource.NORMAL_PRESSURE,
			new DynamicFunction.Parametric(),
			Matrix4fDynamicTarget.SCALE));
		// @formatter:on
		resetChildEditors();
	}

	@Override
	public TipShape shape() {
		return shape;
	}

	@Override
	public BrushSpacing spacing() {
		return spacing;
	}

	@Override
	public Palette palette() {
		return palette;
	}

	@Override
	public List<BrushDynamic<Matrix4f>> shapeDynamics() {
		return shapeDynamics;
	}

	public void loadPreset(BrushPreset preset) {
		shape = preset.shape();
		spacing = preset.spacing();
		palette = preset.palette();
		shapeDynamics = new ArrayList<>();
		shapeDynamics.addAll(preset.shapeDynamics());
		resetChildEditors();
	}

	private void resetChildEditors() {
		paletteEditor = new PaletteEditorImpl(palette);
		dynamicList.reset();
	}

	public void renderImGui() {
		ImGui.pushID("Brush Tip");
		AsImGui.separatorText("Brush Tip");
		if (ImGui.collapsingHeader("Brush Dynamics")) dynamicList.renderList(shapeDynamics, BRUSH_TIP_TARGETS, true);
		ImGui.popID();

		ImGui.pushID("Palette");
		AsImGui.separatorText("Palette");
		paletteEditor.renderImGui();
		ImGui.popID();

		dynamicList.renderWindows();
	}

	private class PaletteEditorImpl extends PaletteEditor {
		public PaletteEditorImpl(Palette palette) {
			super(palette);
		}

		@Override
		protected void onPaletteChanged(Palette oldPalette, Palette newPalette) {
			palette = newPalette;
		}
	}
}
