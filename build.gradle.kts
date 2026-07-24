plugins {
    kotlin("jvm") version "2.3.21"
    // (1) spring 애노테이션이 붙은 클래스를 컴파일 시점에 자동으로 open으로 변경
    //  - kotlin은 모든 클래스/메소드에 대해 기본이 final 선언
    //  - spring @Transactional 애노테이션은 상속된 자식 클래스까지 런타임시에 로드해서 tx 범위를 유지 (final이면 불가능)
    //  - 이러한 spring <-> kotlin 간의 간극을 메우고자 plugin.spring 필요
    kotlin("plugin.spring") version "2.3.21"
    // (2) @Entity 애노테이션이 붙은 클래스의 파라미터 없는 기본 생성자를 컴파일 시점에 자동으로 생성
    //  - jpa는 DB의 데이터를 읽어올때 리플렉션으로 엔티티의 기본 생성자를 호출해서 엔티티 객체를 생성하고, DB 행 값을 하나씩 주입
    //  - java는 컴파일 시점에 컴파일러가 자동으로 기본 생성자를 만들어주지만, kotlin은 그렇지 X
    //  - 이러한 jpa <-> kotlin 간의 간극을 메우고자 plugin.jpa 필요
    kotlin("plugin.jpa") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.hanati"
version = "0.0.1-SNAPSHOT"
description = "demo-publisher"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// JPA 엔티티: plugin.jpa가 no-arg 생성자, allOpen이 lazy proxy용 open 처리

// (3) @Entity 애노테이션이 붙은 클래스를 컴파일 시점에 자동으로 open으로 변경
//  - jpa 구현체 hibernate는 지연로딩시, 연관 엔티티를 프록시로 가져와서 꽂아두고 이후 실제 해당 엔티티 객체의 값이 필요하면 그제서야 조회해옴
//  - kotlin은 final이 기본이기 때문에 open 하지 않으면 프록시로 지연로딩 엔티티를 가져올 수 X
//  - 이러한 jpa <-> kotlin 간의 간극을 메우고자 plugin.jpa 필요
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
