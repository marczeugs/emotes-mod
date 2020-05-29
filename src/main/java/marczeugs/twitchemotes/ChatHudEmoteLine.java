package marczeugs.twitchemotes;

import net.minecraft.text.Text;

public class ChatHudEmoteLine {
	private final int timestamp;
	private final Text text;
	private final int id;

	public ChatHudEmoteLine(int timestamp, Text text, int id) {
		this.timestamp = timestamp;
		this.text = text;
		this.id = id;
	}

	public Text getText() {
		return this.text;
	}

	public int getTimestamp() {
		return this.timestamp;
	}

	public int getId() {
		return this.id;
	}
}
