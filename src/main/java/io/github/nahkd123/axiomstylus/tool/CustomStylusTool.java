package io.github.nahkd123.axiomstylus.tool;

import org.joml.Matrix4f;

import com.moulberry.axiomclientapi.CustomTool;

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

public abstract class CustomStylusTool implements CustomTool {
	private BrushStrokeImpl stroke = null;
	private ImmutableBrushPoint prevPoint = null;

	@Override
	public void render(Camera camera, float tickDelta, long time, MatrixStack poseStack, Matrix4f projection) {
		// TODO handle mouse input
		InputReport input = TabletManager.get(MinecraftClient.getInstance()).getLastReport();
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
		} else {
			// TODO brush outline?
		}

		wasDrawing = drawing;
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
}
