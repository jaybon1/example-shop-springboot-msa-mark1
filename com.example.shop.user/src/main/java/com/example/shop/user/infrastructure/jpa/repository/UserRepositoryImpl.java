package com.example.shop.user.infrastructure.jpa.repository;

import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.infrastructure.jpa.entity.QUserEntity;
import com.example.shop.user.infrastructure.jpa.entity.UserEntity;
import com.example.shop.user.infrastructure.jpa.mapper.UserMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRepositoryImpl implements UserRepository {

    private static final QUserEntity user = QUserEntity.userEntity;

    private final UserJpaRepository userJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public User save(User user) {
        UserEntity userEntity;
        if (user.getId() != null) {
            userEntity = userJpaRepository.findById(user.getId())
                    .orElseGet(() -> userMapper.toEntity(user));
            userMapper.applyDomain(user, userEntity);
        } else {
            userEntity = userMapper.toEntity(user);
        }
        UserEntity savedUserEntity = userJpaRepository.save(userEntity);
        return userMapper.toDomain(savedUserEntity);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userJpaRepository.findById(userId).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public Page<User> searchUsers(String username, String nickname, String email, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(username)) {
            builder.and(user.username.containsIgnoreCase(username));
        }
        if (StringUtils.hasText(nickname)) {
            builder.and(user.nickname.containsIgnoreCase(nickname));
        }
        if (StringUtils.hasText(email)) {
            builder.and(user.email.containsIgnoreCase(email));
        }

        var query = jpaQueryFactory.selectFrom(user)
                .where(builder)
                .orderBy(resolveOrderSpecifiers(pageable.getSort()));

        List<User> content;
        long total;

        if (pageable.isPaged()) {
            content = query
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch()
                    .stream()
                    .map(userMapper::toDomain)
                    .toList();

            Long fetchedTotal = jpaQueryFactory.select(user.count())
                    .from(user)
                    .where(builder)
                    .fetchOne();
            total = fetchedTotal == null ? content.size() : fetchedTotal;
            return new PageImpl<>(content, pageable, total);
        }

        content = query.fetch()
                .stream()
                .map(userMapper::toDomain)
                .toList();
        total = content.size();
        return new PageImpl<>(content, Pageable.unpaged(), total);
    }

    @Override
    public long count() {
        return userJpaRepository.count();
    }

    private OrderSpecifier<?>[] resolveOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (sort != null && sort.isSorted()) {
            for (Sort.Order order : sort) {
                orderSpecifiers.add(toOrderSpecifier(order));
            }
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(user.createdAt.desc());
        }

        return orderSpecifiers.toArray(new OrderSpecifier<?>[0]);
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        return switch (order.getProperty()) {
            case "username" -> order.isAscending() ? user.username.asc() : user.username.desc();
            case "nickname" -> order.isAscending() ? user.nickname.asc() : user.nickname.desc();
            case "email" -> order.isAscending() ? user.email.asc() : user.email.desc();
            case "createdAt" -> order.isAscending() ? user.createdAt.asc() : user.createdAt.desc();
            case "updatedAt" -> order.isAscending() ? user.updatedAt.asc() : user.updatedAt.desc();
            default -> order.isAscending() ? user.createdAt.asc() : user.createdAt.desc();
        };
    }
}
