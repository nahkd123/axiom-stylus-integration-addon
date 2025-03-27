package io.github.nahkd123.axiomstylus.tool;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import com.moulberry.axiomclientapi.CustomTool;
import com.moulberry.axiomclientapi.service.ToolService;

import io.github.nahkd123.axiomstylus.AxiomStylusAddon;
import io.github.nahkd123.axiomstylus.brush.BrushPoint;
import io.github.nahkd123.axiomstylus.brush.BrushStroke;
import io.github.nahkd123.axiomstylus.brush.ImmutableBrushPoint;
import io.github.nahkd123.axiomstylus.input.InputReport;
import io.github.nahkd123.axiomstylus.input.TabletManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

/**
 * <p>
 * An abstract class for tool implementation that uses graphics tablet as input.
 * </p>
 */
public abstract class CustomStylusTool implements CustomTool {
	private BrushStrokeImpl stroke = null;
	private ImmutableBrushPoint prevPoint = null;

	// Emulate input
	private InputEmulateMode inputEmulateMode = null;

	@Override
	public void render(Camera camera, float tickDelta, long time, MatrixStack poseStack, Matrix4f projection) {
		InputReport input;

		if (inputEmulateMode == InputEmulateMode.MOUSE) {
			ToolService toolService = AxiomStylusAddon.TOOL_SERVICE;
			boolean holdingButton = toolService.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)
				|| toolService.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

			if (holdingButton) {
				input = new InputReport(InputReport.STATUS_TIP_TOUCHING, 0, 0f, 0f, 1f, 0f, 0f);
			} else {
				input = new InputReport(0, 0, 0f, 0f, 0f, 0f, 0f);
				inputEmulateMode = null;
			}
		} else {
			input = TabletManager.get(MinecraftClient.getInstance()).getLastReport();
		}

		// TODO handle input in graphics tablet thread
		boolean wasDrawing = stroke != null;
		boolean drawing = input != null && input.tipTouching();
		BlockHitResult raycast = AxiomStylusAddon.TOOL_SERVICE.raycastBlock();
		if (raycast == null) return;

		if (wasDrawing ^ drawing) {
			if (drawing) {
				stroke = new BrushStrokeImpl(System.nanoTime(), (float) Math.random() * 2 - 1);
				prevPoint = inputToBrushPoint(stroke.timestamp, raycast.getPos(), input);
				onBrushBegin(stroke);
				onBrushInput(stroke, null, prevPoint);
			} else {
				onBrushFinish(stroke);
				stroke = null;
			}
		} else if (drawing) {
			ImmutableBrushPoint next = inputToBrushPoint(System.nanoTime(), raycast.getPos(), input);
			stroke.totalDistance += next.position().distanceTo(prevPoint.position());
			onBrushInput(stroke, prevPoint, next);
			prevPoint = next;
		}
	}

	@Override
	public boolean callUseTool() {
		inputEmulateMode = InputEmulateMode.MOUSE;
		return true;
	}

	private ImmutableBrushPoint inputToBrushPoint(long ts, Vec3d position, InputReport report) {
		float jitter = (float) Math.random();
		return new ImmutableBrushPoint(ts, position, report.pressure(), jitter, report.tiltX(), report.tiltY());
	}

	protected abstract void onBrushBegin(BrushStroke stroke);

	protected abstract void onBrushInput(BrushStroke stroke, BrushPoint previous, BrushPoint current);

	protected abstract void onBrushFinish(BrushStroke stroke);

	private class BrushStrokeImpl implements BrushStroke {
		long timestamp;
		float totalDistance;
		float jitter;

		public BrushStrokeImpl(long timestamp, float jitter) {
			this.timestamp = timestamp;
			this.jitter = jitter;
		}

		@Override
		public float totalDistance() {
			return totalDistance;
		}

		@Override
		public float jitter() {
			return jitter;
		}
	}

	private enum InputEmulateMode {
		MOUSE;
	}
}
