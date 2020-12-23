package net.patchworkmc.mixin.extensions.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.patchworkmc.api.block.PatchworkStairsBlockSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(StairsBlock.class)
public class MixinStairsBlock implements PatchworkStairsBlockSupplier {
	// Forge has this as a final field, but if it's final it cannot be in the mixin
	private Supplier<BlockState> stateSupplier = Blocks.AIR::getDefaultState;

	@Override
	public void patchwork$setStateSupplier(Supplier<BlockState> supplier) {
		this.stateSupplier = supplier;
	}

	@Override
	public Supplier<BlockState> patchwork$getStateSupplier() {
		return this.stateSupplier;
	}

	private BlockState patchwork$getModelState() {
		return stateSupplier.get();
	}

	private Block patchwork$getModelBlock() {
		return patchwork$getModelState().getBlock();
	}


	@Redirect(method = "onBlockBreakStart", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlockState:Lnet/minecraft/block/BlockState;"))
	private BlockState redirectOnBlockBreakStart(StairsBlock self) {
		return patchwork$getModelState();
	}

	@Redirect(method = "onBroken", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectOnBroken(StairsBlock self) {
		return patchwork$getModelBlock();
	}

	@Redirect(method = "getBlastResistance", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectBlastResistance(StairsBlock self) {
		return patchwork$getModelBlock();
	}

	@Redirect(method = "getRenderLayer", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectGetRenderLayer(StairsBlock self) {
		return patchwork$getModelBlock();
	}

	@Redirect(method = "onBlockAdded", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectOnBlockAddedBlock(StairsBlock self) {
		return patchwork$getModelBlock();
	}

	@Redirect(method = "onBlockAdded", at = @At(value = "FIELD", target ="Lnet/minecraft/block/StairsBlock;baseBlockState:Lnet/minecraft/block/BlockState;"))
	private BlockState redirectOnBlockAddedState(StairsBlock self) {
		return patchwork$getModelState();
	}

	@Redirect(method = "onBlockRemoved", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlockState:Lnet/minecraft/block/BlockState;"))
	private BlockState redirectOnBlockRemoved(StairsBlock self) {
		return patchwork$getModelState();
	}

	@Redirect(method = "onSteppedOn", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectOnSteppedOn(StairsBlock self) {
		return patchwork$getModelBlock();
	}

	@Redirect(method = "onScheduledTick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectOnScheduledTick(StairsBlock self) {
		return patchwork$getModelBlock();
	}

	@Redirect(method = "activate", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlockState:Lnet/minecraft/block/BlockState;"))
	private BlockState redirectActivate(StairsBlock self) {
		return patchwork$getModelState();
	}

	@Redirect(method = "onDestroyedByExplosion", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectOnDestroyedByExplosion(StairsBlock self) {
		return patchwork$getModelBlock();
	}

}
