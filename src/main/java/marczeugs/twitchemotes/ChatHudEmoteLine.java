package marczeugs.twitchemotes;

import net.minecraft.text.Text;

public class ChatHudEmoteLine {
   private final int timestamp;
   private final Text text;
   private final int id;

   public ChatHudEmoteLine(int int_1, Text text_1, int int_2) {
      this.text = text_1;
      this.timestamp = int_1;
      this.id = int_2;
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
