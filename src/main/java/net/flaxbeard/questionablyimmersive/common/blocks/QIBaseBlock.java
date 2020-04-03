//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.common.items.HammerItem;
import blusunrize.immersiveengineering.common.items.WirecutterItem;
import com.google.common.base.Preconditions;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.QIContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QIBaseBlock extends Block
{
	protected static IProperty[] tempProperties;
	public final String name;
	public final IProperty[] additionalProperties;
	boolean isHidden;
	boolean hasFlavour;
	protected List<BlockRenderLayer> renderLayers;
	protected int lightOpacity;
	protected PushReaction mobilityFlag;
	protected boolean canHammerHarvest;
	protected boolean notNormalBlock;

	public QIBaseBlock(String name, Properties blockProps, @Nullable Class<? extends BlockItem> itemBlock, IProperty... additionalProperties)
	{
		super(setTempProperties(blockProps, additionalProperties));
		this.renderLayers = Collections.singletonList(BlockRenderLayer.SOLID);
		this.mobilityFlag = PushReaction.NORMAL;
		this.name = name;
		this.additionalProperties = (IProperty[]) Arrays.copyOf(tempProperties, tempProperties.length);
		this.setDefaultState(this.getInitDefaultState());
		ResourceLocation registryName = this.createRegistryName();
		this.setRegistryName(registryName);
		QIContent.registeredQIBlocks.add(this);
		if (itemBlock != null)
		{
			try
			{
				Item item = (Item) itemBlock.getConstructor(Block.class, net.minecraft.item.Item.Properties.class).newInstance(this, (new net.minecraft.item.Item.Properties()).group(QuestionablyImmersive.itemGroup));
				item.setRegistryName(registryName);
				QIContent.registeredQIItems.add(item);
			} catch (Exception var7)
			{
				throw new RuntimeException(var7);
			}
		}

		this.lightOpacity = 15;
	}

	protected static Properties setTempProperties(Properties blockProps, Object[] additionalProperties)
	{
		List<IProperty> propList = new ArrayList();
		Object[] var3 = additionalProperties;
		int var4 = additionalProperties.length;

		for (int var5 = 0; var5 < var4; ++var5)
		{
			Object o = var3[var5];
			if (o instanceof IProperty)
			{
				propList.add((IProperty) o);
			}

			if (o instanceof IProperty[])
			{
				propList.addAll(Arrays.asList((IProperty[]) ((IProperty[]) o)));
			}
		}

		tempProperties = (IProperty[]) propList.toArray(new IProperty[0]);
		return blockProps.variableOpacity();
	}

	public QIBaseBlock setHidden(boolean shouldHide)
	{
		this.isHidden = shouldHide;
		return this;
	}

	public boolean isHidden()
	{
		return this.isHidden;
	}

	public QIBaseBlock setHasFlavour(boolean shouldHave)
	{
		this.hasFlavour = shouldHave;
		return this;
	}

	public boolean hasFlavour()
	{
		return this.hasFlavour;
	}

	public QIBaseBlock setBlockLayer(BlockRenderLayer... layer)
	{
		Preconditions.checkArgument(layer.length > 0);
		this.renderLayers = Arrays.asList(layer);
		return this;
	}

	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
	{
		return this.renderLayers.contains(layer);
	}

	public QIBaseBlock setLightOpacity(int opacity)
	{
		this.lightOpacity = opacity;
		return this;
	}

	public BlockRenderLayer getRenderLayer()
	{
		return this.notNormalBlock ? BlockRenderLayer.CUTOUT : BlockRenderLayer.SOLID;
	}

	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		if (this.notNormalBlock)
		{
			return 0;
		} else if (state.isOpaqueCube(worldIn, pos))
		{
			return this.lightOpacity;
		} else
		{
			return state.propagatesSkylightDown(worldIn, pos) ? 0 : 1;
		}
	}

	public QIBaseBlock setMobility(PushReaction flag)
	{
		this.mobilityFlag = flag;
		return this;
	}

	public PushReaction getPushReaction(BlockState state)
	{
		return this.mobilityFlag;
	}

	public QIBaseBlock setNotNormalBlock()
	{
		this.notNormalBlock = true;
		return this;
	}

	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader world, BlockPos pos)
	{
		return this.notNormalBlock ? 1.0F : super.getAmbientOcclusionLightValue(state, world, pos);
	}

	public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_)
	{
		return this.notNormalBlock || super.propagatesSkylightDown(p_200123_1_, p_200123_2_, p_200123_3_);
	}

	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return !this.notNormalBlock;
	}

	public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos)
	{
		return !this.notNormalBlock;
	}

	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(tempProperties);
	}

	protected BlockState getInitDefaultState()
	{
		return (BlockState) this.stateContainer.getBaseState();
	}

	protected <V extends Comparable<V>> BlockState applyProperty(BlockState in, IProperty<V> prop, Object val)
	{
		return (BlockState) in.with(prop, (V) val);
	}

	public void onIEBlockPlacedBy(BlockItemUseContext context, BlockState state)
	{
	}

	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		return true;
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}

	@OnlyIn(Dist.CLIENT)
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
		items.add(new ItemStack(this, 1));
	}

	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		if (worldIn.isRemote && eventID == 255)
		{
			worldIn.notifyBlockUpdate(pos, state, state, 3);
			return true;
		} else
		{
			return super.eventReceived(state, worldIn, pos, eventID, eventParam);
		}
	}

	public QIBaseBlock setHammerHarvest()
	{
		this.canHammerHarvest = true;
		return this;
	}

	public boolean allowHammerHarvest(BlockState blockState)
	{
		return this.canHammerHarvest;
	}

	public boolean allowWirecutterHarvest(BlockState blockState)
	{
		return false;
	}

	public boolean isToolEffective(BlockState state, ToolType tool)
	{
		if (this.allowHammerHarvest(state) && tool == HammerItem.HAMMER_TOOL)
		{
			return true;
		} else
		{
			return this.allowWirecutterHarvest(state) && tool == WirecutterItem.CUTTER_TOOL ? true : super.isToolEffective(state, tool);
		}
	}

	public ResourceLocation createRegistryName()
	{
		return new ResourceLocation("questionablyimmersive", this.name);
	}

	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		ItemStack activeStack = player.getHeldItem(hand);
		return activeStack.getToolTypes().contains(HammerItem.HAMMER_TOOL) ? this.hammerUseSide(hit.getFace(), player, world, pos, hit) : super.onBlockActivated(state, world, pos, player, hand, hit);
	}

	public boolean hammerUseSide(Direction side, PlayerEntity player, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		return false;
	}
}
