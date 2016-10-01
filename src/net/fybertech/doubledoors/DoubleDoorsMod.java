package net.fybertech.doubledoors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MainOrOffHand;
import net.minecraft.world.World;

public class DoubleDoorsMod {

	public void init()
	{		
	}
	
	
	public static void onBlockActivatedHook(BlockDoor door, World world, BlockPos pos, IBlockState state, EntityPlayer player, MainOrOffHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (player.isSneaking()) return;
		
		state = world.getBlockState(pos);		
		BlockPos doorBase = state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
		state = world.getBlockState(doorBase);
		
		for (EnumFacing facing : EnumFacing.values()) {
        	if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) continue;
        	
        	BlockPos newPos = doorBase.offset(facing);
        	IBlockState newState = world.getBlockState(newPos);
        	if (newState == null) continue;
        	if (newState.getBlock() != door) continue;
        	if ((boolean)newState.getValue(BlockDoor.OPEN) == (boolean)state.getValue(BlockDoor.OPEN)) continue;
        	if (newState.getValue(BlockDoor.HALF) != state.getValue(BlockDoor.HALF)) continue;

        	//newState.getBlock().onBlockActivated(world, newPos, newState, player, hand, stack, side, hitX, hitY, hitZ);
        	newState = newState.cycleProperty(BlockDoor.OPEN);
        	world.setBlockState(newPos, newState, 2);
            world.markBlockRangeForRenderUpdate(newPos, newPos);
            //world.playAuxSFXAtEntity(player, ((Boolean)newState.getValue(BlockDoor.OPEN)).booleanValue() ? 1003 : 1006, newPos, 0);
            world.playAuxSFXAtEntity(player, ((Boolean)newState.getValue(BlockDoor.OPEN)).booleanValue() ? 1005 : 1011, newPos, 0);
        }
	}
	
	
	public static void onNeighborBlockChangeHook(BlockDoor door, boolean powered, IBlockState state, World world, BlockPos pos, Block block, BlockPos pos2)
	{
		for (EnumFacing facing : EnumFacing.values()) {
			if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) continue;
			
        	BlockPos newPos = pos.offset(facing);
        	IBlockState newState = world.getBlockState(newPos);
        	if (newState == null) continue;
        	if (newState.getBlock() != door) continue;                	
        	//if ((boolean)newState.getValue(OPEN) == (boolean)state.getValue(OPEN)) continue;
        	if (newState.getValue(BlockDoor.HALF) != state.getValue(BlockDoor.HALF)) continue;
        	
        	world.setBlockState(newPos, state.withProperty(BlockDoor.OPEN, Boolean.valueOf(powered)), 2);
            world.markBlockRangeForRenderUpdate(newPos, newPos);
            //world.playAuxSFXAtEntity((EntityPlayer)null, powered ? 1003 : 1006, newPos, 0);
            world.playAuxSFXAtEntity((EntityPlayer)null, powered ? 1005 : 1011, newPos, 0);
        }
	}
	
}
