package marczeugs.emotes;

import net.minecraft.text.Style;

public class ChatHudLineStyledPart {
	public final StringBuilder textBuilder = new StringBuilder();
	public Style style;

	public ChatHudLineStyledPart(Style style) {
		this.style = style;
	}
}
