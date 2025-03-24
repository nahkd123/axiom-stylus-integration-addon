package io.github.nahkd123.axiomstylus.utils;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;

public class AsImGui {
	public static void separatorText(String text, float thickness) {
		ImVec2 labelSize = ImGui.calcTextSize(text);

		if (labelSize.x > 0f) {
			ImDrawList drawList = ImGui.getWindowDrawList();
			ImVec2 cursor = ImGui.getCursorScreenPos();
			int color = ImColor.rgba(ImGui.getStyle().getColor(ImGuiCol.Separator));
			int textColor = ImColor.rgba(ImGui.getStyle().getColor(ImGuiCol.Text));
			float sxStart = cursor.x;
			float sxLabelStart = sxStart + 10f;
			float sxLabelEnd = sxLabelStart + 10f + labelSize.x + 10f;
			float sxEnd = cursor.x + ImGui.getWindowWidth();
			float sy = cursor.y + labelSize.y / 2f;

			drawList.addLine(sxStart, sy, sxLabelStart, sy, color, thickness);
			drawList.addLine(sxLabelEnd, sy, sxEnd, sy, color, thickness);
			drawList.addText(sxLabelStart + 10f, cursor.y, textColor, text);
			ImGui.setCursorScreenPos(cursor.x, cursor.y + labelSize.y * 1.2f);
		} else {
			ImGui.separator();
		}
	}

	public static void separatorText(String text) {
		separatorText(text, 2f);
	}
}
