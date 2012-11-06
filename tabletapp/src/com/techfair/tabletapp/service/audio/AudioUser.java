package com.techfair.tabletapp.service.audio;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.techfair.tabletapp.Globals;
import com.techfair.tabletapp.jni.Native;
import com.techfair.tabletapp.service.MumbleProtocol;
import com.techfair.tabletapp.service.PacketDataStream;
import com.techfair.tabletapp.service.model.User;


import android.util.Log;

/**
 * Thread safe buffer for audio data.
 * Implements audio queue and decoding.
 *
 * @author pcgod, Rantanen
 */
public class AudioUser {
	public interface PacketReadyHandler {
		public void packetReady(AudioUser user);
	}

	private final Queue<Native.JitterBufferPacket> normalBuffer;

	private final long celtMode;
	private final long celtDecoder;
	private final Queue<byte[]> dataArrayPool = new ConcurrentLinkedQueue<byte[]>();
	float[] lastFrame = new float[MumbleProtocol.FRAME_SIZE];
	private final User user;

	private int missedFrames = 0;

	public AudioUser(final User user) {
		this.user = user;

		celtMode = Native.celt_mode_create(
			MumbleProtocol.SAMPLE_RATE,
			MumbleProtocol.FRAME_SIZE);
		celtDecoder = Native.celt_decoder_create(celtMode, 1);

		
		normalBuffer = new ConcurrentLinkedQueue<Native.JitterBufferPacket>();

		Log.i(Globals.LOG_TAG, "AudioUser created");
	}

	public boolean addFrameToBuffer(
		final PacketDataStream pds,
		final PacketReadyHandler readyHandler) {

		final int packetHeader = pds.next();

		// Make sure this is supported voice packet.
		//
		// (Yes this check is included in MumbleConnection as well but I believe
		// it should be made here since the decoding support is built into this
		// class anyway. In theory only this class needs to know what packets
		// can be decoded.)
		final int type = (packetHeader >> 5) & 0x7;
		if (type != MumbleProtocol.UDPMESSAGETYPE_UDPVOICECELTALPHA &&
			type != MumbleProtocol.UDPMESSAGETYPE_UDPVOICECELTBETA) {
			return false;
		}

		/* long session = */pds.readLong();
		/*final long sequence =*/ pds.readLong();

		int dataHeader;
		//int frameCount = 0;

		byte[] data = null;
		
		do {
			dataHeader = pds.next();
			final int dataLength = dataHeader & 0x7f;
			if (dataLength > 0) {
				data = acquireDataArray();

				pds.dataBlock(data, dataLength);

				final Native.JitterBufferPacket jbp = new Native.JitterBufferPacket();
				jbp.data = data;
				jbp.len = dataLength;
				
				normalBuffer.add(jbp);

				readyHandler.packetReady(this);
				//frameCount++;

			}
		} while ((dataHeader & 0x80) > 0 && pds.isValid());
		
		return true;
	}

	public void freeDataArray(final byte[] data) {
		dataArrayPool.add(data);
	}

	public User getUser() {
		return this.user;
	}

	/**
	 * Checks if this user has frames and sets lastFrame.
	 *
	 * @return
	 */
	public boolean hasFrame() {
		byte[] data = null;
		int dataLength = 0;

		Native.JitterBufferPacket jbp = normalBuffer.poll();
		if (jbp != null) {
			data = jbp.data;
			dataLength = jbp.len;
			missedFrames = 0;
		} else {
			missedFrames++;
		}

		Native.celt_decode_float(celtDecoder, data, dataLength, lastFrame);

		if (data != null) {
			freeDataArray(data);
		}

		return (missedFrames < 10);
	}

	private byte[] acquireDataArray() {
		byte[] data = dataArrayPool.poll();

		if (data == null) {
			data = new byte[128];
		}

		return data;
	}

	@Override
	protected final void finalize() {
		Native.celt_decoder_destroy(celtDecoder);
		Native.celt_mode_destroy(celtMode);
	}
}
