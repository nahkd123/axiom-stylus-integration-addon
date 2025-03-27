package io.github.nahkd123.axiomstylus.preset.dynamic;

import org.joml.Matrix4f;

import com.mojang.serialization.Codec;

public enum Matrix4fDynamicTarget implements DynamicTarget<Matrix4f> {
	SCALE("Scale", "Uniform scale along all axes") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.scale((float) value);
		}
	},
	SCALE_X("Scale X", "Scale along X axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.scale((float) value, 1f, 1f);
		}
	},
	SCALE_Y("Scale Y", "Scale along Y axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.scale(1f, (float) value, 1f);
		}
	},
	SCALE_Z("Scale Z", "Scale along Z axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.scale(1f, 1f, (float) value);
		}
	},
	ROTATE_X("Rotate X", "Rotate along X axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.rotateX((float) (value * Math.PI / 180d));
		}
	},
	ROTATE_Y("Rotate Y", "Rotate along Y axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			System.out.println(value);
			target.rotateY((float) (value * Math.PI / 180d));
		}
	},
	ROTATE_Z("Rotate Z", "Rotate along Z axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.rotateZ((float) (value * Math.PI / 180d));
		}
	};

	public static final Codec<Matrix4fDynamicTarget> CODEC = Codec.stringResolver(
		Matrix4fDynamicTarget::toString,
		Matrix4fDynamicTarget::valueOf);

	private String name;
	private String description;

	private Matrix4fDynamicTarget(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String getName() { return name; }

	@Override
	public String getDescription() { return description; }
}