package io.github.nahkd123.axiomstylus.preset.dynamic;

public interface DynamicTarget<T> {
	String getName();

	String getDescription();

	void addValue(T target, double value);
}