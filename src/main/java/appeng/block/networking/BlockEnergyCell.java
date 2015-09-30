/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.block.networking;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseItemBlockChargeable;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockEnergyCube;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.networking.TileEnergyCell;
import appeng.util.Platform;


public class BlockEnergyCell extends AEBaseTileBlock
{

	public static final PropertyInteger ENERGY_STORAGE = PropertyInteger.create( "fullness", 0, 8 );

	@Override
	public int getMetaFromState(
			final IBlockState state )
	{
		return (int)state.getValue( ENERGY_STORAGE );
	}
	
	@Override
	public IBlockState getStateFromMeta( final int meta )
	{
		return getDefaultState().withProperty( ENERGY_STORAGE, Math.min( 7,  Math.max( 0, meta ) ) );
	}
	
	public BlockEnergyCell()
	{
		super( AEGlassMaterial.INSTANCE );

		this.setTileEntity( TileEnergyCell.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockEnergyCube.class;
	}

	@Override
	public appeng.client.texture.IAESprite getIcon( final net.minecraft.util.EnumFacing side, final IBlockState state)
	{
		switch( (int)state.getValue( ENERGY_STORAGE ) )
		{
			default:
			case 0:
				return ExtraBlockTextures.MEEnergyCell0.getIcon();
			case 1:
				return ExtraBlockTextures.MEEnergyCell1.getIcon();
			case 2:
				return ExtraBlockTextures.MEEnergyCell2.getIcon();
			case 3:
				return ExtraBlockTextures.MEEnergyCell3.getIcon();
			case 4:
				return ExtraBlockTextures.MEEnergyCell4.getIcon();
			case 5:
				return ExtraBlockTextures.MEEnergyCell5.getIcon();
			case 6:
				return ExtraBlockTextures.MEEnergyCell6.getIcon();
			case 7:
				return ExtraBlockTextures.MEEnergyCell7.getIcon();
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		super.getCheckedSubBlocks( item, tabs, itemStacks );

		final ItemStack charged = new ItemStack( this, 1 );
		final NBTTagCompound tag = Platform.openNbtData( charged );
		tag.setDouble( "internalCurrentPower", this.getMaxPower() );
		tag.setDouble( "internalMaxPower", this.getMaxPower() );

		itemStacks.add( charged );
	}

	public double getMaxPower()
	{
		return 200000.0;
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[]{ENERGY_STORAGE};
	}

	@Override
	public Class<? extends AEBaseItemBlock> getItemBlockClass()
	{
		return AEBaseItemBlockChargeable.class;
	}
}
