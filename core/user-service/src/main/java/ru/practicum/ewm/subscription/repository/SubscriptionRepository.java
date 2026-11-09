package ru.practicum.ewm.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.subscription.model.SubscriptionStatus;
import ru.practicum.ewm.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    List<Subscription> findByFollowerId(Long followerId);

    List<Subscription> findByFollowedId(Long followedId);

    List<Subscription> findByFollowerIdAndStatus(Long followerId, SubscriptionStatus status);

    List<Subscription> findByFollowedIdAndStatus(Long followedId, SubscriptionStatus status);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedIdAndStatus(Long followerId, Long followedId, SubscriptionStatus status);

    @Query("""
    select sub.follower
    from Subscription sub
    where sub.followed.id = :userId
      and sub.status = ru.practicum.ewm.subscription.model.SubscriptionStatus.CONFIRMED
    """)
    List<User> getFollowers(Long userId);

    @Query("""
    select sub.followed.id
    from Subscription sub
    where sub.follower.id = :followerId
      and sub.followed.id in :userIds
      and sub.status = ru.practicum.ewm.subscription.model.SubscriptionStatus.CONFIRMED
    """)
    Set<Long> findConfirmedFollowedIds(@Param("followerId") Long followerId,
                                       @Param("userIds") Collection<Long> userIds);
}
