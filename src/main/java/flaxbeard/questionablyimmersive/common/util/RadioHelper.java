package flaxbeard.questionablyimmersive.common.util;

import flaxbeard.questionablyimmersive.common.QEContent;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadioHelper
{
	public static class RadioNetwork
	{
		private final World world;

		public RadioNetwork(World world)
		{
			this.world = world;
		}

		List<BlockPos> recievers = new ArrayList<>();
		Map<BlockPos, Integer> broadcasters = new HashMap<>();
		int maxPower = 0;

		public void recalculatePower()
		{
			int lastMaxPower = maxPower;
			maxPower = 0;
			for (BlockPos pos : broadcasters.keySet())
			{
				maxPower = Math.max(maxPower, broadcasters.get(pos));
			}

			if (maxPower != lastMaxPower)
			{
				updateReceivers();
			}
		}

		private void updateReceivers()
		{
			for (BlockPos pos : recievers)
			{
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityRadio)
				{
					TileEntityRadio radio = (TileEntityRadio) te;
					radio.power = maxPower;
					radio.markContainingBlockForUpdate(null);
					world.notifyNeighborsOfStateChange(pos, QEContent.blockGauge, false);
				}
			}
		}

		public void updatePower(BlockPos pos)
		{
			broadcasters.put(pos, getPower(world, pos));
			recalculatePower();
		}
	}

	static Map<Integer, RadioNetwork> radios = new HashMap<>();

	public static RadioNetwork addRadio(TileEntityRadio te)
	{
		int dimid = te.getWorld().provider.getDimension();
		if (!radios.containsKey(dimid))
		{
			radios.put(dimid, new RadioNetwork(te.getWorld()));
		}

		RadioNetwork net = radios.get(dimid);

		if (te.receiveMode)
		{
			net.recievers.add(te.getPos());
			te.power = net.maxPower;
		}
		else
		{
			net.broadcasters.put(te.getPos(), getPower(te.getWorld(), te.getPos()));
			net.updatePower(te.getPos());
		}

		return net;
	}

	public static int getPower(World world, BlockPos pos)
	{
		return world.getStrongPower(pos);
	}

	public static void removeRadio(TileEntityRadio te)
	{
		int dimid = te.getWorld().provider.getDimension();
		if (radios.containsKey(dimid))
		{
			RadioNetwork net = radios.get(dimid);
			net.recievers.remove(te.getPos());
			net.broadcasters.remove(te.getPos());
			net.recalculatePower();
		}
	}
}
