package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch c.item i where c.item.id in :itemIds")
    List<Comment> findAllByItem_IdIn(@Param("itemIds") Set<Long> itemIds);

    List<Comment> findAllByItem_Id(Long itemId);
}