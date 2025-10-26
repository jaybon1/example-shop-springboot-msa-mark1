package com.example.shop.user.presentation.dto.response;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Getter
@Builder
public class ResGetUsersDtoV1 {

    private final UserPageDto userPage;

    public static ResGetUsersDtoV1 of(Page<User> userPage) {
        return ResGetUsersDtoV1.builder()
                .userPage(new UserPageDto(userPage))
                .build();
    }

    @Getter
    @ToString
    public static class UserPageDto extends PagedModel<UserPageDto.UserDto> {

        public UserPageDto(Page<User> userPage) {
            super(
                    new PageImpl<>(
                            UserDto.from(userPage.getContent()),
                            userPage.getPageable(),
                            userPage.getTotalElements()
                    )
            );
        }

        public UserPageDto(UserDto... userDtoArray) {
            super(
                    new PageImpl<>(
                            List.of(userDtoArray)
                    )
            );
        }

        @Getter
        @Builder
        public static class UserDto {

            private final String id;
            private final String username;
            private final String nickname;
            private final String email;
            private final List<String> roleList;

            private static List<UserDto> from(List<User> userList) {
                return userList.stream()
                        .map(UserDto::from)
                        .toList();
            }

            public static UserDto from(User user) {
                return UserDto.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .roleList(
                                user.getUserRoleList()
                                        .stream()
                                        .map(UserRole::getRole)
                                        .map(Enum::name)
                                        .toList()
                        )
                        .build();
            }
        }
    }
}