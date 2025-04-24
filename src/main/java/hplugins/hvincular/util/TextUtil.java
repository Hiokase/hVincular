package hplugins.hvincular.util;

import org.bukkit.ChatColor;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário para tratamento de texto e correção de encoding.
 */
public class TextUtil {

    private static final Pattern EMOJI_TOKEN_PATTERN = Pattern.compile(":(\\w+):");
    
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    
    static {
        EMOJI_MAP.put("smile", "😄");
        EMOJI_MAP.put("grin", "😁");
        EMOJI_MAP.put("joy", "😂");
        EMOJI_MAP.put("rofl", "🤣");
        EMOJI_MAP.put("smiley", "😃");
        EMOJI_MAP.put("grinning", "😀");
        EMOJI_MAP.put("sweat_smile", "😅");
        EMOJI_MAP.put("laughing", "😆");
        EMOJI_MAP.put("wink", "😉");
        EMOJI_MAP.put("blush", "😊");
        EMOJI_MAP.put("yum", "😋");
        EMOJI_MAP.put("sunglasses", "😎");
        EMOJI_MAP.put("heart_eyes", "😍");
        EMOJI_MAP.put("kissing_heart", "😘");
        EMOJI_MAP.put("thinking", "🤔");
        EMOJI_MAP.put("unamused", "😒");
        EMOJI_MAP.put("disappointed", "😞");
        EMOJI_MAP.put("worried", "😟");
        EMOJI_MAP.put("triumph", "😤");
        EMOJI_MAP.put("cry", "😢");
        EMOJI_MAP.put("sob", "😭");
        EMOJI_MAP.put("frowning", "😦");
        EMOJI_MAP.put("anguished", "😧");
        EMOJI_MAP.put("fearful", "😨");
        EMOJI_MAP.put("weary", "😩");
        EMOJI_MAP.put("exploding_head", "🤯");
        EMOJI_MAP.put("flushed", "😳");
        EMOJI_MAP.put("dizzy_face", "😵");
        EMOJI_MAP.put("rage", "😡");
        EMOJI_MAP.put("angry", "😠");
        EMOJI_MAP.put("innocent", "😇");
        EMOJI_MAP.put("cowboy", "🤠");
        EMOJI_MAP.put("clown", "🤡");
        EMOJI_MAP.put("lying_face", "🤥");
        EMOJI_MAP.put("mask", "😷");
        EMOJI_MAP.put("face_with_thermometer", "🤒");
        EMOJI_MAP.put("face_with_head_bandage", "🤕");
        EMOJI_MAP.put("nauseated_face", "🤢");
        EMOJI_MAP.put("vomiting", "🤮");
        EMOJI_MAP.put("sneezing_face", "🤧");
        EMOJI_MAP.put("cold_face", "🥶");
        EMOJI_MAP.put("hot_face", "🥵");
        EMOJI_MAP.put("sleeping", "😴");
        EMOJI_MAP.put("drooling_face", "🤤");
        EMOJI_MAP.put("money_mouth", "🤑");
        EMOJI_MAP.put("nerd", "🤓");
        EMOJI_MAP.put("sunglasses", "😎");
        EMOJI_MAP.put("confused", "😕");
        EMOJI_MAP.put("worried", "😟");
        EMOJI_MAP.put("slightly_frowning_face", "🙁");
        EMOJI_MAP.put("white_frowning_face", "☹️");
        EMOJI_MAP.put("astonished", "😲");
        EMOJI_MAP.put("zipper_mouth", "🤐");
        
        EMOJI_MAP.put("heart", "❤️");
        EMOJI_MAP.put("yellow_heart", "💛");
        EMOJI_MAP.put("green_heart", "💚");
        EMOJI_MAP.put("blue_heart", "💙");
        EMOJI_MAP.put("purple_heart", "💜");
        EMOJI_MAP.put("black_heart", "🖤");
        EMOJI_MAP.put("broken_heart", "💔");
        EMOJI_MAP.put("heavy_heart_exclamation", "❣️");
        EMOJI_MAP.put("two_hearts", "💕");
        EMOJI_MAP.put("revolving_hearts", "💞");
        EMOJI_MAP.put("heartbeat", "💓");
        EMOJI_MAP.put("heartpulse", "💗");
        EMOJI_MAP.put("sparkling_heart", "💖");
        EMOJI_MAP.put("cupid", "💘");
        EMOJI_MAP.put("gift_heart", "💝");
        EMOJI_MAP.put("heart_decoration", "💟");
        
        EMOJI_MAP.put("thumbsup", "👍");
        EMOJI_MAP.put("thumbsdown", "👎");
        EMOJI_MAP.put("ok_hand", "👌");
        EMOJI_MAP.put("punch", "👊");
        EMOJI_MAP.put("raised_hands", "🙌");
        EMOJI_MAP.put("prayer", "🙏");
        EMOJI_MAP.put("muscle", "💪");
        EMOJI_MAP.put("point_up", "☝️");
        EMOJI_MAP.put("point_down", "👇");
        EMOJI_MAP.put("point_left", "👈");
        EMOJI_MAP.put("point_right", "👉");
        EMOJI_MAP.put("middle_finger", "🖕");
        EMOJI_MAP.put("wave", "👋");
        EMOJI_MAP.put("clap", "👏");
        EMOJI_MAP.put("raised_hand", "✋");
        EMOJI_MAP.put("open_hands", "👐");
        
        EMOJI_MAP.put("fire", "🔥");
        EMOJI_MAP.put("star", "⭐");
        EMOJI_MAP.put("zap", "⚡");
        EMOJI_MAP.put("boom", "💥");
        EMOJI_MAP.put("exclamation", "❗");
        EMOJI_MAP.put("question", "❓");
        EMOJI_MAP.put("warning", "⚠️");
        EMOJI_MAP.put("bell", "🔔");
        EMOJI_MAP.put("trophy", "🏆");
        EMOJI_MAP.put("medal", "🏅");
        EMOJI_MAP.put("crown", "👑");
        EMOJI_MAP.put("gem", "💎");
        EMOJI_MAP.put("rocket", "🚀");
        EMOJI_MAP.put("lock", "🔒");
        EMOJI_MAP.put("key", "🔑");
        EMOJI_MAP.put("mag", "🔍");
        EMOJI_MAP.put("bulb", "💡");
        EMOJI_MAP.put("moneybag", "💰");
        EMOJI_MAP.put("dollar", "💵");
        EMOJI_MAP.put("credit_card", "💳");
        
        EMOJI_MAP.put("video_game", "🎮");
        EMOJI_MAP.put("joystick", "🕹️");
        EMOJI_MAP.put("game_die", "🎲");
        EMOJI_MAP.put("crossed_swords", "⚔️");
        EMOJI_MAP.put("shield", "🛡️");
        EMOJI_MAP.put("bow_and_arrow", "🏹");
        EMOJI_MAP.put("pick", "⛏️");
        EMOJI_MAP.put("wrench", "🔧");
        EMOJI_MAP.put("hammer", "🔨");
        EMOJI_MAP.put("nut_and_bolt", "🔩");
        EMOJI_MAP.put("gear", "⚙️");
        EMOJI_MAP.put("computer", "💻");
        EMOJI_MAP.put("desktop", "🖥️");
        EMOJI_MAP.put("keyboard", "⌨️");
        EMOJI_MAP.put("headphones", "🎧");
        EMOJI_MAP.put("movie_camera", "🎥");
        EMOJI_MAP.put("camera", "📷");
        EMOJI_MAP.put("tv", "📺");
        
        EMOJI_MAP.put("white_check_mark", "✅");
        EMOJI_MAP.put("ballot_box_with_check", "☑️");
        EMOJI_MAP.put("heavy_check_mark", "✔️");
        EMOJI_MAP.put("heavy_multiplication_x", "✖️");
        EMOJI_MAP.put("x", "❌");
        EMOJI_MAP.put("negative_squared_cross_mark", "❎");
        EMOJI_MAP.put("question", "❓");
        EMOJI_MAP.put("grey_question", "❔");
        EMOJI_MAP.put("grey_exclamation", "❕");
        EMOJI_MAP.put("exclamation", "❗");
        
        EMOJI_MAP.put("speech_balloon", "💬");
        EMOJI_MAP.put("thought_balloon", "💭");
        EMOJI_MAP.put("envelope", "✉️");
        EMOJI_MAP.put("email", "📧");
        EMOJI_MAP.put("mailbox", "📫");
        EMOJI_MAP.put("newspaper", "📰");
        EMOJI_MAP.put("bookmark", "🔖");
        EMOJI_MAP.put("chart_with_upwards_trend", "📈");
        EMOJI_MAP.put("chart_with_downwards_trend", "📉");
        EMOJI_MAP.put("calendar", "📆");
        EMOJI_MAP.put("clipboard", "📋");
        
        EMOJI_MAP.put("youtube", "▶️");
        EMOJI_MAP.put("play_button", "▶️");
        EMOJI_MAP.put("pause_button", "⏸️");
        EMOJI_MAP.put("stop_button", "⏹️");
        EMOJI_MAP.put("record_button", "⏺️");
        EMOJI_MAP.put("fast_forward", "⏩");
        EMOJI_MAP.put("rewind", "⏪");
        EMOJI_MAP.put("arrow_forward", "▶️");
        EMOJI_MAP.put("arrow_backward", "◀️");
        EMOJI_MAP.put("arrow_up_small", "🔼");
        EMOJI_MAP.put("arrow_down_small", "🔽");
        EMOJI_MAP.put("film_frames", "🎞️");
        EMOJI_MAP.put("clapper", "🎬");
    }

