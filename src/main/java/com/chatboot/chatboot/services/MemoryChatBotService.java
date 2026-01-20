package com.chatboot.chatboot.services;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemoryChatBotService {

    private final GoogleAiGeminiChatModel gemini;
    private final Map<String, ConversationService> conversationMap = new ConcurrentHashMap<>();
    private final String systemPrompt;

    public interface ConversationService {
        String chat(String message);
    }

    public MemoryChatBotService(@Value("${gemini.api.key}") String geminiApiKey) {
        this.systemPrompt =
                "أنت مساعد ذكي واحترافي خاص بمشروع عقاري، وكتجاوب غير بالدارجة المغربية.\n\n" +

                        "الهدف ديالك:\n" +
                        "- تفهم الاحتياج الحقيقي ديال الكليان (السكن، الاستثمار، الميزانية، المساحة، عدد الغرف).\n" +
                        "- تسول أسئلة توضيحية إلا كان الطلب ما واضحش.\n" +
                        "- تقترح عروض مناسبة غير من قاعدة المعطيات اللي تحت.\n" +
                        "- تجاوب بوضوح، باختصار، وبطريقة سهلة للفهم.\n" +
                        "- ما تزيد حتى معلومة من عندك.\n" +
                        "- ما تستعمل حتى معلومة ما كايناش فالمعطيات.\n" +
                        "- إلا ما لقيتيش الجواب فالمعطيات، قول بوضوح:\n" +
                        "تسول أسئلة توضيحية إلا كان الطلب ما واضحش:\n" +
                        "  \"ما كايناش هاد المعلومة دابا.\"\n\n" +

                        "قاعدة المعطيات العقارية\n" +
                        "==============================\n\n" +

                        "===== سكن اجتماعي =====\n" +
                        "- الدعم: 50.000 درهم فقط.\n" +
                        "- ثمن الشقة: 300.000 درهم (من بعد الدعم: 250.000 درهم).\n" +
                        "- المساحة: 67 متر مربع.\n" +
                        "- عدد الغرف: جوج ولا ثلاثة.\n" +
                        "- ما كاينش تسبيق.\n" +
                        "- رخصة السكن والتيتر الفونسي واجدين.\n" +
                        "- جميع الطوابق متوفرة من 1 حتى 4.\n" +
                        "- كاين المصعد.\n\n" +

                        "===== شقق ميموزا =====\n" +
                        "- المساحة: 74-93 متر مربع.\n" +
                        "- الثمن: ابتداءً من 5800 درهم/م².\n\n" +

                        "===== محلات تجارية =====\n" +
                        "- المساحة: 25-30 متر مربع.\n" +
                        "- الثمن: 10.500 درهم/م².\n\n" +

                        "===== قطع أرض =====\n" +
                        "R+2: 100 م², 3200 درهم/م².\n" +
                        "R+3: 110-120 م², 4000 درهم/م², الطابق الأرضي تجاري, Cave مسموح.\n" +
                        "R+4: 160-177 م², 6500 درهم/م², الطابق الأرضي تجاري, Cave مسموح.\n" +
                        "فيلا: 200-300 م², 2800 درهم/م².\n";

        this.gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.0)
                .maxOutputTokens(500)
                .maxRetries(0)
                .build();
    }

    public String sendMessage(String userMessage, String conversationId) {


        ConversationService conversation = conversationMap.computeIfAbsent(
                conversationId,
                id -> createConversationService()
        );

        String response;
        try {
            response = conversation.chat(userMessage);
        } catch (Exception e) {
            response = " Error calling LLM: " + e.getMessage();
        }


        return response;
    }

    private ConversationService createConversationService() {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(30);
        chatMemory.add(SystemMessage.from(systemPrompt));

        return AiServices.builder(ConversationService.class)
                .chatModel(gemini)
                .chatMemory(chatMemory)
                .build();
    }

    public void clearConversation(String conversationId) {
        conversationMap.remove(conversationId);
    }

    public void clearAllConversations() {
        conversationMap.clear();
    }

    public int getActiveConversationsCount() {
        return conversationMap.size();
    }
}
