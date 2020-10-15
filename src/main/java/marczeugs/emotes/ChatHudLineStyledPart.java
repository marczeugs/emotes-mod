package marczeugs.emotes;

import net.minecraft.text.Style;

public class ChatHudLineStyledPart {
	public StringBuilder textBuilder = new StringBuilder();
	public Style style = null;

	public ChatHudLineStyledPart(Style style) {
		this.style = style;
	}
}
