package net.patchworkmc.mixin.extensions.block;

import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.patchworkmc.api.block.PatchworkStairsBlockSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StairsBlock.class)
public class MixinStairsBlockClient implements PatchworkStairsBlockSupplier {
	@Redirect(method = "randomDisplayTick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/StairsBlock;baseBlock:Lnet/minecraft/block/Block;"))
	private Block redirectRandomDisplayTick(StairsBlock self) {
		return ((PatchworkStairsBlockSupplier) self).patchwork$getStateSupplier().get().getBlock();
	}
}
