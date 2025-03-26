package io.github.nahkd123.axiomstylus.tool;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.moulberry.axiomclientapi.Effects;
import com.moulberry.axiomclientapi.regions.BlockRegion;
import com.moulberry.axiomclientapi.regions.BooleanRegion;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import io.github.nahkd123.axiomstylus.AxiomStylusAddon;
import io.github.nahkd123.axiomstylus.input.InputReport;
import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.preset.BrushPresetEditor;
import io.github.nahkd123.axiomstylus.preset.SavedBrushPreset;
import io.github.nahkd123.axiomstylus.preset.TipShape;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicSource;
import io.github.nahkd123.axiomstylus.utils.AsImGui;
import io.github.nahkd123.axiomstylus.utils.Box3d;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class PresetBrushTool extends CustomStylusTool {
	private ImBoolean settingsBrowserToggle = new ImBoolean(false);
	private ImBoolean presetEditorToggle = new ImBoolean(false);
	private BrushPresetEditor presetEditor = new BrushPresetEditor();

	private BlockRegion commit = null;
	private BooleanRegion removals = AxiomStylusAddon.REGION.createBoolean();
	private Vec3d prevTipPosition = null;
	private InputReport prevInput = null;

	@Override
	public String name() {
		return "Preset Brush (Stylus Integration)";
	}

	@Override
	public void reset() {
		super.reset();
		commit = AxiomStylusAddon.REGION.createBlock();
		removals.clear();
		prevTipPosition = null;
		prevInput = null;
	}

	@Override
	public void render(Camera camera, float tickDelta, long time, MatrixStack poseStack, Matrix4f projection) {
		super.render(camera, tickDelta, time, poseStack, projection);
		if (commit != null) commit.render(camera, Vec3d.ZERO, poseStack, projection, 0.5f, 1f);
		removals.render(camera, Vec3d.ZERO, poseStack, projection, time, Effects.REMOVAL);
	}

	@Override
	protected void onBrushBegin() {
		reset();
	}

	@Override
	protected void onBrushInput(Vec3d tipPosition, InputReport input) {
		presetEditor.spacing().interpolate(prevTipPosition, prevInput, tipPosition, input, this::onDabInput);
	}

	private void onDabInput(Vec3d tipPosition, InputReport input) {
		prevTipPosition = tipPosition;
		prevInput = input;

		Palette palette = presetEditor.palette();
		Matrix4f mat4 = new Matrix4f();
		presetEditor.shapeDynamics().forEach(d -> {
			float value = d.function().apply(switch (d.source()) {
			case DynamicSource.NORMAL_PRESSURE -> input.pressure();
			default -> 0f;
			});
			d.destination().addValue(mat4, value);
		});

		Matrix4f mat4Inv = mat4.invert(new Matrix4f());
		TipShape shape = presetEditor.shape();
		Box3d bb = shape.getBoundingBox().tranformVerticesAndEnclose(new Matrix4d(mat4), 1d);
		Vector3f originInTip = mat4Inv.transformProject(tipPosition.toVector3f());

		for (int y = -1; y <= bb.sizeY() + 0.5; y++) {
			for (int z = -1; z <= bb.sizeZ() + 0.5; z++) {
				for (int x = -1; x <= bb.sizeX() + 0.5; x++) {
					double lx = bb.x() + x;
					double ly = bb.y() + y;
					double lz = bb.z() + z;
					Vector4f localInTip = mat4Inv.transform(
						(float) lx, (float) ly, (float) lz, 1f,
						new Vector4f());
					int wx = (int) (tipPosition.x + lx);
					int wy = (int) (tipPosition.y + ly);
					int wz = (int) (tipPosition.z + lz);
					if (!shape.test(
						originInTip.x, originInTip.y, originInTip.z,
						localInTip.x, localInTip.y, localInTip.z)) continue;
					commit.addBlockIfNotPresent(wx, wy, wz, palette.get(0));
					// TODO support brush operation - add and subtract
				}
			}
		}
	}

	@Override
	protected void onBrushFinish() {
		AxiomStylusAddon.TOOL_SERVICE.pushBlockRegionChange(commit);
		reset();
	}

	@Override
	public void displayImguiOptions() {
		AsImGui.separatorText("Quick preset");
		ImGui.button("+ New preset slot");

		AsImGui.separatorText("Quick settings");
		ImGui.text("(No quick settings)");
		ImGui.checkbox("Browse settings", settingsBrowserToggle);

		AsImGui.separatorText("Advanced");
		ImGui.checkbox("Preset editor", presetEditorToggle);
		ImGui.beginDisabled();
		ImGui.button("Import preset");
		ImGui.endDisabled();
		ImGui.sameLine();
		if (ImGui.button("Export preset")) ImGui.openPopup("Stylus - Export Brush Preset");

		if (presetEditorToggle.get()) {
			ImGui.setNextWindowPos(
				ImGui.getWindowPosX() + ImGui.getWindowWidth(),
				ImGui.getWindowPosY(),
				ImGuiCond.FirstUseEver, 0f, 0f);
			ImGui.setNextWindowSize(400f, 800f, ImGuiCond.FirstUseEver);
			if (ImGui.begin("Stylus - Brush Preset")) presetEditor.renderImGui();
			ImGui.end();
		}

		if (ImGui.beginPopupModal("Stylus - Export Brush Preset")) {
			ImGui.text("You are exporting the brush preset");

			if (ImGui.button("Copy brush preset to clipboard")) {
				SavedBrushPreset saved = SavedBrushPreset.savedPresetOf(presetEditor);
				var result = SavedBrushPreset.CODEC.codec().encodeStart(JsonOps.INSTANCE, saved);
				var json = result.resultOrPartial().orElse(null);
				if (json != null) ImGui.setClipboardText(json.toString());
				else AxiomStylusAddon.LOGGER.error(result.error()
					.map(DataResult.Error::message)
					.orElse("Unable to export preset to clipboard"));
			}

			ImGui.button("Save brush preset as file");
			ImGui.endPopup();
		}
	}
}
