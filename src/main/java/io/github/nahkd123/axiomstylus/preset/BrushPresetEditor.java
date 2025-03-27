package io.github.nahkd123.axiomstylus.preset;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import imgui.ImGui;
import imgui.type.ImString;
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

	private ImString name = new ImString("Brush", 256);
	private TipShape shape = TipShape.sphere(5d);
	private BrushSpacing spacing = BrushSpacing.even(0.5);
	private Palette palette = SpecialPalette.CURRENT_BLOCK;
	private List<BrushDynamic<Matrix4f>> shapeDynamics = new ArrayList<>();

	private TipShapeEditor tipShapeEditor;
	private PaletteEditor paletteEditor;
	private BrushDynamicListWidget dynamicList = new BrushDynamicListWidget();

	public BrushPresetEditor() {
		// @formatter:off
		shapeDynamics.add(new BrushDynamic<>(
			DynamicSource.NORMAL_PRESSURE,
			DynamicFunction.Simple.IDENTITY,
			Matrix4fDynamicTarget.SCALE));
		// @formatter:on
		resetChildEditors();
	}

	@Override
	public String name() {
		return name.get();
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
		name.set(preset.name());
		shape = preset.shape();
		spacing = preset.spacing();
		palette = preset.palette();
		shapeDynamics = new ArrayList<>();
		shapeDynamics.addAll(preset.shapeDynamics().stream().map(BrushDynamic::makeCopy).toList());
		resetChildEditors();
	}

	private void resetChildEditors() {
		tipShapeEditor = new TipShapeEditorImpl(shape);
		paletteEditor = new PaletteEditorImpl(palette);
		dynamicList.reset();
	}

	public void renderImGui() {
		ImGui.pushID("General");
		AsImGui.separatorText("General");
		ImGui.inputText("Name", name);
		ImGui.popID();

		ImGui.pushID("Brush Tip");
		AsImGui.separatorText("Brush Tip");
		tipShapeEditor.renderImGui();
		if (ImGui.collapsingHeader("Brush Dynamics")) dynamicList.renderList(shapeDynamics, BRUSH_TIP_TARGETS, true);
		ImGui.popID();

		ImGui.pushID("Palette");
		AsImGui.separatorText("Palette");
		paletteEditor.renderImGui();
		ImGui.popID();

		dynamicList.renderWindows();
	}

	private class TipShapeEditorImpl extends TipShapeEditor {
		public TipShapeEditorImpl(TipShape shape) {
			super(shape);
		}

		@Override
		protected void onShapeChanged(TipShape oldShape, TipShape newShape) {
			shape = newShape;
		}
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
