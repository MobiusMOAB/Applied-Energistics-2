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

package appeng.client.render.blocks;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.storage.data.IAEItemStack;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.client.ClientHelper;
import appeng.client.render.ModelGenerator;
import appeng.core.AELog;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv1
 */
public class RenderBlockCraftingCPUMonitor extends RenderBlockCraftingCPU<BlockCraftingMonitor, TileCraftingMonitorTile>
{
	private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;

	public RenderBlockCraftingCPUMonitor()
	{
		super( true, 20 );
	}

	@Override
	public void renderTile( final BlockCraftingMonitor block, final TileCraftingMonitorTile tile, final WorldRenderer tess, final double x, final double y, final double z, final float f, final ModelGenerator renderer )
	{
		if( tile != null )
		{
			final IAEItemStack ais = tile.getJobProgress();

			if( tile.dspList == null )
			{
				tile.updateList = true;
				tile.dspList = GLAllocation.generateDisplayLists( 1 );
			}

			if( ais != null )
			{
				GL11.glPushMatrix();
				GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );

				if( tile.updateList )
				{
					tile.updateList = false;
					GL11.glNewList( tile.dspList, GL11.GL_COMPILE_AND_EXECUTE );
					this.tesrRenderScreen( tess, tile, ais );
					GL11.glEndList();
				}
				else
				{
					GL11.glCallList( tile.dspList );
				}

				GL11.glPopMatrix();
			}
		}
	}

	private void tesrRenderScreen( final WorldRenderer tess, final TileCraftingMonitorTile cmt, final IAEItemStack ais )
	{
		final EnumFacing side = cmt.getForward();

		EnumFacing walrus = side.getFrontOffsetY() != 0 ? EnumFacing.SOUTH : EnumFacing.UP;
		int spin = 0;

		int max = 5;
		while( walrus != cmt.getUp() && max > 0 )
		{
			max--;
			spin++;
			walrus = Platform.rotateAround( walrus, side );
		}
		max--;

		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glTranslated( side.getFrontOffsetX() * 0.69, side.getFrontOffsetY() * 0.69, side.getFrontOffsetZ() * 0.69 );

		final float scale = 0.7f;
		GL11.glScalef( scale, scale, scale );

		if( side == EnumFacing.UP )
		{
			GL11.glScalef( 1.0f, -1.0f, 1.0f );
			GL11.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
			GL11.glRotatef( spin * 90.0F, 0, 0, 1 );
		}

		if( side == EnumFacing.DOWN )
		{
			GL11.glScalef( 1.0f, -1.0f, 1.0f );
			GL11.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
			GL11.glRotatef( spin * -90.0F, 0, 0, 1 );
		}

		if( side == EnumFacing.EAST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
		}

		if( side == EnumFacing.WEST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
		}

		if( side == EnumFacing.NORTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
		}

		if( side == EnumFacing.SOUTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
		}

		GL11.glPushMatrix();
		try
		{
			final ItemStack sis = ais.getItemStack();
			sis.stackSize = 1;

			final int br = 16 << 20 | 16 << 4;
			final int var11 = br % 65536;
			final int var12 = br / 65536;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F );

			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

			GL11.glDisable( GL11.GL_LIGHTING );
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );
			// RenderHelper.enableGUIStandardItemLighting();
			tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

			ClientHelper.proxy.doRenderItem( sis, cmt.getWorld() );
		}
		catch( final Exception e )
		{
			AELog.error( e );
		}

		GL11.glPopMatrix();

		GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

		final long stackSize = ais.getStackSize();
		final String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm( stackSize );

		final FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		final int width = fr.getStringWidth( renderedStackSize );
		GL11.glTranslatef( -0.5f * width, 0.0f, -1.0f );
		fr.drawString( renderedStackSize, 0, 0, 0 );

		GL11.glPopAttrib();
	}
}
