package com.cuutruyen.controller;

import com.cuutruyen.entity.TranslationGroup;
import com.cuutruyen.entity.User;
import com.cuutruyen.repository.TranslationGroupRepository;
import com.cuutruyen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class TranslationGroupController {

    @Autowired
    private TranslationGroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả nhóm dịch
    @GetMapping
    public ResponseEntity<List<TranslationGroup>> getAllGroups() {
        return ResponseEntity.ok(groupRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TranslationGroup> getGroupById(@PathVariable Integer id) {
        TranslationGroup group = groupRepository.findById(id).orElse(null);
        if (group == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(group);
    }

    // Lấy nhóm dịch của user đang đăng nhập
    @GetMapping("/my-group")
    public ResponseEntity<?> getMyGroup(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        List<TranslationGroup> groups = groupRepository.findByLeader_UserId(user.getUserId());
        TranslationGroup myGroup = groups.stream()
            .filter(g -> g.getStatus() == TranslationGroup.Status.ACTIVE)
            .findFirst()
            .orElse(groups.isEmpty() ? null : groups.get(0));

        // Nếu không phải leader, kiểm tra xem có thuộc nhóm nào không thông qua groupId
        if (myGroup == null && user.getGroupId() != null) {
            myGroup = groupRepository.findById(user.getGroupId()).orElse(null);
        }

        if (myGroup == null) return ResponseEntity.ok(Map.of("found", false));
        return ResponseEntity.ok(myGroup);
    }

    // Lấy danh sách nhóm đang chờ duyệt
    @GetMapping("/pending")
    public ResponseEntity<List<TranslationGroup>> getPendingGroups() {
        return ResponseEntity.ok(groupRepository.findByStatus(TranslationGroup.Status.PENDING));
    }

    // Yêu cầu tạo nhóm mới (User)
    @PostMapping("/request")
    public ResponseEntity<?> requestGroup(@RequestBody Map<String, String> request, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        String name = request.get("name");
        if (name == null || name.isEmpty()) return ResponseEntity.badRequest().body("Tên nhóm không được để trống");

        TranslationGroup group = new TranslationGroup();
        group.setName(name);
        group.setLeader(user);
        group.setStatus(TranslationGroup.Status.PENDING);
        groupRepository.save(group);

        return ResponseEntity.ok(group);
    }

    // Duyệt nhóm (Admin)
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptGroup(@PathVariable Integer id) {
        TranslationGroup group = groupRepository.findById(id).orElse(null);
        if (group == null) return ResponseEntity.badRequest().body("Group not found");

        group.setStatus(TranslationGroup.Status.ACTIVE);
        groupRepository.save(group);

        // Cập nhật quyền uploader và gán groupId cho leader
        User leader = group.getLeader();
        leader.setGroupId(group.getGroupId());
        if (leader.getRole() == User.Role.user) {
            leader.setRole(User.Role.uploader);
        }
        userRepository.save(leader);

        return ResponseEntity.ok(group);
    }

    // Từ chối nhóm (Admin)
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectGroup(@PathVariable Integer id) {
        TranslationGroup group = groupRepository.findById(id).orElse(null);
        if (group == null) return ResponseEntity.badRequest().body("Group not found");

        group.setStatus(TranslationGroup.Status.REJECTED);
        groupRepository.save(group);

        return ResponseEntity.ok(group);
    }

    // Lấy danh sách thành viên của nhóm
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Integer groupId) {
        TranslationGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) return ResponseEntity.badRequest().body("Group not found");

        Integer leaderId = group.getLeader() != null ? group.getLeader().getUserId() : -1;

        List<User> members = userRepository.findAll().stream()
                .filter(u -> (u.getGroupId() != null && u.getGroupId().equals(groupId)) || u.getUserId().equals(leaderId))
                .map(u -> {
                    u.setPasswordHash(null); // hide password
                    return u;
                })
                .toList();
        return ResponseEntity.ok(members);
    }

    // Thêm hoặc cập nhật vai trò thành viên trong nhóm
    @PutMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> addMemberToGroup(@PathVariable Integer groupId, @PathVariable Integer userId, @RequestBody Map<String, Object> request, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Unauthorized");
        User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
        TranslationGroup group = groupRepository.findById(groupId).orElse(null);
        User targetUser = userRepository.findById(userId).orElse(null);

        if (group == null || targetUser == null || currentUser == null) return ResponseEntity.badRequest().body("Group or User not found");

        boolean isAdmin = currentUser.getRole() == User.Role.admin;
        boolean isLeader = group.getLeader().getUserId().equals(currentUser.getUserId());

        if (!isAdmin && !isLeader) {
            return ResponseEntity.status(403).body("Chỉ trưởng nhóm hoặc admin mới được thực hiện");
        }

        Object roleObj = request.get("role");
        String roleStr = roleObj != null ? roleObj.toString() : "translator";
        User.Role newRole;
        try {
            newRole = User.Role.valueOf(roleStr.toLowerCase());
        } catch (Exception e) {
            newRole = User.Role.translator;
        }

        targetUser.setGroupId(groupId);
        // Cập nhật role (ngoại trừ leader của nhóm)
        if (!targetUser.getUserId().equals(group.getLeader().getUserId())) {
            targetUser.setRole(newRole);
        }
        userRepository.save(targetUser);

        return ResponseEntity.ok(Map.of("message", "Thao tác thành công"));
    }

    // Xóa thành viên khỏi nhóm
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMemberFromGroup(@PathVariable Integer groupId, @PathVariable Integer userId, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Unauthorized");
        User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
        TranslationGroup group = groupRepository.findById(groupId).orElse(null);
        User targetUser = userRepository.findById(userId).orElse(null);

        if (group == null || targetUser == null || currentUser == null) return ResponseEntity.badRequest().body("Group or User not found");

        boolean isAdmin = currentUser.getRole() == User.Role.admin;
        boolean isLeader = group.getLeader().getUserId().equals(currentUser.getUserId());

        if (!isAdmin && !isLeader) {
            return ResponseEntity.status(403).body("Không có quyền");
        }
        
        if (targetUser.getUserId().equals(group.getLeader().getUserId())) {
            return ResponseEntity.badRequest().body("Không thể xóa trưởng nhóm");
        }

        targetUser.setGroupId(null);
        targetUser.setRole(User.Role.user);
        userRepository.save(targetUser);

        return ResponseEntity.ok(Map.of("message", "Đã xóa thành viên"));
    }

    // Xóa nhóm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id) {
        groupRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
