/*
 * Minecraft Forge, Patchwork Project
 * Copyright (c) 2016-2020, 2019-2020
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.patchworkmc.mixin.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeIngameGui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.patchworkmc.impl.gui.PatchworkIngameGui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Implements events in {@link net.minecraftforge.client.ForgeIngameGui}
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private int scaledHeight;

	@Inject(method = "render", at = @At("HEAD"))
	private void registerEventParent(float tickDelta, CallbackInfo ci) {
		PatchworkIngameGui.eventParent = new RenderGameOverlayEvent(tickDelta, this.client.window);
	}

	// This fires all the Pre- events that are necessary for the status bars
	// The results of these events are handled later
	@Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void fireGuiEvents(CallbackInfo ci, PlayerEntity entity) {
		PatchworkIngameGui.fireGuiEvents(entity);
	}

	/**
	 * This disables the health status bar.
	 *
	 * InGameHud contains the following for loop which renders the health bar:
	 * {@code for(z = MathHelper.ceil((f + (float)p) / 2.0F) - 1; z >= 0; --z)}
	 *
	 * This mixin redirects the MathHelper#ceil call. If the pre event is canceled,
	 * the returned value is 0, which will look like this:
	 * {@code for(z = 0 - 1; z >= 0; --z}
	 *
	 * 0 - 1 is calculated, which leaves {@code z = -1}
	 *
	 * The next condition will fail, as -1 >= 0 is not true,
	 * thus canceling the for loop and causing the health bar to not render
	 */
	@Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I", ordinal = 4))
	private int hookDisableHealthBar(float arg) {
		return ForgeIngameGui.renderHealth && PatchworkIngameGui.preRenderHealthSnapshot.preResult ? 0 : MathHelper.ceil(arg);
	}

	/**
	 * This disables the armor status bar.
	 *
	 * InGameHud contains the following for loop which renders the armor bar:
	 * {@code for(z = 0; z < 10; ++z)}
	 *
	 * This mixin modifies the 0 constant. If the pre event is canceled,
	 * the 0 constant is replaced with 10, so {@code z = 10}.
	 * The next condition, {@code z < 10} will fail as {@code 10 < 10} is not true,
	 * thus canceling the for loop and causing the armor bar to not render
	 */
	@ModifyConstant(method = "renderStatusBars", constant = @Constant(intValue = 0, ordinal = 1))
	private int hookDisableArmor(int originalValue) {
		return ForgeIngameGui.renderArmor && PatchworkIngameGui.preRenderArmorSnapshot.preResult ? 10 : originalValue;
	}

	/**
	 * Properly hooks the left_height field for the armor bar.
	 *
	 * InGameHud renders the armor bar with this method call:
	 * {@code this.blit(aa, s, 34, 9, 9, 9)}
	 *
	 * This modifies the s variable and replaces it with the
	 * proper value for ForgeIngameGui
	 */
	@ModifyVariable(method = "renderStatusBars", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=armor"), ordinal = 9)
	private int hookArmorLeftHeight(int originaValue) {
		return this.scaledHeight - PatchworkIngameGui.preRenderArmorSnapshot.left_height;
	}

	/**
	 * Properly hooks the left_height field for the health bar.
	 *
	 * InGameHud renders the health bar with this method call:
	 * {@code this.blit(ad, ae, aa + 54, 9 * af, 9, 9)}
	 *
	 * This modifies the ae variable and replaces it with the
	 * proper value for ForgeIngameGui
	 */
	@ModifyVariable(method = "renderStatusBars", at = @At(value = "CONSTANT", args = "intValue=4", shift = At.Shift.BEFORE), ordinal = 19)
	private int hookHealthLeftHeight(int originalValue) {
		return this.scaledHeight - PatchworkIngameGui.preRenderHealthSnapshot.left_height;
	}

	/**
	 * Properly hooks the right_height field for the food bar.
	 *
	 * InGameHud renders the food bar with this method call:
	 * {@code this.blit(al, ai, ad + 36, 27, 9, 9)}
	 *
	 * The ai variable is set to {@code ai = o}. This mixin
	 * modifies the o variable as it's needed later in the food
	 * bar.
	 */
	@ModifyVariable(method = "renderStatusBars", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=food"), ordinal = 5)
	private int hookFoodLeftHeight(int originalValue) {
		return this.scaledHeight - PatchworkIngameGui.preRenderFoodSnapshot.right_height;
	}


	/**
	 * This disables the food bar.
	 *
	 * InGameHud contains the following for loop which renders the food bar:
	 * {@code for(ah = 0; ah < 10; ++ah)}
	 *
	 * This mixin modifies the 0 constant. If the pre event is canceled,
	 * the 0 constant is replaced with 10, so {@code ah = 10}.
	 * The next condition, {@code ah < 10} will fail as {@code 10 < 10} is not true,
	 * thus canceling the for loop and causing the food bar to not render
	 */
	@ModifyConstant(method = "renderStatusBars", constant = @Constant(intValue = 0, ordinal = 4))
	private int hookDisableFood(int originalValue) {
		return ForgeIngameGui.renderFood && PatchworkIngameGui.preRenderFoodSnapshot.preResult ? 10 : originalValue;
	}
}
