package flaxbeard.questionablyimmersive.api.mechpower;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDynamo;
import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = QuestionablyImmersive.MODID)
public class MechNetworkHelper
{
	private static Map<Integer, Map<BlockPos, MechNetworkData>> networks = new HashMap<>();

	public static MechNetworkData getNetworkData(World world, BlockPos pos)
	{
		return getNetworkData(world, pos, new HashSet<>());
	}

	private static MechNetworkData getNetworkData(World world, BlockPos pos, Set<BlockPos> visited)
	{
		int worldId = world.provider.getDimension();
		if (!networks.containsKey(worldId))
		{
			networks.put(worldId, new HashMap<>());
		}

		Map<BlockPos, MechNetworkData> worldData = networks.get(worldId);
		if (worldData.containsKey(pos))
		{
			MechNetworkData data = worldData.get(pos);
			if (data.isValid())
			{
				return data;
			}
		}

		Tuple<Set<BlockPos>, Set<BlockPos>> connectedConduits = getNetworkDataHelper(world, pos, visited);

		MechNetworkData newData = new MechNetworkData(world, connectedConduits.getFirst(), connectedConduits.getSecond());

		for (BlockPos conduitPos : connectedConduits.getFirst())
		{
			worldData.put(conduitPos, newData);
		}

		return newData;
	}

	public static void removeFromNetwork(World world, BlockPos pos)
	{

		MechNetworkData data = getNetworkData(world, pos);
		data.invalidate();

		TileEntity te = world.getTileEntity(pos);
		if (te == null || !(te instanceof IMechConnector))
		{
			return;
		}

		Set<BlockPos> set = new HashSet<>();
		set.add(pos);

		IMechConnector connector = (IMechConnector) te;
		for (EnumFacing facing : EnumFacing.values())
		{
			if (connector.doesConnect(facing))
			{
				BlockPos movePos = pos.offset(facing);
				getNetworkData(world, movePos, set);
			}
		}
	}

	private static Tuple<Set<BlockPos>, Set<BlockPos>> getNetworkDataHelper(World world, BlockPos pos, Set<BlockPos> visited)
	{
		visited.add(pos);

		TileEntity te = world.getTileEntity(pos);
		if (te == null || !(te instanceof IMechConnector || te instanceof IRotationAcceptor))
		{
			return new Tuple<>(new HashSet<>(), new HashSet<>());
		}
		else if (te instanceof IRotationAcceptor && !(te instanceof IMechConnector))
		{
			Set<BlockPos> toRet = new HashSet<>();
			toRet.add(pos);
			return new Tuple<>(new HashSet<>(), toRet);
		}

		IMechConnector connector = (IMechConnector) te;

		int worldId = world.provider.getDimension();
		Map<BlockPos, MechNetworkData> worldData = networks.get(worldId);
		if (worldData.containsKey(pos))
		{
			MechNetworkData data = worldData.get(pos);
			if (data.isValid())
			{
				return new Tuple<>(data.getContainedBlocks(), data.getContainedAcceptors());
			}
		}

		Set<BlockPos> resultsContained = new HashSet<>();
		Set<BlockPos> resultsAccept = new HashSet<>();

		resultsContained.add(pos);
		for (EnumFacing facing : EnumFacing.values())
		{
			if (connector.doesConnect(facing))
			{
				BlockPos movePos = pos.offset(facing);
				if (!visited.contains(movePos))
				{
					TileEntity moveTe = world.getTileEntity(movePos);
					if (moveTe != null &&
							(((moveTe instanceof IMechConnector && ((IMechConnector) moveTe).doesConnect(facing.getOpposite()))
									|| (moveTe instanceof IRotationAcceptor && !(moveTe instanceof IMechConnector) && canConnect((IRotationAcceptor) moveTe, facing.getOpposite())))))
					{
						Tuple<Set<BlockPos>, Set<BlockPos>> test = getNetworkDataHelper(world, movePos, visited);
						resultsContained.addAll(test.getFirst());
						resultsAccept.addAll(test.getSecond());
					}
				}
			}
		}

		return new Tuple<>(resultsContained, resultsAccept);
	}

	private static boolean canConnect(IRotationAcceptor te, EnumFacing facing)
	{
		if (te instanceof TileEntityDynamo)
		{
			return facing == ((TileEntityDynamo) te).getFacing();
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void handleLogOut(PlayerEvent.PlayerLoggedInEvent event)
	{
		System.out.println("RESET");
		networks = new HashMap<>();
	}
}
