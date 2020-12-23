package net.patchworkmc.api.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

public interface PatchworkStairsBlockSupplier {
	default Supplier<BlockState> patchwork$getStateSupplier() {
		throw new AssertionError("Mixin not applied");
	}

	default void patchwork$setStateSupplier(Supplier<BlockState> supplier) {
		throw new AssertionError("Mixin not applied");
	}
}
