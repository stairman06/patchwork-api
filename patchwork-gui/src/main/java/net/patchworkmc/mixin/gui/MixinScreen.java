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

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.patchworkmc.impl.gui.PatchworkScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Screen.class)
public abstract class MixinScreen implements PatchworkScreen {
	@Unique
	private static final ThreadLocal<TextRenderer> renderTooltip_textRenderer = ThreadLocal.withInitial(() -> null);

	@Unique
	private static final ThreadLocal<ItemStack> renderTooltip_itemStack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

	@Unique
	private static final ThreadLocal<Integer> renderTooltip_tooltipTextWidth = ThreadLocal.withInitial(() -> 0);

	@Unique
	private static final ThreadLocal<Integer> renderTooltip_tooltipX = ThreadLocal.withInitial(() -> 0);

	@Shadow
	@Final
	protected List<AbstractButtonWidget> buttons;

	@Shadow
	protected abstract <T extends AbstractButtonWidget> T addButton(T button);

	@Shadow
	@Final
	protected List<Element> children;

	@Shadow
	@Nullable
	protected MinecraftClient minecraft;

	@Shadow
	protected TextRenderer font;

	@Shadow
	public abstract void renderTooltip(List<String> text, int x, int y);

	@Shadow
	public int width;
	@Shadow
	public int height;
	@Unique
	private Consumer<AbstractButtonWidget> remove = (b) -> {
		buttons.remove(b);
		children.remove(b);
	};

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", ordinal = 0, remap = false), cancellable = true)
	private void preInit(MinecraftClient client, int width, int height, CallbackInfo info) {
		if (MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre((Screen) (Object) this, this.buttons, this::addButton, remove))) {
			info.cancel();
		}
	}

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("RETURN"), cancellable = true)
	private void postInit(MinecraftClient client, int width, int height, CallbackInfo info) {
		MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post((Screen) (Object) this, this.buttons, this::addButton, remove));
	}

	@Inject(method = "renderBackground(I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;fillGradient(IIIIII)V", ordinal = 0, shift = At.Shift.AFTER))
	private void renderBackground(int alpha, CallbackInfo info) {
		MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent((Screen) (Object) this));
	}

	public MinecraftClient getMinecraft() {
		return this.minecraft;
	}

	@Inject(method = "renderTooltip(Ljava/util/List;II)V", at = @At("HEAD"))
	private void hookRenderTooltipTextRenderer(List<String> text, int x, int y, CallbackInfo ci) {
		if (renderTooltip_textRenderer.get() == null) {
			renderTooltip_textRenderer.set(this.font);
		}
	}

	// Forge's renderTooltip with font arg
	@Override
	public void renderTooltip(List<String> text, int x, int y, TextRenderer font) {
		renderTooltip_textRenderer.set(font);
		this.renderTooltip(text, x, y);
	}

	@Redirect(method = "renderTooltip(Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getStringWidth(Ljava/lang/String;)I"))
	private int renderTooltip_redirectGetStringWidth(TextRenderer textRenderer, String text) {
		return renderTooltip_textRenderer.get().getStringWidth(text);
	}

	@Redirect(method = "renderTooltip(Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I"))
	private int renderTooltip_redirectDrawWithShadow(TextRenderer textRenderer, String text, float x, float y, int color) {
		return renderTooltip_textRenderer.get().drawWithShadow(text, x, y, color);
	}

	@Inject(method = "renderTooltip(Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;disableRescaleNormal()V"), cancellable = true)
	private void renderTooltip_firePreEvent(List<String> text, int x, int y, CallbackInfo ci) {
		RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(renderTooltip_itemStack.get(), text, x, y, this.width, this.height, -1, renderTooltip_textRenderer.get());
		if (MinecraftForge.EVENT_BUS.post(event)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderTooltip(Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void renderTooltip_saveTextWidth(List<String> text, int x, int y, CallbackInfo ci, int i) {
		// The text width vanishes off of the LVT so we need to save it here
		renderTooltip_tooltipTextWidth.set(i);
	}

	@Inject(method = "renderTooltip(Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;fillGradient(IIIIII)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void renderTooltip_saveTooltipX(List<String> text, int x, int y, CallbackInfo ci, int i, int k) {
		renderTooltip_tooltipX.set(k);
	}

	@Inject(method = "renderTooltip(Ljava/util/List;II)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;enableLighting()V"))
	private void renderTooltip_emitPostTextEvent(List<String> text, int x, int y, CallbackInfo ci) {
		// TODO: Forge actually handles edges of screens here and wrapping
		int tooltipX = x + 12;
		int tooltipY = y - 12;

		// TODO: Wrapping titles
		int titleLinesCount = 1;


		// TODO: Mess
		int tooltipHeight = 8;

		if (text.size() > 1) {
			tooltipHeight += (text.size() - 1) * 10;
			if (text.size() > titleLinesCount) {
				tooltipHeight += 2; // gap between title lines and next lines
			}
		}

		MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(renderTooltip_itemStack.get(), text, renderTooltip_tooltipX.get(), tooltipY, renderTooltip_textRenderer.get(), renderTooltip_tooltipTextWidth.get(), tooltipHeight));
	}

	// Technically these are cached in GuiUtils and not Screen,
	// but I don't think anything access them
	@Inject(method = "renderTooltip(Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"))
	private void renderTooltip_cacheItemStack(ItemStack stack, int x, int y, CallbackInfo ci) {
		renderTooltip_itemStack.set(stack);
	}

	@Inject(method = "renderTooltip(Lnet/minecraft/item/ItemStack;II)V", at = @At("TAIL"))
	private void renderTooltip_resetItemStack(ItemStack stack, int x, int y, CallbackInfo ci) {
		renderTooltip_itemStack.set(ItemStack.EMPTY);
	}

}
