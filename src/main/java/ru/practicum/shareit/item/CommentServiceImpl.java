package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;

import ru.practicum.shareit.item.model.Comment;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper mapper;

    @Override
    public CommentResponse saveComment(CommentRequest commentRequest) {
        Comment saved = commentRepository.save(mapper.mapToComment(commentRequest));
        return mapper.mapToDto(saved);
    }

    @Override
    public List<CommentResponse> getCommentsOfItem(Long itemId) {
        return commentRepository.findAllByItem_Id(itemId)
                .stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<CommentResponse>> getItemIdToComments(Set<Long> itemIds) {
        return commentRepository.findAllByItem_IdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId(),
                        Collectors.mapping(mapper::mapToDto, Collectors.toList())));
    }
}