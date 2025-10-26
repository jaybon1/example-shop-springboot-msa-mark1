package com.example.shop.user.infrastructure.jpa.entity;

import com.example.shop.global.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "`USER`")
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email")
    private String email;

    @Builder.Default
    @Column(name = "jwt_validator")
    private Long jwtValidator = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRoleEntity> userRoleList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSocialEntity> userSocialList = new ArrayList<>();

    public void update(String nickname, String email, Long jwtValidator) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (email != null) {
            this.email = email;
        }
        if (jwtValidator != null) {
            this.jwtValidator = jwtValidator;
        }
    }

    public void add(UserRoleEntity userRoleEntity) {
        userRoleList.add(userRoleEntity);
        userRoleEntity.setUser(this);
    }

    public void remove(UserRoleEntity userRoleEntity) {
        userRoleList.remove(userRoleEntity);
        userRoleEntity.setUser(null);
    }

    public void add(UserSocialEntity userSocialEntity) {
        userSocialList.add(userSocialEntity);
        userSocialEntity.setUser(this);
    }

    public void remove(UserSocialEntity userSocialEntity) {
        userSocialList.remove(userSocialEntity);
        userSocialEntity.setUser(null);
    }

    public void updatePassword(String encodedPassword) {
        if (encodedPassword != null) {
            this.password = encodedPassword;
        }
    }
}
