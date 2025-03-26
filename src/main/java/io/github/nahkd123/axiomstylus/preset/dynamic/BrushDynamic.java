package io.github.nahkd123.axiomstylus.preset.dynamic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BrushDynamic<T>(DynamicSource source, DynamicFunction function, DynamicTarget<T> destination) {
	public static <T> Codec<BrushDynamic<T>> createCodec(Codec<DynamicTarget<T>> destinationCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
			DynamicSource.CODEC.fieldOf("source").forGetter(BrushDynamic::source),
			DynamicFunction.CODEC.fieldOf("function").forGetter(BrushDynamic::function),
			destinationCodec.fieldOf("destination").forGetter(BrushDynamic::destination))
			.apply(instance, BrushDynamic::new));
	}

	// TODO make it fully immutable
	public BrushDynamic<T> makeCopy() {
		return new BrushDynamic<>(source, function.makeCopy(), destination);
	}

	@Override
	public final String toString() {
		return "%s -> %s".formatted(source.getName(), destination.getName());
	}
}
