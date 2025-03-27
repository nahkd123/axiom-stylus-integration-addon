package io.github.nahkd123.axiomstylus.preset.dynamic;

import java.util.List;

import imgui.ImGui;
import imgui.type.ImInt;
import io.github.nahkd123.axiomstylus.preset.PresetConfigurator;
import io.github.nahkd123.axiomstylus.utils.AsImGui;

public abstract class BrushDynamicEditor<T> {
	private DynamicSource source;
	private DynamicFunction function;
	private DynamicTarget<T> target;
	private BrushDynamic<T> dynamic;

	// Configurators
	private PresetConfigurator<DynamicFunction> functionConfig;

	// Imgui
	private DynamicSource[] allSources = DynamicSource.values();
	private String[] sourceLabels;
	private ImInt sourceIndex;

	private List<DynamicTarget<T>> allTargets = getAllTargets();
	private String[] targetLabels;
	private ImInt targetIndex;

	public BrushDynamicEditor(DynamicSource source, DynamicFunction function, DynamicTarget<T> target) {
		this.source = source;
		this.function = function;
		this.target = target;
		this.dynamic = new BrushDynamic<>(source, function, target);

		sourceLabels = new String[allSources.length];
		for (int i = 0; i < allSources.length; i++) sourceLabels[i] = allSources[i].getName();
		sourceIndex = new ImInt(source.ordinal());

		this.functionConfig = DynamicFunction.createAllConfigurator(function);

		targetLabels = new String[allTargets.size()];
		for (int i = 0; i < allTargets.size(); i++) targetLabels[i] = allTargets.get(i).getName();
		targetIndex = new ImInt(allTargets.indexOf(target));
	}

	protected abstract List<DynamicTarget<T>> getAllTargets();

	protected abstract void onDataChanged(BrushDynamic<T> oldData, BrushDynamic<T> newData);

	public void renderImGui() {
		ImGui.pushID("Source");
		AsImGui.separatorText("Source");
		if (ImGui.combo("Type", sourceIndex, sourceLabels)) {
			source = allSources[sourceIndex.get()];
			dataChanged();
		}
		ImGui.popID();

		ImGui.pushID("Function");
		AsImGui.separatorText("Function");
		functionConfig.renderImGui(newFunction -> {
			function = newFunction;
			dataChanged();
		});
		ImGui.popID();

		ImGui.pushID("Target");
		AsImGui.separatorText("Target");
		if (ImGui.combo("Type", targetIndex, targetLabels)) {
			target = allTargets.get(targetIndex.get());
			dataChanged();
		}
		ImGui.popID();
	}

	private void dataChanged() {
		var newDynamic = new BrushDynamic<>(source, function, target);
		onDataChanged(dynamic, newDynamic);
		dynamic = newDynamic;
	}
}
