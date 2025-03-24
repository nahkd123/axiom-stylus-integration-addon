package io.github.nahkd123.axiomstylus.tool;

import org.joml.Matrix4f;

import com.moulberry.axiomclientapi.CustomTool;

import io.github.nahkd123.axiomstylus.AxiomStylusAddon;
import io.github.nahkd123.axiomstylus.input.InputReport;
import io.github.nahkd123.axiomstylus.input.TabletManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public abstract class CustomStylusTool implements CustomTool {
	private boolean wasDrawing = false;

	@Override
	public void render(Camera camera, float tickDelta, long time, MatrixStack poseStack, Matrix4f projection) {
		// TODO handle mouse input
		InputReport input = TabletManager.get(MinecraftClient.getInstance()).getLastReport();
		boolean drawing = input != null && input.tipTouching();
		BlockHitResult raycast = AxiomStylusAddon.TOOL_SERVICE.raycastBlock();
		if (raycast == null) return;

		if (wasDrawing ^ drawing) {
			if (drawing) {
				onBrushBegin();
				onBrushInput(raycast.getPos(), input);
			} else {
				onBrushFinish();
			}
		} else if (drawing) {
			onBrushInput(raycast.getPos(), input);
		} else {
			// TODO brush outline?
		}

		wasDrawing = drawing;
	}

	protected abstract void onBrushBegin();

	/**
	 * <p>
	 * Called on every input received from input device.
	 * </p>
	 * 
	 * @param tipPosition The position of the brush tip in the 3D world.
	 * @param input       The input coming from graphics tablet.
	 */
	protected abstract void onBrushInput(Vec3d tipPosition, InputReport input);

	protected abstract void onBrushFinish();
}
