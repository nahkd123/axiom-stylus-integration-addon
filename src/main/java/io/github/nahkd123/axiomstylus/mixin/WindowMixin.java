package io.github.nahkd123.axiomstylus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.nahkd123.axiomstylus.bridge.WindowBridge;
import io.github.nahkd123.axiomstylus.input.TabletManager;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;

@Mixin(Window.class)
public abstract class WindowMixin implements WindowBridge {
	@Shadow
	private long handle;

	@Unique
	private TabletManager tabletManager = null;

	@Override
	public TabletManager getTabletManager() { return tabletManager; }

	@Inject(method = "<init>", at = @At("TAIL"))
	private void axiomstylus$init(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, String fullscreenVideoMode, String title, CallbackInfo ci) {
		tabletManager = TabletManager.createTabletManager(handle);
	}

	@Inject(method = "close", at = @At("HEAD"))
	private void axiomstylus$close(CallbackInfo ci) {
		tabletManager.close();
		tabletManager = null;
	}
}
