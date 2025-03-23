package io.github.nahkd123.axiomstylus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.nahkd123.axiomstylus.input.InputReport;
import io.github.nahkd123.axiomstylus.input.TabletManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Shadow
	private MinecraftClient client;

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V",
			shift = Shift.AFTER),
		locals = LocalCapture.CAPTURE_FAILHARD)
	private void axiomstylus$renderScreen(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, @Local(
		ordinal = 0) DrawContext drawContext) {
		// We only display pointer debugging info while in development environment
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			TabletManager tabletManager = TabletManager.get(client);
			InputReport lastReport = tabletManager.getLastReport();
			if (lastReport == null) return;

			double uiScale = client.getWindow().getScaleFactor();
			drawContext.getMatrices().push();
			drawContext.getMatrices().translate(
				lastReport.x() / uiScale,
				lastReport.y() / uiScale,
				0f);
			drawContext.fill(-1, -1, 1, 1, 0xFFFFFFFF);
			drawContext.getMatrices().scale(lastReport.pressure(), lastReport.pressure(), 1f);
			drawContext.drawBorder(-5, -5, 10, 10, 0xFFFFFFFF);
			drawContext.getMatrices().pop();
		}
	}
}
