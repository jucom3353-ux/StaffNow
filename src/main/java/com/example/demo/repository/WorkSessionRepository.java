package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkSession;
import com.example.demo.entity.WorkStatus;


public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {

    List<WorkSession> findByJobPost(JobPost jobPost);
    List<WorkSession> findByWorkDate(String workDate);
    List<WorkSession> findByJobPostAndWorkDate(JobPost jobPost, String workDate);
    List<WorkSession> findByStatusAndWorkDate(WorkStatus status, String workDate);
    List<WorkSession> findByJobPostUserAndDeletedAtIsNull(User user);
    List<WorkSession> findByIdInAndDeletedAtIsNull(List<Long> ids);
}