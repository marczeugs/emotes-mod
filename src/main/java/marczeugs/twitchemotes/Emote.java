package marczeugs.twitchemotes;

import net.minecraft.util.Identifier;

public class Emote {
	public Identifier identifier;
	public int width;
	public int height;
	public boolean animated;
	public int frames;
	public int delay;
	
	public Emote(Identifier identifier, int width, int height, boolean animated, int frames, int delay) {
		this.identifier = identifier;
		this.width = width;
		this.height = height;
		this.animated = animated;
		this.frames = frames;
		this.delay = delay;
	}
}
