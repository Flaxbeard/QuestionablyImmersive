package flaxbeard.questionablyimmersive.common.blocks.metal;

import flaxbeard.questionablyimmersive.common.blocks.BlockQIBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_QIMetalDevice implements IStringSerializable, BlockQIBase.IBlockEnum
{
	GAUGE,
	RADIO,
	AXLE;

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
		return true;
	}
}