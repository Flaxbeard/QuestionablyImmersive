package flaxbeard.questionablyimmersive.common;

import flaxbeard.questionablyimmersive.common.util.RadioHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class QISaveData extends WorldSavedData
{
	//	private static HashMap<Integer, IESaveData> INSTANCE = new HashMap<Integer, IESaveData>();
	private static QISaveData INSTANCE;
	public static final String dataName = "QuestionablyImmersive-SaveData";

	public QISaveData(String s)
	{
		super(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList radioList = (NBTTagList) nbt.getTag("radioList");
		for (int i = 0; i < radioList.tagCount(); i++)
		{
			NBTTagCompound tag = radioList.getCompoundTagAt(i);
			int world = tag.getInteger("world");
			int station = tag.getInteger("frequency");


			RadioHelper.RadioNetwork network = RadioHelper.getNetwork(world, station);
			network.readFromNBT(tag.getCompoundTag("info"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList radioList = new NBTTagList();
		for (Tuple<Integer, Integer> station : RadioHelper.networks.keySet())
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("world", station.getFirst());
			tag.setInteger("frequency", station.getSecond());
			tag.setTag("info", RadioHelper.networks.get(station).writeToNBT());
			radioList.appendTag(tag);
		}
		nbt.setTag("radioList", radioList);

		return nbt;
	}


	public static void setDirty(int dimension)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && INSTANCE != null)
			INSTANCE.markDirty();
	}
	
	public static void setInstance(int dimension, QISaveData in)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			INSTANCE = in;
	}

}