    /**
     * Converte uma string para UTF-8 e aplica cores do Minecraft.
     * 
     * @param text Texto a ser convertido
     * @return Texto convertido com cores
     */
    public static String formatText(String text) {
        if (text == null) {
            return "";
        }
        
        String utf8Text = toUTF8(text);
        
        return ChatColor.translateAlternateColorCodes('&', utf8Text);
    }
    
    /**
     * Converte uma string para UTF-8.
     * 
     * @param text Texto a ser convertido
     * @return Texto em UTF-8
     */
    public static String toUTF8(String text) {
        if (text == null) {
            return "";
        }
        
        try {
            byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
            String utf8Text = new String(bytes, StandardCharsets.UTF_8);
            
            if (utf8Text.contains("�")) {
                return text;
            }
            
            return utf8Text;
        } catch (Exception e) {
            return text;
        }
    }
    
    /**
     * Remove acentos de uma string.
     * 
     * @param text Texto com acentos
     * @return Texto sem acentos
     */
    public static String removeAccents(String text) {
        if (text == null) {
            return "";
        }
        
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
    }
    
    /**
     * Aplica cores do Minecraft e formata mensagens com placeholders.
     * 
     * @param text Texto a ser formatado
     * @param placeholders Placeholders no formato {chave}
     * @param values Valores correspondentes aos placeholders
     * @return Texto formatado
     */
    public static String formatWithPlaceholders(String text, String[] placeholders, String[] values) {
        if (text == null) {
            return "";
        }
        
        String formattedText = toUTF8(text);
        
        if (placeholders != null && values != null && placeholders.length == values.length) {
            for (int i = 0; i < placeholders.length; i++) {
                if (placeholders[i] != null && values[i] != null) {
                    formattedText = formattedText.replace(placeholders[i], values[i]);
                }
            }
        }
        
        return ChatColor.translateAlternateColorCodes('&', formattedText);
    }
    
