package flaxbeard.questionablyimmersive.common.blocks.metal;

import flaxbeard.questionablyimmersive.common.blocks.BlockQEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_QEMetalDevice implements IStringSerializable, BlockQEBase.IBlockEnum
{
	GAUGE,
	RADIO;

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