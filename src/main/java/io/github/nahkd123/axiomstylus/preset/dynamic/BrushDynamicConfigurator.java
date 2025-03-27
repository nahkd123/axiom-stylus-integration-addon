package io.github.nahkd123.axiomstylus.preset.dynamic;

import java.util.List;
import java.util.function.Consumer;

import imgui.ImGui;
import io.github.nahkd123.axiomstylus.preset.PresetConfigurator;
import io.github.nahkd123.axiomstylus.utils.AsImGui;

public abstract class BrushDynamicConfigurator<T> implements PresetConfigurator<BrushDynamic<T>> {
	private DynamicSource source;
	private DynamicFunction function;
	private DynamicTarget<T> target;

	// Configurators
	private PresetConfigurator<DynamicSource> sourceConfig;
	private PresetConfigurator<DynamicFunction> functionConfig;
	private PresetConfigurator<DynamicTarget<T>> targetConfig;

	public BrushDynamicConfigurator(DynamicSource source, DynamicFunction function, DynamicTarget<T> target) {
		this.source = source;
		this.function = function;
		this.target = target;

		this.sourceConfig = source.createConfigurator("Type");
		this.functionConfig = DynamicFunction.createAllConfigurator(function);
		this.targetConfig = createTargetConfigurator(target);
	}

	protected abstract List<DynamicTarget<T>> getAllTargets();

	protected PresetConfigurator<DynamicTarget<T>> createTargetConfigurator(DynamicTarget<T> target) {
		class ConfiguratorImpl implements PresetConfigurator<DynamicTarget<T>> {
			List<DynamicTarget<T>> targets = getAllTargets();
			int index = getAllTargets().indexOf(target);

			@Override
			public void renderImGui(Consumer<DynamicTarget<T>> applyCallback) {
				if (ImGui.beginCombo("Type", targets.get(index).getName())) {
					for (int i = 0; i < targets.size(); i++) {
						DynamicTarget<T> target = targets.get(i);

						if (ImGui.selectable(target.getName())) {
							index = i;
							applyCallback.accept(target);
						}

						if (ImGui.isItemHovered()) ImGui.setTooltip(target.getDescription());
					}

					ImGui.endCombo();
				}
			}
		}

		return new ConfiguratorImpl();
	}

	@Override
	public void renderImGui(Consumer<BrushDynamic<T>> applyCallback) {
		ImGui.pushID("Source");
		AsImGui.separatorText("Source");
		sourceConfig.renderImGui(newSource -> {
			source = newSource;
			applyCallback.accept(new BrushDynamic<>(source, function, target));
		});
		ImGui.popID();

		ImGui.pushID("Function");
		AsImGui.separatorText("Function");
		functionConfig.renderImGui(newFunction -> {
			function = newFunction;
			applyCallback.accept(new BrushDynamic<>(source, function, target));
		});
		ImGui.popID();

		ImGui.pushID("Target");
		AsImGui.separatorText("Target");
		targetConfig.renderImGui(newTarget -> {
			target = newTarget;
			applyCallback.accept(new BrushDynamic<>(source, function, target));
		});
		ImGui.popID();
	}
}
