package io.github.nahkd123.axiomstylus.tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.FormattingStyle;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.moulberry.axiomclientapi.Effects;
import com.moulberry.axiomclientapi.regions.BlockRegion;
import com.moulberry.axiomclientapi.regions.BooleanRegion;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import io.github.nahkd123.axiomstylus.AxiomStylusAddon;
import io.github.nahkd123.axiomstylus.brush.BrushPoint;
import io.github.nahkd123.axiomstylus.brush.BrushStroke;
import io.github.nahkd123.axiomstylus.palette.Palette;
import io.github.nahkd123.axiomstylus.preset.BrushPresetEditor;
import io.github.nahkd123.axiomstylus.preset.SavedBrushPreset;
import io.github.nahkd123.axiomstylus.preset.TipShape;
import io.github.nahkd123.axiomstylus.utils.AsImGui;
import io.github.nahkd123.axiomstylus.utils.Box3d;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class PresetBrushTool extends CustomStylusTool {
	private Path presetDir;

	private ImBoolean settingsBrowserToggle = new ImBoolean(false);
	private ImBoolean presetEditorToggle = new ImBoolean(false);
	private ImBoolean exportPresetToggle = new ImBoolean(false);
	private BrushPresetEditor presetEditor = new BrushPresetEditor();
	private ImInt savedPresetIndex = new ImInt(0);
	private List<SavedBrushPreset> savedPresets = new ArrayList<>();
	private String[] savedPresetLabels = new String[0];
	private String[] noSavedPresets = { "<No saved presets>" };

	private BlockRegion commit = null;
	private BooleanRegion removals = AxiomStylusAddon.REGION.createBoolean();
	private BrushPoint prevPoint;

	public PresetBrushTool(Path presetDir) {
		this.presetDir = presetDir;
		refreshPresets();
	}

	@Override
	public String name() {
		return "Preset Brush (Stylus Integration)";
	}

	@Override
	public void reset() {
		super.reset();
		commit = AxiomStylusAddon.REGION.createBlock();
		removals.clear();
		prevPoint = null;
	}

	@Override
	public void render(Camera camera, float tickDelta, long time, MatrixStack poseStack, Matrix4f projection) {
		super.render(camera, tickDelta, time, poseStack, projection);
		if (commit != null) commit.render(camera, Vec3d.ZERO, poseStack, projection, 0.5f, 1f);
		removals.render(camera, Vec3d.ZERO, poseStack, projection, time, Effects.REMOVAL);
	}

	@Override
	protected void onBrushBegin(BrushStroke stroke) {
		reset();
	}

	@Override
	protected void onBrushInput(BrushStroke stroke, BrushPoint prev, BrushPoint next) {
		presetEditor.spacing().interpolate(prevPoint, next, generated -> onDabInput(stroke, generated));
	}

	private void onDabInput(BrushStroke stroke, BrushPoint point) {
		Palette palette = presetEditor.palette();
		Matrix4f mat4 = new Matrix4f();
		presetEditor.shapeDynamics().forEach(d -> {
			float value = d.source().calculate(stroke, prevPoint, point);
			System.out.println("%f -> %f".formatted(value, d.function().apply(value)));
			d.destination().addValue(mat4, d.function().apply(value));
		});
		prevPoint = point;

		Matrix4f mat4Inv = mat4.invert(new Matrix4f());
		TipShape shape = presetEditor.shape();
		Box3d bb = shape.getBoundingBox().tranformVerticesAndEnclose(new Matrix4d(mat4), 1d);
		Vector3f originInTip = mat4Inv.transformProject(point.position().toVector3f());

		for (int y = -1; y <= bb.sizeY() + 0.5; y++) {
			for (int z = -1; z <= bb.sizeZ() + 0.5; z++) {
				for (int x = -1; x <= bb.sizeX() + 0.5; x++) {
					double lx = bb.x() + x;
					double ly = bb.y() + y;
					double lz = bb.z() + z;
					Vector4f localInTip = mat4Inv.transform(
						(float) lx, (float) ly, (float) lz, 1f,
						new Vector4f());
					int wx = (int) (point.position().x + lx);
					int wy = (int) (point.position().y + ly);
					int wz = (int) (point.position().z + lz);
					if (!shape.test(
						originInTip.x, originInTip.y, originInTip.z,
						localInTip.x, localInTip.y, localInTip.z)) continue;
					BlockState block = palette.get(0); // TODO select from palette function
					commit.addBlockIfNotPresent(wx, wy, wz, block);
					if (block.isAir()) removals.add(wx, wy, wz);
					// TODO support brush operation - add and subtract
				}
			}
		}
	}

	@Override
	protected void onBrushFinish(BrushStroke stroke) {
		AxiomStylusAddon.TOOL_SERVICE.pushBlockRegionChange(commit);
		reset();
	}

	@Override
	public void displayImguiOptions() {
		AsImGui.separatorText("Presets");
		ImGui.beginDisabled(savedPresets.size() == 0);
		ImGui.setNextItemWidth(ImGui.getWindowWidth() - 20f);
		if (ImGui.combo("##Preset", savedPresetIndex, savedPresets.size() > 0 ? savedPresetLabels : noSavedPresets))
			selectSavedPreset(savedPresetIndex.get());
		if (ImGui.button("- Remove")) removeSavedPreset(savedPresetIndex.get());
		ImGui.endDisabled();
		ImGui.sameLine();
		if (ImGui.button("+ Add")) addSavedPreset(SavedBrushPreset.savedPresetOf(presetEditor));
		ImGui.sameLine();
		if (ImGui.button("Refresh")) refreshPresets();

		AsImGui.separatorText("Quick settings");
		ImGui.text("(Not yet available)");
		ImGui.checkbox("Browse settings", settingsBrowserToggle);

		AsImGui.separatorText("Advanced");
		ImGui.checkbox("Preset editor", presetEditorToggle);

		if (presetEditorToggle.get()) {
			ImGui.setNextWindowPos(
				ImGui.getWindowPosX() + ImGui.getWindowWidth(),
				ImGui.getWindowPosY(),
				ImGuiCond.FirstUseEver, 0f, 0f);
			ImGui.setNextWindowSize(400f, 800f, ImGuiCond.FirstUseEver);
			if (ImGui.begin("Stylus - Brush Preset", presetEditorToggle)) presetEditor.renderImGui();
			ImGui.end();
		}

		if (exportPresetToggle.get()) {
			if (ImGui.begin("Stylus - Export Brush Preset", exportPresetToggle)) {
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
			}
			ImGui.end();
		}
	}

	public void addSavedPreset(SavedBrushPreset preset) {
		savedPresets.add(preset);
		String[] oldLabels = savedPresetLabels;
		savedPresetLabels = new String[oldLabels.length + 1];
		System.arraycopy(oldLabels, 0, savedPresetLabels, 0, oldLabels.length);
		savedPresetLabels[oldLabels.length] = preset.name();
		savedPresetIndex.set(oldLabels.length);

		try {
			Path brushFile = presetDir.resolve(preset.name() + ".json");
			var result = SavedBrushPreset.CODEC.codec().encodeStart(JsonOps.INSTANCE, preset);
			var gson = new GsonBuilder().setFormattingStyle(FormattingStyle.PRETTY).create();
			var json = gson.toJson(result.resultOrPartial()
				.orElseThrow(() -> new IOException(result.error().get().message())));
			Files.writeString(brushFile, json, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeSavedPreset(int index) {
		SavedBrushPreset preset = savedPresets.get(index);
		savedPresets.remove(index);
		String[] oldLabels = savedPresetLabels;

		if (oldLabels.length == 1) {
			clearSavedPresets();
			return;
		}

		savedPresetLabels = new String[oldLabels.length - 1];
		System.arraycopy(oldLabels, 0, savedPresetLabels, 0, index);
		System.arraycopy(oldLabels, index + 1, savedPresetLabels, index, oldLabels.length - index - 1);
		if (savedPresetIndex.get() == index) selectSavedPreset(0);

		try {
			Path brushFile = presetDir.resolve(preset.name() + ".json");
			Files.deleteIfExists(brushFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void clearSavedPresets() {
		savedPresets.clear();
		savedPresetLabels = new String[0];
		savedPresetIndex.set(0);

		try {
			List<Path> list = Files.list(presetDir).toList();
			for (Path child : list) Files.delete(child);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void selectSavedPreset(int index) {
		presetEditor.loadPreset(savedPresets.get(index));
		savedPresetIndex.set(index);
	}

	public void refreshPresets() {
		savedPresets.clear();
		savedPresetIndex.set(0);

		try {
			if (Files.notExists(presetDir)) Files.createDirectories(presetDir);
			else {
				for (Path child : Files.list(presetDir).toList()) {
					String json = Files.readString(child, StandardCharsets.UTF_8);
					var result = SavedBrushPreset.CODEC.codec().decode(JsonOps.INSTANCE, JsonParser.parseString(json));
					var resultOrPartial = result.resultOrPartial();
					if (resultOrPartial.isEmpty()) AxiomStylusAddon.LOGGER.warn("Failed to load {} preset", child);
					SavedBrushPreset preset = resultOrPartial.get().getFirst();
					savedPresets.add(preset);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		savedPresetLabels = savedPresets.stream().map(SavedBrushPreset::name).toArray(String[]::new);
		if (savedPresets.size() > 0) selectSavedPreset(0);
	}
}
