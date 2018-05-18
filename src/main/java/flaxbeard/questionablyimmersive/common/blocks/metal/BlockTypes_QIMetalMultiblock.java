package flaxbeard.questionablyimmersive.common.blocks.metal;

import flaxbeard.questionablyimmersive.common.blocks.BlockQIBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_QIMetalMultiblock implements IStringSerializable, BlockQIBase.IBlockEnum
{
    MORTAR(false),
    MORTAR_PARENT(false),
    COKE_OVEN_BATTERY(false),
	COKE_OVEN_BATTERY_PARENT(false),
	COKE_OVEN_BATTERY_RENDERED(false),
	TRIPHAMMER(false),
	TRIPHAMMER_PARENT(false);

	private boolean needsCustomState;
	BlockTypes_QIMetalMultiblock(boolean needsCustomState)
	{
		this.needsCustomState = needsCustomState;
	}

	@Override
	public String getName()
	{
		return this.toString().toLowerCase(Locale.ENGLISH);
	}
	@Override
	public int getMeta()
	{
		return ordinal();
	}
	@Override
	public boolean listForCreative()
	{
		return false;
	}

	public boolean needsCustomState()
	{
		return this.needsCustomState;
	}
	public String getCustomState()
	{
		String[] split = getName().split("_");
		String s = split[0].toLowerCase(Locale.ENGLISH);
		for(int i=1; i<split.length; i++)
			s+=split[i].substring(0,1).toUpperCase(Locale.ENGLISH)+split[i].substring(1).toLowerCase(Locale.ENGLISH);
		return s;
	}
}