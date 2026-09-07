#!/usr/bin/env bash

# 아래는 현재 Spring Boot 멀티모듈 프로젝트의 구조와 Gradle/configuration 설정이다. 이 구조를 기준으로 답변해줘.
set -e

OUTPUT="project-context.txt"

{
    echo "============================================================"
    echo " GRADLE MODULES"
    echo "============================================================"
    echo

    ./gradlew projects

    echo
    echo
    echo "============================================================"
    echo " FILESYSTEM STRUCTURE"
    echo "============================================================"
    echo

    tree -L 4 \
        -I 'build|.gradle|.git|.idea|out|target|node_modules'

    echo
    echo
    echo "============================================================"
    echo " MODULE CONFIGURATION"
    echo "============================================================"
    echo

    find . \
        -type f \
        \( \
            -name "build.gradle" \
            -o -name "build.gradle.kts" \
            -o -name "application.yml" \
            -o -name "application.yaml" \
            -o -name "application.properties" \
            -o -name "application-*.yml" \
            -o -name "application-*.yaml" \
            -o -name "application-*.properties" \
        \) \
        -not -path "*/build/*" \
        -not -path "*/.gradle/*" \
        -not -path "*/.git/*" \
        | sort \
        | while read -r file; do

            echo
            echo "------------------------------------------------------------"
            echo " FILE: $file"
            echo "------------------------------------------------------------"
            cat "$file"
            echo

        done

} > "$OUTPUT"

echo "Generated: $OUTPUT"