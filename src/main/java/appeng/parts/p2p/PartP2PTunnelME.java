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

package appeng.parts.p2p;


import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AELog;
import appeng.core.settings.TickRates;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.Connections;
import appeng.me.cache.helpers.TunnelConnection;
import appeng.me.helpers.AENetworkProxy;


public class PartP2PTunnelME extends PartP2PTunnel<PartP2PTunnelME> implements IGridTickable
{

	public final Connections connection = new Connections( this );
	final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", null, true );

	public PartP2PTunnelME( final ItemStack is )
	{
		super( is );
		this.proxy.setFlags( GridFlags.REQUIRE_CHANNEL, GridFlags.COMPRESSED_CHANNEL );
		this.outerProxy.setFlags( GridFlags.DENSE_CAPACITY, GridFlags.CANNOT_CARRY_COMPRESSED );
	}

	@Override
	public void readFromNBT( final NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.outerProxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT( final NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.outerProxy.writeToNBT( extra );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		super.onTunnelNetworkChange();
		if( !this.output )
		{
			try
			{
				this.proxy.getTick().wakeDevice( this.proxy.getNode() );
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.DENSE;
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		this.outerProxy.invalidate();
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.outerProxy.onReady();
	}

	@Override
	public void setPartHostInfo( final AEPartLocation side, final IPartHost host, final TileEntity tile )
	{
		super.setPartHostInfo( side, host, tile );
		this.outerProxy.setValidSides( EnumSet.of( side.getFacing() ) );
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return this.outerProxy.getNode();
	}

	@Override
	public void onPlacement( final EntityPlayer player, final ItemStack held, final AEPartLocation side )
	{
		super.onPlacement( player, held, side );
		this.outerProxy.setOwner( player );
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.METunnel.min, TickRates.METunnel.max, true, false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		// just move on...
		try
		{
			if( !this.proxy.getPath().isNetworkBooting() )
			{
				if( !this.proxy.getEnergy().isNetworkPowered() )
				{
					this.connection.markDestroy();
					TickHandler.INSTANCE.addCallable( this.tile.getWorld(), this.connection );
				}
				else
				{
					if( this.proxy.isActive() )
					{
						this.connection.markCreate();
						TickHandler.INSTANCE.addCallable( this.tile.getWorld(), this.connection );
					}
					else
					{
						this.connection.markDestroy();
						TickHandler.INSTANCE.addCallable( this.tile.getWorld(), this.connection );
					}
				}

				return TickRateModulation.SLEEP;
			}
		}
		catch( final GridAccessException e )
		{
			// meh?
		}

		return TickRateModulation.IDLE;
	}

	public void updateConnections( final Connections connections )
	{
		if( connections.destroy )
		{
			for( final TunnelConnection cw : this.connection.connections.values() )
			{
				cw.c.destroy();
			}

			this.connection.connections.clear();
		}
		else if( connections.create )
		{

			final Iterator<TunnelConnection> i = this.connection.connections.values().iterator();
			while( i.hasNext() )
			{
				final TunnelConnection cw = i.next();
				try
				{
					if( cw.tunnel.proxy.getGrid() != this.proxy.getGrid() )
					{
						cw.c.destroy();
						i.remove();
					}
					else if( !cw.tunnel.proxy.isActive() )
					{
						cw.c.destroy();
						i.remove();
					}
				}
				catch( final GridAccessException e )
				{
					// :P
				}
			}

			final LinkedList<PartP2PTunnelME> newSides = new LinkedList<PartP2PTunnelME>();
			try
			{
				for( final PartP2PTunnelME me : this.getOutputs() )
				{
					if( me.proxy.isActive() && connections.connections.get( me.getGridNode() ) == null )
					{
						newSides.add( me );
					}
				}

				for( final PartP2PTunnelME me : newSides )
				{
					try
					{
						connections.connections.put( me.getGridNode(), new TunnelConnection( me, AEApi.instance().createGridConnection( this.outerProxy.getNode(), me.outerProxy.getNode() ) ) );
					}
					catch( final FailedConnection e )
					{
						final TileEntity start = this.getTile();
						final TileEntity end = me.getTile();
						AELog.warning( "Failed to establish a ME P2P Tunnel between the tunnels at [x=%d, y=%d, z=%d] and [x=%d, y=%d, z=%d]", 
							start.getPos().getX(), start.getPos().getY(), start.getPos().getZ(), 
							end.getPos().getX(), end.getPos().getY(), end.getPos().getZ() );
						// :(
					}
				}
			}
			catch( final GridAccessException e )
			{
				AELog.error( e );
			}
		}
	}
}
