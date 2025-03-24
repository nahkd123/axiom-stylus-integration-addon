package io.github.nahkd123.axiomstylus.preset;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiSelectableFlags;
import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.palette.SpecialPalette;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamic;
import io.github.nahkd123.axiomstylus.preset.dynamic.BrushDynamicEditor;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunction;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicSource;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicTarget;
import io.github.nahkd123.axiomstylus.preset.dynamic.Matrix4fDynamicTarget;
import io.github.nahkd123.axiomstylus.utils.AsImGui;

public class BrushPresetEditor implements BrushPreset {
	private TipShape shape = TipShape.sphere(5d);
	private BrushSpacing spacing = BrushSpacing.even(0.5);
	private Palette palette = SpecialPalette.CURRENT_BLOCK;
	private List<BrushDynamic<Matrix4f>> shapeDynamics = new ArrayList<>();

	private int[] selectedDynamic = { -1, -1 };
	private BrushDynamicEditor<?> dynamicEditor = null;

	public BrushPresetEditor() {
		// @formatter:off
		shapeDynamics.add(new BrushDynamic<>(
			DynamicSource.NORMAL_PRESSURE,
			new DynamicFunction.ExponentDynamicFunction(),
			Matrix4fDynamicTarget.SCALE));
		// @formatter:on
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
	}

	public void renderImGui() {
		ImGui.pushID("General");
		AsImGui.separatorText("General");
		ImGui.popID();

		ImGui.pushID("Palette");
		AsImGui.separatorText("Palette");
		ImGui.text("TODO");
		ImGui.popID();

		ImGui.pushID("Dynamics");
		AsImGui.separatorText("Dynamics");

		int actionIndex = -1;
		boolean duplicate = false, delete = false;

		if (ImGui.collapsingHeader("Shape Dynamics")) {
			if (ImGui.beginTable("shape_dynamics_table", 3)) {
				ImGui.tableSetupColumn("Source");
				ImGui.tableSetupColumn("Target");
				ImGui.tableSetupColumn("Actions");
				ImGui.tableHeadersRow();

				for (int i = 0; i < shapeDynamics.size(); i++) {
					BrushDynamic<Matrix4f> dynamic = shapeDynamics.get(i);
					boolean selected = selectedDynamic[0] == 0 && selectedDynamic[1] == i;

					ImGui.pushID(i);
					ImGui.tableNextRow();
					ImGui.tableSetColumnIndex(0);
					boolean select = ImGui.selectable(
						dynamic.source().getName(), selected,
						ImGuiSelectableFlags.SpanAllColumns | ImGuiSelectableFlags.AllowItemOverlap);
					if (ImGui.isItemHovered()) ImGui.setTooltip(dynamic.source().getDescription());
					ImGui.tableSetColumnIndex(1);
					ImGui.text(dynamic.destination().getName());
					if (ImGui.isItemHovered()) ImGui.setTooltip(dynamic.destination().getDescription());
					ImGui.tableSetColumnIndex(2);
					boolean dup = ImGui.button("+ Dup");
					if (ImGui.isItemHovered()) ImGui.setTooltip("Duplicate dynamic entry");
					ImGui.sameLine();
					boolean del = ImGui.button("- Del");
					if (ImGui.isItemHovered()) ImGui.setTooltip("Delete dynamic entry");
					ImGui.popID();

					if (select) {
						if (selected) {
							selectedDynamic[0] = -1;
							dynamicEditor = null;
						} else {
							selectedDynamic[0] = 0;
							selectedDynamic[1] = i;
							dynamicEditor = new ShapeBrushDynamicEditor(i);
						}
					} else if (dup) {
						actionIndex = i;
						duplicate = true;
					} else if (del) {
						actionIndex = i;
						delete = true;
					}
				}

				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(2);
				boolean add = ImGui.button("+ Add");
				if (ImGui.isItemHovered()) ImGui.setTooltip("Add new dynamic entry");
				ImGui.endTable();

				if (add) {
					var source = DynamicSource.NORMAL_PRESSURE;
					var function = new DynamicFunction.ExponentDynamicFunction();
					var target = Matrix4fDynamicTarget.SCALE;
					shapeDynamics.add(new BrushDynamic<>(source, function, target));
					selectedDynamic[0] = 0;
					selectedDynamic[1] = shapeDynamics.size() - 1;
					dynamicEditor = new ShapeBrushDynamicEditor(selectedDynamic[1]);
				}
			}
		}
		ImGui.popID();

		if (duplicate) {
			// TODO
		}

		if (delete) {
			if (selectedDynamic[0] == 0 && selectedDynamic[1] == actionIndex) {
				selectedDynamic[0] = -1;
				dynamicEditor = null;
			}

			shapeDynamics.remove(actionIndex);
		}

		if (dynamicEditor != null && selectedDynamic[0] == 0) {
			ImGui.setNextWindowPos(
				ImGui.getWindowPosX() + ImGui.getWindowWidth(),
				ImGui.getWindowPosY(),
				ImGuiCond.FirstUseEver, 0f, 0f);
			ImGui.setNextWindowSize(400f, 600f, ImGuiCond.FirstUseEver);
			if (ImGui.begin("Stylus - Dynamic Editor")) dynamicEditor.renderImGui();
			ImGui.end();
		}
	}

	private class ShapeBrushDynamicEditor extends BrushDynamicEditor<Matrix4f> {
		private int index;

		public ShapeBrushDynamicEditor(int index) {
			// @formatter:off
			super(
				shapeDynamics.get(index).source(),
				shapeDynamics.get(index).function(),
				shapeDynamics.get(index).destination());
			// @formatter:on
			this.index = index;
		}

		@Override
		protected List<DynamicTarget<Matrix4f>> getAllTargets() { return List.of(Matrix4fDynamicTarget.values()); }

		@Override
		protected void onDataChanged(BrushDynamic<Matrix4f> oldData, BrushDynamic<Matrix4f> newData) {
			shapeDynamics.set(index, newData);
		}
	}
}
