package com.techfair.tabletapp;

import com.techfair.tabletapp.R;

/**
 * Constant global values
 *
 * @author Rantanen
 */
public class Globals {
	public static final String LOG_TAG = "tabletapp";
	public static final int PROTOCOL_VERSION = (1 << 16) | (2 << 8) |
											   (3 & 0xFF);
	public static final int CELT_VERSION = 0x8000000b;
}
