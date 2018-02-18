package flaxbeard.questionablyimmersive.common.util;

import flaxbeard.questionablyimmersive.common.QIContent;
import flaxbeard.questionablyimmersive.common.QISaveData;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import flaxbeard.questionablyimmersive.common.network.QIPacketHandler;
import flaxbeard.questionablyimmersive.common.network.UpdateRadioNetworkPacket;
import javafx.util.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadioHelper
{

	public static class RadioNetwork
	{
		private final int world;
		private final int frequency;

		public RadioNetwork(int world, int frequency)
		{
			this.world = world;
			this.frequency = frequency;
		}

		List<BlockPos> recievers = new ArrayList<>();
		Map<BlockPos, Integer> broadcasters = new HashMap<>();
		int maxPower = 0;
		private boolean portableRadioOn = false;

		private void recalculatePower()
		{
			int lastMaxPower = maxPower;

			if (!portableRadioOn)
			{
				maxPower = 0;
				for (BlockPos pos : broadcasters.keySet())
				{
					maxPower = Math.max(maxPower, broadcasters.get(pos));
				}
			}
			else
			{
				maxPower = 15;
			}


			if (maxPower != lastMaxPower)
			{
				updateReceivers();
			}
		}

		private World getWorld()
		{
			return DimensionManager.getWorld(world);
		}

		private void updateReceivers()
		{
			QIPacketHandler.INSTANCE.sendToAll(new UpdateRadioNetworkPacket(world, frequency, writeToNBT()));
			for (BlockPos pos : recievers)
			{
				TileEntity te = getWorld().getTileEntity(pos);
				if (te instanceof TileEntityRadio)
				{
					TileEntityRadio radio = (TileEntityRadio) te;
					radio.power = maxPower;
					radio.markContainingBlockForUpdate(null);
					getWorld().notifyNeighborsOfStateChange(pos, QIContent.blockGauge, false);
				}
			}
		}

		public void updatePower(BlockPos pos)
		{
			System.out.println(FMLCommonHandler.instance().getEffectiveSide() + " " + world + " " + frequency + " " + recievers.size());
			QISaveData.setDirty(world);
			broadcasters.put(pos, getPowerForBlock(getWorld(), pos));
			recalculatePower();
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("maxPower", maxPower);
			compound.setBoolean("portableRadioOn", portableRadioOn);
			return compound;
		}

		public void readFromNBT(NBTTagCompound compound)
		{
			this.maxPower = compound.getInteger("maxPower");
			this.portableRadioOn = compound.getBoolean("portableRadioOn");
		}

		public void togglePortableRadio()
		{
			System.out.println(FMLCommonHandler.instance().getEffectiveSide() + " " + world + " " + frequency + " " + recievers.size());
			portableRadioOn = !portableRadioOn;
			recalculatePower();
		}
	}

	public static void togglePortableRadio(World world, int frequency)
	{
		RadioNetwork net = getNetwork(world.provider.getDimension(), frequency);
		net.togglePortableRadio();
	}

	public static Map<Pair<Integer, Integer>, RadioNetwork> networks = new HashMap<>();

	public static void addRadio(TileEntityRadio te)
	{
		int dimid = te.getWorld().provider.getDimension();
		RadioNetwork net = getNetwork(dimid, te.frequency);

		if (te.receiveMode)
		{
			net.recievers.add(te.getPos());
			te.power = net.maxPower;
		}
		else
		{
			net.broadcasters.put(te.getPos(), getPowerForBlock(te.getWorld(), te.getPos()));
			net.updatePower(te.getPos());
		}
	}

	private static int getPowerForBlock(World world, BlockPos pos)
	{
		return world.getStrongPower(pos);
	}

	public static void removeRadio(TileEntityRadio te)
	{
		int dimid = te.getWorld().provider.getDimension();
		RadioNetwork net = getNetwork(dimid, te.frequency);
		net.recievers.remove(te.getPos());
		net.broadcasters.remove(te.getPos());
		net.recalculatePower();
	}

	public static RadioNetwork getNetwork(int dimension, int frequency)
	{
		Pair<Integer, Integer> station = new Pair<>(dimension, frequency);
		if (!networks.containsKey(station))
		{
			networks.put(station, new RadioNetwork(dimension, frequency));
		}
		return networks.get(station);
	}

	public static int getPower(int dimension, int frequency)
	{
		RadioNetwork network = getNetwork(dimension, frequency);
		return network.maxPower;
	}
}