    /**
     * Converte tokens de emoji no formato :emoji_name: para caracteres Unicode reais.
     * 
     * @param text Texto contendo tokens de emoji
     * @return Texto com emojis no formato Unicode
     */
    public static String processEmojiTokens(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        
        Matcher matcher = EMOJI_TOKEN_PATTERN.matcher(text);
        
        int lastEnd = 0;
        
        while (matcher.find()) {
            result.append(text, lastEnd, matcher.start());
            
            String emojiName = matcher.group(1);
            
            String emojiUnicode = EMOJI_MAP.get(emojiName);
            if (emojiUnicode != null) {
                result.append(emojiUnicode);
            } else {
                result.append(matcher.group());
            }
            
            lastEnd = matcher.end();
        }
        
        if (lastEnd < text.length()) {
            result.append(text.substring(lastEnd));
        }
        
        return result.toString();
    }
    
    /**
     * Adiciona token de emoji a uma string.
     * Útil para mostrar o formato token nos menus e mensagens de instrução.
     * 
     * @param emojiName Nome do emoji a ser adicionado
     * @return Token de emoji no formato :emoji_name:
     */
    public static String addEmojiToken(String emojiName) {
        if (emojiName == null || emojiName.isEmpty()) {
            return "";
        }
        return ":" + emojiName + ":";
    }
    
    /**
     * Verifica se uma string contém tokens de emoji.
     * 
     * @param text Texto a ser verificado
     * @return true se contiver tokens, false caso contrário
     */
    public static boolean containsEmojiTokens(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        return EMOJI_TOKEN_PATTERN.matcher(text).find();
    }
}