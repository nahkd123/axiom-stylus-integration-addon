package io.github.nahkd123.axiomstylus.preset;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.nahkd123.axiomstylus.input.InputReport;
import net.minecraft.util.math.Vec3d;

/**
 * <p>
 * Represent brush dab spacing mode.
 * </p>
 */
public interface BrushSpacing {
	void interpolate(@Nullable Vec3d prevTipPos, @Nullable InputReport prevInput, Vec3d nextTipPos, InputReport nextInput, BiConsumer<Vec3d, InputReport> consumer);

	/**
	 * <p>
	 * Use position data from input events as-is. Kinda like airbrush effect,
	 * terrible for ink-based brushes.
	 * </p>
	 */
	static Exact exact() {
		return Exact.EXACT;
	}

	/**
	 * <p>
	 * Interpolating between previous and next position data for brush dabs.
	 * </p>
	 */
	static Even even(double spacing) {
		return new Even(spacing);
	}

	Codec<BrushSpacing> CODEC = Codec.STRING.dispatch(
		spacingMode -> switch (spacingMode) {
		case Even _ -> "even";
		case Exact _ -> "exact";
		default -> throw new IllegalArgumentException("Unexpected value: " + spacingMode);
		},
		name -> switch (name) {
		case "even" -> Codec.DOUBLE.xmap(Even::new, Even::spacing).fieldOf("spacing");
		case "exact" -> MapCodec.unit(Exact.EXACT);
		default -> throw new IllegalArgumentException("Unexpected value: " + name);
		});

	enum Exact implements BrushSpacing {
		EXACT;

		@Override
		public void interpolate(@Nullable Vec3d prevTipPos, @Nullable InputReport prevInput, Vec3d nextTipPos, InputReport nextInput, BiConsumer<Vec3d, InputReport> consumer) {
			consumer.accept(nextTipPos, nextInput);
		}
	}

	record Even(double spacing) implements BrushSpacing {
		@Override
		public void interpolate(@Nullable Vec3d prevTipPos, @Nullable InputReport prevInput, Vec3d nextTipPos, InputReport nextInput, BiConsumer<Vec3d, InputReport> consumer) {
			if (prevTipPos != null || prevInput != null) {
				double distance = nextTipPos.distanceTo(prevTipPos);

				for (double d = spacing; d <= distance + 1e-6; d += spacing) {
					double progress = d / distance;
					Vec3d lerpTipPos = prevTipPos.lerp(nextTipPos, progress);
					InputReport lerpInput = InputReport.lerp(prevInput, nextInput, (float) progress);
					consumer.accept(lerpTipPos, lerpInput);
				}
			} else {
				consumer.accept(nextTipPos, nextInput);
			}
		}
	}
}
