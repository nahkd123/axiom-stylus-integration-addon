package io.github.nahkd123.axiomstylus.preset;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix4f;

import imgui.ImGui;
import imgui.type.ImString;
import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.palette.SpecialPalette;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamic;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamicListWidget;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicTarget;
import io.github.nahkd123.axiomstylus.preset.dynamic.Matrix4fDynamicTarget;
import io.github.nahkd123.axiomstylus.utils.AsImGui;

public class BrushPresetConfigurator implements PresetConfigurator<BrushPreset> {
	private static final List<DynamicTarget<Matrix4f>> BRUSH_TIP_TARGETS = List.of(Matrix4fDynamicTarget.values());

	private ImString name = new ImString("Brush", 256);
	private TipShape shape = new TipShape.Sphere(5, 5, 5);
	private BrushSpacing spacing = BrushSpacing.even(0.5);
	private Palette palette = SpecialPalette.CURRENT_BLOCK;
	private List<BrushDynamic<Matrix4f>> shapeDynamics = new ArrayList<>();

	private PresetConfigurator<TipShape> tipShapeConfig;
	private PresetConfigurator<Palette> paletteConfig;
	private BrushDynamicListWidget dynamicList = new BrushDynamicListWidget();

	public BrushPresetConfigurator(String name, TipShape shape, BrushSpacing spacing, Palette palette, List<BrushDynamic<Matrix4f>> shapeDynamics) {
		this.name.set(name);
		this.shape = shape;
		this.spacing = spacing;
		this.palette = palette;
		this.shapeDynamics.addAll(shapeDynamics);
		resetChildEditors();
	}

	public BrushPresetConfigurator(BrushPreset preset) {
		loadPreset(preset);
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
		tipShapeConfig = TipShape.createAllConfigurator(shape);
		paletteConfig = Palette.createAllConfigurator(palette);
		dynamicList.reset();
	}

	@Override
	public void renderImGui(Consumer<BrushPreset> applyCallback) {
		ImGui.pushID("General");
		AsImGui.separatorText("General");
		ImGui.inputText("Name", name);
		ImGui.popID();

		ImGui.pushID("Brush Tip");
		AsImGui.separatorText("Brush Tip");
		tipShapeConfig.renderImGui(newShape -> {
			shape = newShape;
			applyCallback.accept(new SavedBrushPreset(name.get(), shape, spacing, palette, shapeDynamics));
		});
		if (ImGui.collapsingHeader("Brush Dynamics")) dynamicList.renderList(shapeDynamics, BRUSH_TIP_TARGETS, true);
		ImGui.popID();

		ImGui.pushID("Palette");
		AsImGui.separatorText("Palette");
		paletteConfig.renderImGui(newPalette -> {
			palette = newPalette;
			applyCallback.accept(new SavedBrushPreset(name.get(), shape, spacing, palette, shapeDynamics));
		});
		ImGui.popID();

		dynamicList.renderWindows();
	}
}
