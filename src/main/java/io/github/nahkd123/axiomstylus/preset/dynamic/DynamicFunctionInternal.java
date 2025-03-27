package io.github.nahkd123.axiomstylus.preset.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.MapCodec;

class DynamicFunctionInternal {
	static final Map<String, MapCodec<DynamicFunction>> ID_TO_CODEC = new HashMap<>();
	static final Map<Class<?>, String> CLASS_TO_ID = new HashMap<>();
	static final Map<Class<?>, DynamicFunction> CLASS_TO_DEFAULT = new HashMap<>();
	static final Map<Class<?>, Integer> CLASS_TO_INDICES = new HashMap<>();
	static final List<Class<?>> INDICES_TO_CLASS = new ArrayList<>();
	static String[] imGuiLabels = new String[0];
}
