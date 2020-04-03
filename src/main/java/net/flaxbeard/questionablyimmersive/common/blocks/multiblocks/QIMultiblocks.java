//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;

public class QIMultiblocks
{
	public static IETemplateMultiblock COKE_OVEN_BATTERY_DISPLAY;
	public static IETemplateMultiblock COKE_OVEN_BATTERY_SLICE;

	public QIMultiblocks()
	{
	}

	public static void init()
	{
		COKE_OVEN_BATTERY_DISPLAY = new CokeOvenBatteryDisplayMultiblock();
		COKE_OVEN_BATTERY_SLICE = new CokeOvenBatterySliceMultiblock();
	}

}
