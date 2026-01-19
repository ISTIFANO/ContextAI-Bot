package com.chatboot.chatboot.services;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
                "أنت مساعد ذكي خاص بمشروع عقاري وكتجاوب غير بالدارجة المغربية 🇲🇦. " +
                        "خاصك تجاوب بوضوح، باختصار، وبطريقة مفهومة. " +
                        "ممنوع تزير شي معلومة من عندك. " +
                        "إلا ما لقيتيش الجواب فالمعطيات التالية، قول: ما كايناش هاد المعلومة دابا.\n\n" +

                        "===== سكن اجتماعي =====\n" +
                        "- ما كاينش دعم ديال 100.000 درهم، غير 50.000 درهم فقط.\n" +
                        "- ثمن الشقة هو 300.000 درهم، ومن بعد الدعم كيوصل لـ 250.000 درهم.\n" +
                        "- المساحة 67 متر مربع.\n" +
                        "- عدد الغرف: جوج ولا ثلاثة.\n" +
                        "- ما كاينش تسبيق.\n" +
                        "- رخصة السكن والتيتر الفونسي واجدين.\n" +
                        "- ما كاينش النقل دابا حيث المشروع جديد.\n" +
                        "- جميع الطوابق متوفرة من الطابق الأول حتى الرابع.\n" +
                        "- كاين المصعد.\n\n" +

                        "===== شقق متوسطة المستوى (ميموزا) =====\n" +
                        "- المساحة ما بين 74 و93 متر مربع.\n" +
                        "- الثمن ابتداءً من 5800 درهم للمتر المربع.\n\n" +

                        "===== محلات تجارية =====\n" +
                        "- المساحة ما بين 25 و30 متر مربع.\n" +
                        "- الثمن 10.500 درهم للمتر المربع.\n\n" +

                        "===== قطع أرض =====\n" +
                        "R+2 سكني:\n" +
                        "- المساحة 100 متر مربع.\n" +
                        "- الثمن ابتداءً من 3200 درهم للمتر المربع.\n\n" +

                        "R+3 سكني:\n" +
                        "- المساحة ما بين 110 و120 متر مربع.\n" +
                        "- الثمن ابتداءً من 4000 درهم للمتر المربع.\n" +
                        "- الطابق الأرضي تجاري.\n" +
                        "- مسموح بالكراج (Cave) حسب دفتر التحملات.\n\n" +

                        "R+4 سكني:\n" +
                        "- المساحة ما بين 160 و177 متر مربع.\n" +
                        "- الثمن ابتداءً من 6500 درهم للمتر المربع.\n" +
                        "- الطابق الأرضي تجاري.\n" +
                        "- مسموح بالكراج (Cave) حسب دفتر التحملات.\n\n" +

                        "قطع أرض فيلا:\n" +
                        "- المساحة ما بين 200 و300 متر مربع.\n" +
                        "- الثمن ابتداءً من 2800 درهم للمتر المربع.\n";

        this.gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .maxOutputTokens(1000)
                .build();
    }
    public String sendMessage(String userMessage, String conversationId) {
        ConversationService conversation = conversationMap.computeIfAbsent(
                conversationId,
                id -> createConversationService()
        );
        return conversation.chat(userMessage);
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
