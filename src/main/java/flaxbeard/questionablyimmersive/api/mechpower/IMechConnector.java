package flaxbeard.questionablyimmersive.api.mechpower;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public interface IMechConnector
{
	boolean doesConnect(@Nullable EnumFacing facing);
}
