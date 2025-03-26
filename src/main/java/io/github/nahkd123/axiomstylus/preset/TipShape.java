package io.github.nahkd123.axiomstylus.preset;

import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import io.github.nahkd123.axiomstylus.utils.Box3d;

/**
 * <p>
 * The shape of brush tip.
 * </p>
 */
public interface TipShape {
	/**
	 * <p>
	 * Get the bounding box of this tip shape, assuming the origin of the tip is at
	 * {@code (0; 0; 0)}.
	 * </p>
	 */
	Box3d getBoundingBox();

	/**
	 * <p>
	 * Test whether the tip shape occupies a certain position. Origin position is
	 * absolute (in world space), while local position is relative (in brush tip's
	 * space). Origin and local are for static textures, such as brick or noise.
	 * </p>
	 * 
	 * @param ox The origin X position of brush tip in world space.
	 * @param oy The origin X position of brush tip in world space.
	 * @param oz The origin X position of brush tip in world space.
	 * @param lx The local X position in shape volume.
	 * @param ly The local Y position in shape volume.
	 * @param lz The local Z position in shape volume.
	 * @return Whether this tip shape occupies the specific coordinates.
	 */
	boolean test(double ox, double oy, double oz, double lx, double ly, double lz);

	static Sphere sphere(double radiusX, double radiusY, double radiusZ) {
		return new Sphere(radiusX, radiusY, radiusZ);
	}

	static Sphere sphere(double radius) {
		return new Sphere(radius, radius, radius);
	}

	static Codec<TipShape> CODEC = Codec.STRING.dispatch(
		"shape",
		shape -> switch (shape) {
		case Sphere _ -> "sphere";
		default -> throw new IllegalArgumentException("Unexpected value: " + shape);
		},
		key -> switch (key) {
		case "sphere" -> Sphere.CODEC.fieldOf("radius");
		default -> throw new IllegalArgumentException("Unexpected value: " + key);
		});

	record Sphere(double radiusX, double radiusY, double radiusZ) implements TipShape {
		private List<Double> asCodecList() {
			return radiusX == radiusY && radiusX == radiusZ
				? List.of(radiusX)
				: List.of(radiusX, radiusY, radiusZ);
		}

		public static final Codec<Sphere> CODEC = Codec.either(
			Codec.DOUBLE.xmap(r -> new Sphere(r, r, r), s -> s.radiusX),
			Codec.DOUBLE.listOf(1, 3).xmap(
				list -> list.size() == 1
					? new Sphere(list.get(0), list.get(0), list.get(0))
					: new Sphere(list.get(0), list.get(1), list.get(2)),
				Sphere::asCodecList))
			.xmap(
				either -> either.left().or(either::right).get(),
				sphere -> sphere.radiusX == sphere.radiusY && sphere.radiusX == sphere.radiusZ
					? Either.left(sphere)
					: Either.right(sphere));

		@Override
		public Box3d getBoundingBox() { return Box3d.radius(radiusX, radiusY, radiusZ); }

		@Override
		public boolean test(double ox, double oy, double oz, double lx, double ly, double lz) {
			double sx = lx / radiusX, sy = ly / radiusY, sz = lz / radiusZ;
			return sx * sx + sy * sy + sz * sz < 1d;
		}
	}
}
