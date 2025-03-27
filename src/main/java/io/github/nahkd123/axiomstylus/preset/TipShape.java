package io.github.nahkd123.axiomstylus.preset;

import static io.github.nahkd123.axiomstylus.preset.TipShapeInternal.CLASS_TO_ID;
import static io.github.nahkd123.axiomstylus.preset.TipShapeInternal.CLASS_TO_INDICES;
import static io.github.nahkd123.axiomstylus.preset.TipShapeInternal.ID_TO_CODEC;
import static io.github.nahkd123.axiomstylus.preset.TipShapeInternal.INDICES_TO_DEFAULT;
import static io.github.nahkd123.axiomstylus.preset.TipShapeInternal.imGuiLabels;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import imgui.ImGui;
import io.github.nahkd123.axiomstylus.utils.Box3d;

/**
 * <p>
 * The shape of brush tip.
 * </p>
 */
public interface TipShape {
	String getName();

	String getDescription();

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

	default PresetConfigurator<? extends TipShape> createConfigurator() {
		return PresetConfigurator.empty();
	}

	static PresetConfigurator<TipShape> createAllConfigurator(TipShape function) {
		class ConfiguratorImpl implements PresetConfigurator<TipShape> {
			int typeIndex = CLASS_TO_INDICES.get(function.getClass());
			PresetConfigurator<? extends TipShape> child = function.createConfigurator();

			@Override
			public void renderImGui(Consumer<TipShape> applyCallback) {
				if (ImGui.beginCombo("Type", INDICES_TO_DEFAULT.get(typeIndex).getName())) {
					for (int i = 0; i < INDICES_TO_DEFAULT.size(); i++) {
						TipShape def = INDICES_TO_DEFAULT.get(i);

						if (ImGui.selectable(def.getName())) {
							typeIndex = i;
							child = def.createConfigurator();
							applyCallback.accept(def);
						}

						if (ImGui.isItemHovered()) ImGui.setTooltip(def.getDescription());
					}

					ImGui.endCombo();
				}

				child.renderImGui(c -> applyCallback.accept(c));
			}
		}

		return new ConfiguratorImpl();
	}

	static MapCodec<TipShape> CODEC = Codec.STRING.dispatchMap(
		"shape",
		func -> CLASS_TO_ID.get(func.getClass()),
		ID_TO_CODEC::get);

	@SuppressWarnings("unchecked")
	static <T extends TipShape> void registerMapCodec(String[] keys, MapCodec<T> codec, T def) {
		if (CLASS_TO_ID.putIfAbsent(def.getClass(), keys[0]) != null)
			throw new IllegalArgumentException("%s already registered".formatted(def.getClass()));
		for (String key : keys) ID_TO_CODEC.put(key, codec.xmap(Function.identity(), v -> (T) v));

		String[] oldLabels = imGuiLabels;
		imGuiLabels = new String[imGuiLabels.length + 1];
		System.arraycopy(oldLabels, 0, imGuiLabels, 0, oldLabels.length);
		imGuiLabels[oldLabels.length] = def.getName();
		CLASS_TO_INDICES.put(def.getClass(), oldLabels.length);
		INDICES_TO_DEFAULT.add(def);
	}

	static void initialize() {
		registerMapCodec(new String[] { "cube" }, Cube.CODEC.fieldOf("radius"), new Cube(5, 5, 5));
		registerMapCodec(new String[] { "sphere" }, Sphere.CODEC.fieldOf("radius"), new Sphere(5, 5, 5));
	}

	record Cube(double radiusX, double radiusY, double radiusZ) implements TipShape {
		private List<Double> asCodecList() {
			return radiusX == radiusY && radiusX == radiusZ
				? List.of(radiusX)
				: List.of(radiusX, radiusY, radiusZ);
		}

		public static final Codec<Cube> CODEC = Codec.either(
			Codec.DOUBLE.xmap(r -> new Cube(r, r, r), s -> s.radiusX),
			Codec.DOUBLE.listOf(1, 3).xmap(
				list -> list.size() == 1
					? new Cube(list.get(0), list.get(0), list.get(0))
					: new Cube(list.get(0), list.get(1), list.get(2)),
				Cube::asCodecList))
			.xmap(
				either -> either.left().or(either::right).get(),
				sphere -> sphere.radiusX == sphere.radiusY && sphere.radiusX == sphere.radiusZ
					? Either.left(sphere)
					: Either.right(sphere));

		@Override
		public String getName() { return "Cube"; }

		@Override
		public String getDescription() { return "A solid cube"; }

		@Override
		public Box3d getBoundingBox() { return Box3d.radius(radiusZ, radiusY, radiusX); }

		@Override
		public boolean test(double ox, double oy, double oz, double lx, double ly, double lz) {
			return lx >= -radiusX && lx <= radiusX
				&& ly >= -radiusY && ly <= radiusY
				&& lz >= -radiusZ && lz <= radiusZ;
		}

		@Override
		public PresetConfigurator<? extends TipShape> createConfigurator() {
			float[] xyz = { (float) radiusX, (float) radiusY, (float) radiusZ };
			return applyCallback -> {
				if (ImGui.sliderFloat3("Radius", xyz, 0f, 10f)) {
					Cube cube = new Cube(xyz[0], xyz[1], xyz[2]);
					applyCallback.accept(cube);
				}
			};
		}
	}

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
		public String getName() { return "Sphere"; }

		@Override
		public String getDescription() { return "A solid voxel sphere"; }

		@Override
		public Box3d getBoundingBox() { return Box3d.radius(radiusX, radiusY, radiusZ); }

		@Override
		public boolean test(double ox, double oy, double oz, double lx, double ly, double lz) {
			double sx = lx / radiusX, sy = ly / radiusY, sz = lz / radiusZ;
			return sx * sx + sy * sy + sz * sz < 1d;
		}

		@Override
		public PresetConfigurator<? extends TipShape> createConfigurator() {
			float[] xyz = { (float) radiusX, (float) radiusY, (float) radiusZ };
			return applyCallback -> {
				if (ImGui.sliderFloat3("Radius", xyz, 0f, 10f)) {
					Sphere cube = new Sphere(xyz[0], xyz[1], xyz[2]);
					applyCallback.accept(cube);
				}
			};
		}
	}
}
