package net.patchworkmc.impl.gui;

import net.minecraft.client.font.TextRenderer;

import java.util.List;

public interface PatchworkScreen {
	// Forge's renderTooltip with font arg
	void renderTooltip(List<String> text, int x, int y, TextRenderer font);
}
