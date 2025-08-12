package com.example.k5_iot_springboot.service.impl;

import com.example.k5_iot_springboot.dto.B_Student.StudentCreateRequestDto;
import com.example.k5_iot_springboot.dto.B_Student.StudentResponseDto;
import com.example.k5_iot_springboot.dto.B_Student.StudentUpdateRequestDto;
import com.example.k5_iot_springboot.entity.B_Student;
import com.example.k5_iot_springboot.repository.B_StudentRepository;
import com.example.k5_iot_springboot.service.B_StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class B_StudentServiceImpl implements B_StudentService {

    private final B_StudentRepository studentRepository;

    // == 조회 계열 (GET) 은 Transactional 의 readOnly 옵션을 사용 ==
    //                      --> 데이터를 수정,삭제, 생성 하려고 하면 오류 발생
    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDto> getAllStudents() {

        return studentRepository.findAll()
                .stream()
              //  .map(student -> toDto(student))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto getstudentById(Long id) {
        B_Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 id 의 학생이 없습니다."));
        return toDto(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponseDto> filterStudentByName(String name) {

        return studentRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toDto)
                .toList();
    }
    // === 쓰기 계열 (POST, PUT, DELETE) 은 기본 @Transactional 사용
    @Override
    @Transactional
    public StudentResponseDto createStudent(StudentCreateRequestDto requestDto) {
        // 엔티티 생성
        B_Student student = new B_Student(
                requestDto.getName(),
                requestDto.getEmail()
        );
        B_Student saved = studentRepository.save(student);

        return toDto(saved);
    }

    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentUpdateRequestDto requestDto) {
        B_Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 id 의 학생이 없습니다."));

        student.setName(requestDto.getName());

        // student = studentRepository.save(student);

        // === 영속성 컨텍스트와 관리 == //
        // : @Transactional 의 변경 감지(Dirty Checking)로 위의 save 호출없이도 Update 반영됨
        // - save 호출 없이도 트랜잭션 종료시 수정 가능

        // cf) UPDATE 는 save 생략 가능 / CREATE 는 save 생략 불가

        // UPDATE 흐름
        // 1) findById() 로 엔티티 조회: 영속성 컨텍스트에 진입(1차 캐시되고, 관리 상태로 변경)
        // 2) field 변경: 관리 상태인 경우 JPA 는 엔티티의 스냅샷(조회 시점의 원본 값)을 몰래 보관
        //              >> 비교후 Dirty Checking(변경 감지)
        // 3) 데이터 변경 감지시: UPDATE SQL 을 만들어 실행
        // 4) 자동으로 SQL 쿼리 실행

        // *** CREATE - save 명시 필요

        return toDto(student);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        B_Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 id 의 학생이 없습니다."));

        studentRepository.delete(student);
    }



    // === Entity -> DTO 매핑 유틸 메서드
    private StudentResponseDto toDto(B_Student student) {
//        return new StudentResponseDto(
//                student.getId(), student.getName()
//        );
        return StudentResponseDto.builder()
                .id(student.getId())
                .name(student.getName())
                .build();
        // 두 방법 중 암꺼나 사용
    }
}
