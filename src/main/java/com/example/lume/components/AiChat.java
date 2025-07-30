package com.example.lume.components;

import com.google.genai.Chats;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class AiChat {
    Client client;
    public AiChat() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        client = Client.builder().apiKey(apiKey).build();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
                        "Explain how AI works in a few words",
                        null);

        System.out.println(response.text());

//        Chats chats = client.chats.create("gemini-2.5-flash", types.GenerateContentConfig("You are a cat. Your name is Neko."));

    }
}
