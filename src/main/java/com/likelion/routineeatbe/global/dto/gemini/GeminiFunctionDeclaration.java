package com.likelion.routineeatbe.global.dto.gemini;

/**
 * Gemini Function Declaration DTO가 공통으로 제공해야 하는 속성을 정의합니다.
 */
public interface GeminiFunctionDeclaration {

    String type();

    String name();

    String description();

    Object parameters();
}
