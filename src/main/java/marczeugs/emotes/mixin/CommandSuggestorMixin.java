package marczeugs.emotes.mixin;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import marczeugs.emotes.EmotesMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(CommandSuggestor.class)
public class CommandSuggestorMixin {
	@Shadow @Final MinecraftClient client;
	
	@Redirect(
			method = "refresh()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/command/CommandSource;suggestMatching(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	public CompletableFuture<?> redirectSuggestMatching(Iterable<String> collection, SuggestionsBuilder builder) {
		assert this.client.player != null;
		var playerNames = this.client.player.networkHandler.getCommandSource().getPlayerNames();
		var allSuggestions = Stream.concat(playerNames.stream(), EmotesMod.emoteNames.stream()).collect(Collectors.toList());
		return CommandSource.suggestMatching(allSuggestions, builder);
	}
}
