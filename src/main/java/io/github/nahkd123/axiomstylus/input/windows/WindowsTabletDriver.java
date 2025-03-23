package io.github.nahkd123.axiomstylus.input.windows;

import java.util.Set;

import org.lwjgl.glfw.GLFWNativeWin32;

import io.github.nahkd123.axiomstylus.input.TabletDriver;
import io.github.nahkd123.axiomstylus.input.TabletDriverContext;
import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.itf.realtimestylus.IRealTimeStylus;
import io.github.nahkd123.com4j.itf.realtimestylus.IStylusAsyncPlugin;
import io.github.nahkd123.com4j.itf.realtimestylus.RealTimeStylus;
import io.github.nahkd123.com4j.types.realtimestylus.PacketField;
import net.minecraft.text.Text;

public class WindowsTabletDriver implements TabletDriver {
	private boolean initialized = false;
	private ComFactory com = null;
	private IRealTimeStylus rts = null;

	@Override
	public Text getName() { return Text.translatable("axiomstylus.driver.windows"); }

	@Override
	public boolean isSupported() { return System.getProperty("os.name").startsWith("Windows "); }

	@Override
	public void initialize(TabletDriverContext context) {
		if (initialized) return;
		initialized = true;
		com = ComFactory.instance();
		rts = com.createFromClsid(IRealTimeStylus.class, RealTimeStylus.CLSID);
		rts.setHwnd(GLFWNativeWin32.glfwGetWin32Window(context.getGlfwHandle()));
		rts.setDesiredFields(Set.of(
			PacketField.X,
			PacketField.Y,
			PacketField.PACKET_STATUS,
			PacketField.NORMAL_PRESSURE,
			PacketField.X_TILT_ORIENTATION,
			PacketField.Y_TILT_ORIENTATION));
		rts.SetAllTabletsMode(0);
		IStylusAsyncPlugin plugin = com.createJava(
			IStylusAsyncPlugin.class,
			(a, b) -> new StylusPluginImpl(a, b, context));
		rts.addAsyncPlugin(0, plugin);
		plugin.Release();
		rts.setEnable(true);
	}

	@Override
	public void close() {
		if (!initialized) return;
		initialized = false;
		rts.setEnable(false);
		rts.removeAllAsyncPlugins();
		rts.Release();
		rts = null;
	}
}
