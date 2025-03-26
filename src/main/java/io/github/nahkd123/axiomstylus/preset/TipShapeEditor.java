package io.github.nahkd123.axiomstylus.preset;

import imgui.ImGui;
import imgui.type.ImInt;

public abstract class TipShapeEditor {
	private static final String[] LABELS = { "Cube", "Sphere" };
	private TipShape oldShape;
	private ImInt index = new ImInt();
	private float[] radius = { 1f, 1f, 1f };

	public TipShapeEditor(TipShape shape) {
		setCurrent(shape);
	}

	public void setCurrent(TipShape shape) {
		switch (oldShape = shape) {
		case TipShape.Cube(double rx, double ry, double rz):
			index.set(0);
			radius[0] = (float) rx;
			radius[1] = (float) ry;
			radius[2] = (float) rz;
			break;
		case TipShape.Sphere(double rx, double ry, double rz):
			index.set(1);
			radius[0] = (float) rx;
			radius[1] = (float) ry;
			radius[2] = (float) rz;
			break;
		default:
			throw new IllegalArgumentException("Not implemented: %s".formatted(shape));
		}
	}

	public TipShape getCurrent() {
		return switch (index.get()) {
		case 0 -> TipShape.cube(radius[0], radius[1], radius[2]);
		case 1 -> TipShape.sphere(radius[0], radius[1], radius[2]);
		default -> throw new IllegalArgumentException("Unexpected value: " + index.get());
		};
	}

	protected abstract void onShapeChanged(TipShape oldShape, TipShape newShape);

	public void renderImGui() {
		if (ImGui.combo("Type", index, LABELS)) shapeChanged();

		switch (index.get()) {
		case 0:
		case 1:
			if (ImGui.sliderFloat3("Radius", radius, 0f, 10f)) shapeChanged();
			break;
		default:
			break;
		}
	}

	private void shapeChanged() {
		TipShape newShape = getCurrent();
		onShapeChanged(oldShape, newShape);
		oldShape = newShape;
	}
}
