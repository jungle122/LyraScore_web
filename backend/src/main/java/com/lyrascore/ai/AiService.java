package com.lyrascore.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 调用阿里云百炼 qwen-turbo 清洗自由文本点歌请求。
 * 失败时返回 null（不抛异常），调用方按 CLAUDE.md 的降级策略处理：
 * 写入原文 + ai_song/ai_artist 留 null + status=2 待人工审核。
 */
@Slf4j
@Service
public class AiService {

    private static final String ENDPOINT =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            你是音乐信息提取助手。用户会给你一段自由文本（含歌曲名/歌手名等），
            请只返回 JSON，不要解释、不要 markdown：
            {"song":"歌曲名","artist":"歌手名"}

            规则：
            1. 歌手名优先返回业内通用的官方艺名（中文歌手用中文，外文歌手用英文原名）。
               例如：用户写「霉霉」→ 返回「Taylor Swift」；
                     用户写「碧梨」→ 返回「Billie Eilish」；
                     用户写「周董」→ 返回「周杰伦」。
            2. 歌曲名保留原始大小写，不要乱翻译。
            3. 如果歌手名识别不出来，artist 返回空字符串。
            4. 如果完全识别不出歌曲，返回 {"song":"","artist":""}。
            """;

    @Value("${lyrascore.ai.enabled:false}")
    private boolean enabled;

    @Value("${lyrascore.ai.api-key:}")
    private String apiKey;

    @Value("${lyrascore.ai.model:qwen-turbo}")
    private String model;

    private final ObjectMapper json = new ObjectMapper();

    /** 返回 [song, artist]；任意一项可能为空字符串。失败返回 null。 */
    public String[] cleanSongRequest(String rawInput) {
        if (!enabled) {
            log.info("AI 未启用 (lyrascore.ai.enabled=false)，跳过清洗");
            return null;
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI 已启用但 api-key 为空");
            return null;
        }

        try {
            RestClient client = RestClient.builder()
                    .baseUrl(ENDPOINT)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user",   "content", rawInput)
                    ),
                    "temperature", 0.2
            );

            String resp = client.post()
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (resp == null) return null;
            JsonNode root = json.readTree(resp);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) return null;

            // qwen 偶尔会包 markdown 反引号，剥一下
            content = content.trim();
            if (content.startsWith("```")) {
                int firstNl = content.indexOf('\n');
                int lastTicks = content.lastIndexOf("```");
                if (firstNl > 0 && lastTicks > firstNl) {
                    content = content.substring(firstNl + 1, lastTicks).trim();
                }
            }

            JsonNode parsed = json.readTree(content);
            String song   = parsed.path("song").asText("").trim();
            String artist = parsed.path("artist").asText("").trim();
            return new String[]{song, artist};
        } catch (Exception e) {
            log.warn("AI 清洗失败: {}", e.getMessage());
            return null;
        }
    }
}
