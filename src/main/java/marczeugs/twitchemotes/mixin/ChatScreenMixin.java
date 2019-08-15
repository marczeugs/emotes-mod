package marczeugs.twitchemotes.mixin;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import marczeugs.twitchemotes.TwitchEmotesMod;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.server.command.CommandSource;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {
	@Shadow protected TextFieldWidget chatField;
	@Shadow private ParseResults<CommandSource> parseResults;
	@Shadow private CompletableFuture<Suggestions> suggestionsFuture;
	@Shadow private String originalChatText;
	
	@Shadow private void updateCommandFeedback() {}
	@Shadow private static int getLastWhitespaceIndex(String string_1) { return 0; }
	
	
	private ChatScreenMixin(String string_1) { super(null); } // Useless, doesn't get called anyways
	
	
	@Inject(method = "updateCommand", at = @At("TAIL"))
	private void onUpdateCommand(CallbackInfo ci) {
		String chatText = this.chatField.getText();
		StringReader chatReader = new StringReader(chatText);
		
		if(!(chatReader.canRead() && chatReader.peek() == '/')) {
			int index = getLastWhitespaceIndex(chatText);
			Collection<String> playerNames = this.minecraft.player.networkHandler.getCommandSource().getPlayerNames();
			Collection<String> allSuggestions = Stream.concat(playerNames.stream(), TwitchEmotesMod.twitchEmoteNames.stream()).collect(Collectors.toList());
			
			this.suggestionsFuture = CommandSource.suggestMatching(allSuggestions, new SuggestionsBuilder(chatText, index));
	    }
	}
}
