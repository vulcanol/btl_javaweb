package com.cuutruyen.controller;

import com.cuutruyen.dto.ChatRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/ai")
public class OllamaController {

    private final RestTemplate restTemplate;
    private final com.cuutruyen.repository.SeriesRepository seriesRepository;
    private final com.cuutruyen.repository.TranslationGroupRepository groupRepository;
    private final com.cuutruyen.repository.UserRepository userRepository;
    private final com.cuutruyen.repository.GenreRepository genreRepository;
    private final com.cuutruyen.repository.AuthorRepository authorRepository;

    public OllamaController(RestTemplate restTemplate, 
                            com.cuutruyen.repository.SeriesRepository seriesRepository,
                            com.cuutruyen.repository.TranslationGroupRepository groupRepository,
                            com.cuutruyen.repository.UserRepository userRepository,
                            com.cuutruyen.repository.GenreRepository genreRepository,
                            com.cuutruyen.repository.AuthorRepository authorRepository) {
        this.restTemplate = restTemplate;
        this.seriesRepository = seriesRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.authorRepository = authorRepository;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithOllama(@RequestBody ChatRequest request) {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = false;
            String currentUser = "Khách (Guest)";
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                currentUser = auth.getName();
                isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }

            // Lấy một số thông tin giới hạn từ DB để làm context (top 50)
            long mangaCount = seriesRepository.count();
            String seriesTitles = seriesRepository.findAll(PageRequest.of(0, 50)).stream()
                                    .map(com.cuutruyen.entity.Series::getTitle)
                                    .collect(java.util.stream.Collectors.joining(", "));

            long groupCount = groupRepository.count();
            String groupNames = groupRepository.findAll(PageRequest.of(0, 50)).stream()
                                    .map(com.cuutruyen.entity.TranslationGroup::getName)
                                    .collect(java.util.stream.Collectors.joining(", "));

            long genreCount = genreRepository.count();
            String genreNames = genreRepository.findAll(PageRequest.of(0, 50)).stream()
                                    .map(com.cuutruyen.entity.Genre::getGenreName)
                                    .collect(java.util.stream.Collectors.joining(", "));

            long authorCount = authorRepository.count();
            String authorNames = authorRepository.findAll(PageRequest.of(0, 50)).stream()
                                    .map(com.cuutruyen.entity.Author::getName)
                                    .collect(java.util.stream.Collectors.joining(", "));

            String ollamaUrl = "http://localhost:11434/api/generate";

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama3");
            
            StringBuilder dbContext = new StringBuilder();
            dbContext.append("DỮ LIỆU WEBSITE (Hãy dùng thông tin này để trả lời):\n");
            dbContext.append("- Tác phẩm (").append(mangaCount).append("): ").append(seriesTitles).append("\n");
            dbContext.append("- Nhóm dịch (").append(groupCount).append("): ").append(groupNames).append("\n");
            dbContext.append("- Thể loại (").append(genreCount).append("): ").append(genreNames).append("\n");
            if (authorCount > 0) {
                dbContext.append("- Tác giả (").append(authorCount).append("): ").append(authorNames).append("\n");
            }

            if (isAdmin) {
                long userCount = userRepository.count();
                String userNames = userRepository.findAll(PageRequest.of(0, 50)).stream()
                                        .map(com.cuutruyen.entity.User::getUsername)
                                        .collect(java.util.stream.Collectors.joining(", "));
                dbContext.append("- Thành viên (").append(userCount).append("): ").append(userNames).append("\n");
            }

            String roleInstruction = isAdmin ? 
                "Người dùng hiện tại là ADMIN (" + currentUser + "). Bạn có quyền trả lời mọi câu hỏi kể cả thông tin nhạy cảm của hệ thống." : 
                "Người dùng hiện tại là " + currentUser + " (Thành viên thường/Khách). TỪ CHỐI cung cấp thông tin cá nhân của các thành viên khác hoặc thông tin bảo mật, hãy nói rằng họ không đủ quyền hạn.";

            String systemPrompt = "Bạn là Maid AI dễ thương của website đọc truyện tranh 'Cứu Truyện'.\n" +
                                  dbContext.toString() + "\n" +
                                  roleInstruction + "\n" +
                                  "QUY TẮC TỐI THƯỢNG: BẮT BUỘC PHẢI TRẢ LỜI BẰNG TIẾNG VIỆT 100%, TUYỆT ĐỐI KHÔNG ĐƯỢC SỬ DỤNG TIẾNG ANH. " +
                                  "Hãy luôn gọi người dùng là 'Chủ nhân', trả lời ngắn gọn, ngọt ngào và tự nhiên. " +
                                  "Nếu được hỏi thông tin mà bạn KHÔNG BIẾT HOẶC KHÔNG CÓ TRONG DỮ LIỆU, hãy xin lỗi và bảo rằng bạn không biết, tuyệt đối không được tự bịa ra thông tin. " +
                                  "Tin nhắn của Chủ nhân: " + request.getMessage();

            body.put("prompt", systemPrompt);
            body.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String aiResponse = (String) response.getBody().get("response");
                Map<String, String> result = new HashMap<>();
                result.put("response", aiResponse);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(500).body("Error communicating with Ollama.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
