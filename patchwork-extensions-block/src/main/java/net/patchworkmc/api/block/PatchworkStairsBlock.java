package net.patchworkmc.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;

import java.util.function.Supplier;

public class PatchworkStairsBlock extends StairsBlock {

	public PatchworkStairsBlock(BlockState state, Block.Settings settings) {
		super(state, settings);
		((PatchworkStairsBlockSupplier) this).patchwork$setStateSupplier(() -> state);
	}

	public PatchworkStairsBlock(Supplier<BlockState> supplier, Block.Settings settings) {
		super(Blocks.AIR.getDefaultState(), settings);
		((PatchworkStairsBlockSupplier) this).patchwork$setStateSupplier(supplier);
	}
}
