package io.github.nahkd123.axiomstylus.input.windows;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.github.nahkd123.axiomstylus.AxiomStylusIntegrationAddon;
import io.github.nahkd123.axiomstylus.input.InputReport;
import io.github.nahkd123.axiomstylus.input.TabletDriverContext;
import io.github.nahkd123.com4j.itf.realtimestylus.IRealTimeStylus;
import io.github.nahkd123.com4j.itf.realtimestylus.IStylusAsyncPlugin;
import io.github.nahkd123.com4j.types.realtimestylus.Packet;
import io.github.nahkd123.com4j.types.realtimestylus.PacketDescription;
import io.github.nahkd123.com4j.types.realtimestylus.PacketField;
import io.github.nahkd123.com4j.types.realtimestylus.PacketProperty;
import io.github.nahkd123.com4j.types.realtimestylus.PacketsIO;
import io.github.nahkd123.com4j.types.realtimestylus.RtsEvent;
import io.github.nahkd123.com4j.types.realtimestylus.StylusInfo;
import io.github.nahkd123.com4j.win32.HResult;

class StylusPluginImpl extends IStylusAsyncPlugin {
	// TODO clone this to IStylusPlugin in COM4J
	public static final int STATUS_TIP = 0x0001;
	public static final int STATUS_INVERT = 0x0002;
	public static final int STATUS_BUTTON_1 = 0x0008;

	private TabletDriverContext context;
	private Map<Integer, PacketDescription> tcidToDesc = new HashMap<>();

	public StylusPluginImpl(MemorySegment comPtr, Runnable destroyCallback, TabletDriverContext context) {
		super(comPtr, destroyCallback);
		this.context = context;
	}

	@Override
	public Collection<RtsEvent> getDataInterest() {
		return Set.of(
			RtsEvent.Packets,
			RtsEvent.InAirPackets,
			RtsEvent.StylusButtonDown,
			RtsEvent.StylusButtonUp,
			RtsEvent.TabletAdded,
			RtsEvent.TabletRemoved);
	}

	@Override
	public HResult TabletRemoved(IRealTimeStylus rts, long index) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment pTcidCount = arena.allocate(ValueLayout.JAVA_INT);
			MemorySegment pTicds = arena.allocate(ValueLayout.ADDRESS);
			rts.GetAllTabletContextIds(pTcidCount, pTicds).throwIfFail();

			int tcidCount = pTcidCount.get(ValueLayout.JAVA_INT, 0L);
			int[] tcids = pTicds
				.reinterpret(ValueLayout.JAVA_INT.scale(0L, tcidCount))
				.toArray(ValueLayout.JAVA_INT);
			int tcid = tcids[(int) index];
			tcidToDesc.remove(tcid);
			return HResult.SUCCEED;
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	@Override
	public void onAirPackets(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
		onPacket0(rts, stylus, io);
	}

	@Override
	public void onPackets(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
		onPacket0(rts, stylus, io);
	}

	@Override
	public void onStylusOutOfRange(IRealTimeStylus rts, int tcid, int sid) {
		context.reportInput(null);
	}

	private void onPacket0(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
		PacketDescription desc = tcidToDesc.computeIfAbsent(stylus.tcid(), rts::getPacketDescription);

		for (int pktIdx = 0; pktIdx < io.getInputCount(); pktIdx++) {
			Packet packet = io.getInput(pktIdx);
			int status = 0, buttons = 0;
			float x = 0f, y = 0f, pressure = -1f, tiltX = 0f, tiltY = 0f;

			for (int i = 0; i < packet.size(); i++) {
				PacketProperty prop = desc.properties().get(i);
				PacketField type = prop.field();
				int value = packet.get(i);

				switch (type) {
				case PACKET_STATUS:
					if ((value & STATUS_TIP) != 0) status |= InputReport.STATUS_TIP_TOUCHING;
					if ((value & STATUS_INVERT) != 0) status |= InputReport.STATUS_INVERT;
					if ((value & STATUS_BUTTON_1) != 0) buttons |= 0b0001;
					break;
				case X:
					x = value * 144f / 2540f; // TODO handle HiDPI
					break;
				case Y:
					y = value * 144f / 2540f; // TODO handle HiDPI
					break;
				case NORMAL_PRESSURE:
					pressure = (float) (value - prop.metrics().logicalMin())
						/ (prop.metrics().logicalMax() - prop.metrics().logicalMin());
					break;
				case X_TILT_ORIENTATION:
					tiltX = value;
					break;
				case Y_TILT_ORIENTATION:
					tiltY = value;
					break;
				default:
					AxiomStylusIntegrationAddon.LOGGER.warn("Unknown field type: {}", type);
					break;
				}
			}

			if (pressure < 0f) pressure = (status & InputReport.STATUS_TIP_TOUCHING) != 0 ? 1f : 0f;
			InputReport report = new InputReport(status, buttons, x, y, pressure, tiltX, tiltY);
			context.reportInput(report);
		}
	}
}
