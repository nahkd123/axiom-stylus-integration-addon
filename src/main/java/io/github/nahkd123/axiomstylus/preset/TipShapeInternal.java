package io.github.nahkd123.axiomstylus.preset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.MapCodec;

public class TipShapeInternal {
	static final Map<String, MapCodec<TipShape>> ID_TO_CODEC = new HashMap<>();
	static final Map<Class<?>, String> CLASS_TO_ID = new HashMap<>();
	static final Map<Class<?>, Integer> CLASS_TO_INDICES = new HashMap<>();
	static final List<TipShape> INDICES_TO_DEFAULT = new ArrayList<>();
	static String[] imGuiLabels = new String[0];
}
