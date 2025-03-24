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
	SCALE_X("Scale X", "The scaling along X axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.scale((float) value, 1f, 1f);
		}
	},
	SCALE_Y("Scale Y", "The scaling along Y axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.scale(1f, (float) value, 1f);
		}
	},
	SCALE_Z("Scale Z", "The scaling along Z axis") {
		@Override
		public void addValue(Matrix4f target, double value) {
			target.scale(1f, 1f, (float) value);
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