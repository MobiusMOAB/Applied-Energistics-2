package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import appeng.util.Platform;


public class StandardWorld implements IMeteoriteWorld
{

	protected final World w;

	public StandardWorld( final World w )
	{
		this.w = w;
	}

	@Override
	public int minX( final int in )
	{
		return in;
	}

	@Override
	public int minZ( final int in )
	{
		return in;
	}

	@Override
	public int maxX( final int in )
	{
		return in;
	}

	@Override
	public int maxZ( final int in )
	{
		return in;
	}

	@Override
	public boolean hasNoSky()
	{
		return !this.w.provider.getHasNoSky();
	}

	@Override
	public Block getBlock( final int x, final int y, final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getBlockState( new BlockPos( x, y, z ) ).getBlock();
		}
		return Platform.AIR_BLOCK;
	}

	@Override
	public boolean canBlockSeeTheSky( final int x, final int y, final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.canBlockSeeSky( new BlockPos(x,y,z) );
		}
		return false;
	}

	@Override
	public TileEntity getTileEntity( final int x, final int y, final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getTileEntity( new BlockPos( x, y, z)  );
		}
		return null;
	}

	@Override
	public World getWorld()
	{
		return this.w;
	}

	@Override
	public void setBlock( final int x, final int y, final int z, final Block blk )
	{
		if( this.range( x, y, z ) )
		{
			this.w.setBlockState( new BlockPos( x, y, z ), blk.getDefaultState() );
		}
	}

	@Override
	public void done()
	{

	}

	public boolean range( final int x, final int y, final int z )
	{
		return true;
	}

	@Override
	public void setBlock(
			final int x,
			final int y,
			final int z,
			final IBlockState state,
			final int l )
	{
		if( this.range( x, y, z ) )
		{
			this.w.setBlockState( new BlockPos( x, y, z ), state, l );
		}
	}

	@Override
	public IBlockState getBlockState(
			final int x,
			final int y,
			final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getBlockState( new BlockPos(x,y,z) );
		}
		return Blocks.air.getDefaultState();
	}
}
