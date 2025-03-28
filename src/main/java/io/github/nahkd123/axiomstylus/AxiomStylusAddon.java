package io.github.nahkd123.axiomstylus;

import java.nio.file.Path;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.moulberry.axiomclientapi.service.RegionProvider;
import com.moulberry.axiomclientapi.service.ToolPatherProvider;
import com.moulberry.axiomclientapi.service.ToolRegistryService;
import com.moulberry.axiomclientapi.service.ToolService;

import io.github.nahkd123.axiomstylus.preset.TipShape;
import io.github.nahkd123.axiomstylus.preset.dynamic.DynamicFunction;
import io.github.nahkd123.axiomstylus.tool.FreehandTool;
import io.github.nahkd123.axiomstylus.tool.PresetBrushTool;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class AxiomStylusAddon implements ModInitializer {
	public static final String MOD_ID = "axiom-stylus-integration-addon";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegionProvider REGION;
	public static final ToolPatherProvider PATHER;
	public static final ToolService TOOL_SERVICE;
	public static final ToolRegistryService TOOL_REGISTRY;

	static {
		REGION = ServiceLoader.load(RegionProvider.class)
			.findFirst()
			.orElseThrow(() -> new Error("Missing RegionProvider! Is Axiom loaded?"));
		PATHER = ServiceLoader.load(ToolPatherProvider.class)
			.findFirst()
			.orElseThrow(() -> new Error("Missing ToolPatherProvider! Is Axiom loaded?"));
		TOOL_SERVICE = ServiceLoader.load(ToolService.class)
			.findFirst()
			.orElseThrow(() -> new Error("Missing ToolService! Is Axiom loaded?"));
		TOOL_REGISTRY = ServiceLoader.load(ToolRegistryService.class)
			.findFirst()
			.orElseThrow(() -> new Error("Missing ToolRegistryService! Is Axiom loaded?"));
	}

	{
		// Pre-initialize phase
		TipShape.initialize();
		DynamicFunction.initialize();
	}

	@Override
	public void onInitialize() {
		TOOL_REGISTRY.register(new FreehandTool());
		TOOL_REGISTRY.register(new PresetBrushTool(getConfigDir().resolve("presets", "brushes")));
	}

	public Path getConfigDir() { return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID); }
}