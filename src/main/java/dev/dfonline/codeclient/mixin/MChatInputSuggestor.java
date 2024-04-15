package dev.dfonline.codeclient.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.dfonline.codeclient.CodeClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.List;

@Mixin(ChatInputSuggestor.class)
public class MChatInputSuggestor {
    @Inject(method = "sortSuggestions", at = @At("RETURN"))
    private void sortSuggestions(Suggestions suggestions, CallbackInfoReturnable<List<Suggestion>> cir) throws IOException {
        CodeClient.handleChatSuggestions(suggestions);
    }
}
