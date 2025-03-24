package io.github.nahkd123.axiomstylus.tool;

import java.util.function.BiConsumer;

import org.joml.Matrix4f;

import com.moulberry.axiomclientapi.CustomTool;
import com.moulberry.axiomclientapi.regions.BlockRegion;
import com.moulberry.axiomclientapi.service.ToolService;

import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;
import io.github.nahkd123.axiomstylus.AxiomStylusAddon;
import io.github.nahkd123.axiomstylus.input.InputReport;
import io.github.nahkd123.axiomstylus.input.TabletManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

/**
 * <p>
 * A basic freehand drawing tool with pressure sensitivity.
 * </p>
 */
public class FreehandTool implements CustomTool {
	private float[] startRadius = { 0f };
	private float[] endRadius = { 5f };
	private float[] spacing = { 1f };
	private float[] pressureExponent = { 1f, -1f };
	private float[] pressureCurvePlot = new float[128];

	private boolean drawing = false;
	private Vec3d lastTipPos = null;
	private InputReport lastInput = null;
	private BlockRegion region = AxiomStylusAddon.REGION.createBlock();

	@Override
	public String name() {
		return "Freehand Draw (Stylus Integration)";
	}

	@Override
	public void reset() {
		drawing = false;
		region.clear();
		lastTipPos = null;
		lastInput = null;
	}

	@Override
	public void render(Camera camera, float tickDelta, long time, MatrixStack poseStack, Matrix4f projection) {
		ToolService toolService = AxiomStylusAddon.TOOL_SERVICE;
		InputReport input = TabletManager.get(MinecraftClient.getInstance()).getLastReport();
		if (input == null) return;
		boolean penDown = input.pressure() > 0f;

		if (drawing ^ penDown) {
			if (penDown) {
				reset();
				drawing = true;
			} else {
				toolService.pushBlockRegionChange(region);
				reset();
			}
		} else {
			BlockHitResult raycast = toolService.raycastBlock();
			if (raycast == null) return;

			if (drawing) {
				BlockState palette = toolService.getActiveBlock();
				onTipMove(raycast.getPos().add(-0.5), input, spacing[0], (p, i) -> onBrushDab(p, i, palette, region));
				region.render(camera, Vec3d.ZERO, poseStack, projection, 0.5f, 1f);
			} else {
				// TODO brush outline
			}
		}
	}

	private void onTipMove(Vec3d tipPos, InputReport input, float spacing, BiConsumer<Vec3d, InputReport> interpolator) {
		if (lastTipPos == null || lastInput == null) {
			lastTipPos = tipPos;
			lastInput = input;
			interpolator.accept(tipPos, input);
		} else {
			Vec3d direction = tipPos.subtract(lastTipPos).normalize();
			double distance = tipPos.distanceTo(lastTipPos);

			for (double p = 0d; p < distance; p += spacing) {
				Vec3d interpolatedPos = lastTipPos.add(direction.multiply(p));
				InputReport interpolatedInput = InputReport.lerp(lastInput, input, (float) (p / distance));
				interpolator.accept(interpolatedPos, interpolatedInput);
			}

			lastTipPos = tipPos;
			lastInput = input;
			interpolator.accept(tipPos, input);
		}
	}

	/**
	 * <p>
	 * Called on every brush dab.
	 * </p>
	 * 
	 * @param tipPos  The brush tip's position in 3D space.
	 * @param input   The user's input.
	 * @param palette The palette to use. Limited to 1 block for now.
	 * @param diff    The diff region to commit changes.
	 */
	private void onBrushDab(Vec3d tipPos, InputReport input, BlockState palette, BlockRegion diff) {
		float pressure = applyPressureCurve(input.pressure());
		float radius = endRadius[0] * pressure + startRadius[0] * (1f - pressure);
		int radiusInt = Math.round(radius);
		float radiusSqr = (radius + 0.5f) * (radius + 0.5f);
		int bx = (int) tipPos.x;
		int by = (int) tipPos.y;
		int bz = (int) tipPos.z;

		for (int y = -radiusInt; y <= radiusInt; y++) {
			for (int z = -radiusInt; z <= radiusInt; z++) {
				for (int x = -radiusInt; x <= radiusInt; x++) {
					if (x * x + y * y + z * z >= radiusSqr) continue;
					diff.addBlockIfNotPresent(bx + x, by + y, bz + z, palette);
				}
			}
		}
	}

	private float applyPressureCurve(float input) {
		return (float) Math.pow(input, pressureExponent[0]);
	}

	@Override
	public void displayImguiOptions() {
		ImGui.sliderFloat("Start Radius", startRadius, 0f, 100f);
		ImGui.sliderFloat("End Radius", endRadius, 0f, 100f);
		ImGui.sliderFloat("Spacing", spacing, 0.1f, 10f);
		ImGui.sliderFloat("Pressure Exponent", pressureExponent, 0.1f, 10f, "%f", ImGuiSliderFlags.Logarithmic);

		if (pressureExponent[0] != pressureExponent[1]) {
			pressureExponent[1] = pressureExponent[0];

			for (int i = 0; i < pressureCurvePlot.length; i++) {
				float input = i / (float) (pressureCurvePlot.length - 1);
				pressureCurvePlot[i] = applyPressureCurve(input);
			}
		}

		ImGui.plotLines(
			"Pressure Curve",
			pressureCurvePlot, pressureCurvePlot.length, 0,
			"Exponent = %.02f".formatted(pressureExponent[0]),
			0f, 1f,
			ImGui.calcItemWidth(),
			ImGui.calcItemWidth());
	}
}
