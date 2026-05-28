package com.example.demo.service;

import com.example.demo.dto.EventRequestDto;
import com.example.demo.dto.EventResponseDto;
import com.example.demo.entity.Event;
import com.example.demo.entity.EventStatus;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    // 전체 목록 조회 (비회원 가능)
    @Transactional(readOnly = true)
    public List<EventResponseDto> getAll() {
        return eventRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(EventResponseDto::new).collect(Collectors.toList());
    }

    // 진행중/종료 필터 조회 (비회원 가능)
    @Transactional(readOnly = true)
    public List<EventResponseDto> getByStatus(EventStatus status) {
        return eventRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream().map(EventResponseDto::new).collect(Collectors.toList());
    }

    // 단건 조회 + 조회수 증가 (비회원 가능)
    @Transactional
    public EventResponseDto getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));
        event.setViewCount(event.getViewCount() + 1);
        eventRepository.save(event);
        return new EventResponseDto(event);
    }

    // 이벤트 등록 (ADMIN)
    @Transactional
    public EventResponseDto createEvent(EventRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Event event = new Event();
        event.setTitle(requestDto.getTitle());
        event.setContent(requestDto.getContent());
        event.setThumbnailUrl(requestDto.getThumbnailUrl());
        event.setDetailImageUrl(requestDto.getDetailImageUrl());
        event.setStatus(requestDto.getStatus() != null
                ? requestDto.getStatus() : EventStatus.ONGOING);
        event.setStartDate(requestDto.getStartDate());
        event.setEndDate(requestDto.getEndDate());
        event.setWinnerContent(requestDto.getWinnerContent());
        event.setWinnerAnnounced(requestDto.isWinnerAnnounced());

        return new EventResponseDto(eventRepository.save(event));
    }

    // 이벤트 수정 (ADMIN)
    @Transactional
    public EventResponseDto updateEvent(Long id, EventRequestDto requestDto, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.setTitle(requestDto.getTitle());
        event.setContent(requestDto.getContent());
        event.setThumbnailUrl(requestDto.getThumbnailUrl());
        event.setDetailImageUrl(requestDto.getDetailImageUrl());
        event.setStatus(requestDto.getStatus());
        event.setStartDate(requestDto.getStartDate());
        event.setEndDate(requestDto.getEndDate());
        event.setWinnerContent(requestDto.getWinnerContent());
        event.setWinnerAnnounced(requestDto.isWinnerAnnounced());

        return new EventResponseDto(eventRepository.save(event));
    }

    // 당첨자 발표 (ADMIN)
    @Transactional
    public EventResponseDto announceWinner(Long id, String winnerContent, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.setWinnerContent(winnerContent);
        event.setWinnerAnnounced(true);

        return new EventResponseDto(eventRepository.save(event));
    }

    // 이벤트 삭제 (ADMIN)
    @Transactional
    public void deleteEvent(Long id, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        eventRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        eventRepository.deleteById(id);
    }
}