package io.github.nahkd123.axiomstylus.preset.dynamic;

import static imgui.flag.ImGuiTableFlags.BordersH;
import static imgui.flag.ImGuiTableFlags.BordersOuterV;

import java.util.List;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiTableColumnFlags;

public class BrushDynamicListWidget {
	private IndexedBrushDynamicEditor<?> editor = null;

	public void reset() {
		editor = null;
	}

	public <T> void renderList(List<BrushDynamic<T>> list, List<DynamicTarget<T>> allTargets, boolean allowAdding) {
		int removeLater = -1;

		if (ImGui.beginTable("Dynamics Table", 3, BordersOuterV | BordersH)) {
			ImGui.tableSetupColumn("Source");
			ImGui.tableSetupColumn("Target");
			ImGui.tableSetupColumn("", ImGuiTableColumnFlags.WidthFixed, ImGui.calcTextSize("...").x);
			ImGui.tableHeadersRow();

			for (int i = 0; i < list.size(); i++) {
				BrushDynamic<T> dynamic = list.get(i);
				boolean selected = editor != null && editor.list.get(editor.index) == dynamic;

				ImGui.pushID(i);
				ImGui.tableNextRow();
				ImGui.tableSetColumnIndex(0);
				if (ImGui.selectable(
					dynamic.source().getName(), selected,
					ImGuiSelectableFlags.SpanAllColumns | ImGuiSelectableFlags.AllowItemOverlap)) {
					if (selected) editor = null;
					else editor = new IndexedBrushDynamicEditor<>(list, i) {
						@Override
						protected List<DynamicTarget<T>> getAllTargets() { return allTargets; }
					};
				}

				ImGui.tableSetColumnIndex(1);
				ImGui.text(dynamic.destination().getName());

				ImGui.tableSetColumnIndex(2);
				if (ImGui.selectable("...")) { ImGui.openPopup("Dynamic Entry Option"); }

				if (ImGui.beginPopup("Dynamic Entry Option")) {
					ImGui.menuItem("Move up", "", false, false);
					ImGui.menuItem("Move down", "", false, false);
					ImGui.menuItem("Duplicate", "", false, false);

					if (ImGui.menuItem("Delete")) {
						editor = null;
						removeLater = i;
					}

					ImGui.endPopup();
				}

				ImGui.popID();
			}

			ImGui.tableNextRow();
			ImGui.tableSetColumnIndex(0);
			if (ImGui.selectable("+ Add new", false, ImGuiSelectableFlags.SpanAllColumns)) {
				var source = DynamicSource.NORMAL_PRESSURE;
				var function = DynamicFunction.Simple.IDENTITY;
				var target = allTargets.get(0);
				var entry = new BrushDynamic<>(source, function, target);
				list.add(entry);
				editor = new IndexedBrushDynamicEditor<>(list, list.size() - 1) {
					@Override
					protected List<DynamicTarget<T>> getAllTargets() { return allTargets; }
				};
			}

			ImGui.endTable();
		}

		if (removeLater != -1) list.remove(removeLater);
	}

	public void renderWindows() {
		if (editor != null) {
			ImGui.setNextWindowPos(
				ImGui.getWindowPosX() + ImGui.getWindowWidth(),
				ImGui.getWindowPosY(),
				ImGuiCond.FirstUseEver, 0f, 0f);
			ImGui.setNextWindowSize(400f, 600f, ImGuiCond.FirstUseEver);
			if (ImGui.begin("Stylus - Dynamic Editor")) editor.renderImGui();
			ImGui.end();
		}
	}

	private abstract class IndexedBrushDynamicEditor<T> extends BrushDynamicEditor<T> {
		private List<BrushDynamic<T>> list;
		private int index;

		public IndexedBrushDynamicEditor(List<BrushDynamic<T>> list, int index) {
			super(list.get(index).source(), list.get(index).function(), list.get(index).destination());
			this.list = list;
			this.index = index;
		}

		@Override
		protected void onDataChanged(BrushDynamic<T> oldData, BrushDynamic<T> newData) {
			list.set(index, newData);
		}
	}
}
