package io.github.nahkd123.axiomstylus.preset;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.nahkd123.axiomstylus.brush.BrushPoint;
import io.github.nahkd123.axiomstylus.brush.ImmutableBrushPoint;

/**
 * <p>
 * Represent brush point spacing mode.
 * </p>
 */
public interface BrushSpacing {
	void interpolate(@Nullable BrushPoint prev, BrushPoint next, Consumer<BrushPoint> consumer);

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
	 * Evenly interpolating between previous and next position data for brush
	 * points.
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
		public void interpolate(@Nullable BrushPoint prev, BrushPoint next, Consumer<BrushPoint> consumer) {
			consumer.accept(next);
		}
	}

	record Even(double spacing) implements BrushSpacing {
		@Override
		public void interpolate(@Nullable BrushPoint prev, BrushPoint next, Consumer<BrushPoint> consumer) {
			if (prev != null) {
				double distance = prev.position().distanceTo(next.position());

				for (double d = spacing; d <= distance + 1e-6; d += spacing) {
					double progress = d / distance;
					ImmutableBrushPoint intermediate = ImmutableBrushPoint.lerp(prev, next, (float) progress);
					consumer.accept(intermediate);
				}
			} else {
				consumer.accept(next);
			}
		}
	}
}
