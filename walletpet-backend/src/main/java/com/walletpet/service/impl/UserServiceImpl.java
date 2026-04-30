package com.walletpet.service.impl;

import com.walletpet.dto.user.UserRegisterRequest;
import com.walletpet.dto.user.UserResponse;
import com.walletpet.dto.user.UserUpdateRequest;
import com.walletpet.entity.User;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.AccountService;
import com.walletpet.service.CategoryService;
import com.walletpet.service.PetService;
import com.walletpet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final PetService petService;

    @Override
    public UserResponse registerUser(UserRegisterRequest request) {
        // 0. 檢查註冊資料是否完整
        if (request == null) {
            throw new BusinessException("註冊資料不可為空");
        }

        if (request.getUserName() == null || request.getUserName().isBlank()) {
            throw new BusinessException("帳號不可為空");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("密碼不可為空");
        }

        if (request.getPetName() == null || request.getPetName().isBlank()) {
            throw new BusinessException("寵物名稱不可為空");
        }

        // 1. 檢查帳號重複
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new BusinessException("帳號名稱已被使用");
        }

        // 2. DTO 轉 Entity
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUserName(request.getUserName());
        user.setPassword(request.getPassword()); // 之後建議加上加密
        user.setRole("USER");

        // 3. 儲存
        User savedUser = userRepository.save(user);

        // 3-1. 建立新使用者時，同步建立預設分類
        categoryService.createDefaultCategoriesForUser(savedUser);

        // 3-2. 建立新使用者時，同步建立預設帳戶
        accountService.createDefaultAccountsForUser(savedUser);

        // 3-3. 建立新使用者時，同步建立預設寵物
        petService.createDefaultPetForUser(savedUser, request.getPetName());

        // 4. Entity 轉 Response DTO
        return convertToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserById(String userId) {
        User user = getUserEntityById(userId);
        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserByUserName(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));
        return convertToResponse(user);
    }

    @Override
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = getUserEntityById(userId);

        if (request == null) {
            throw new BusinessException("修改資料不可為空");
        }

        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            // 如果新帳號名稱和原本不同，才需要檢查是否重複
            if (!request.getUserName().equals(user.getUserName())
                    && userRepository.existsByUserName(request.getUserName())) {
                throw new BusinessException("帳號名稱已被使用");
            }

            user.setUserName(request.getUserName());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword()); // 之後建議加上加密
        }

        return convertToResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("使用者不存在");
        }

        userRepository.deleteById(userId);
    }

    // 輔助方法：將 User 實體轉為 UserResponse
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // 輔助方法：取得 User 實體，供其他 Service 或登入驗證使用
    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));
        return user;
    }
}