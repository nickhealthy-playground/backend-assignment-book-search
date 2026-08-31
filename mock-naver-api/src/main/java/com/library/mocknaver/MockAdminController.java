package com.library.mocknaver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mock-admin")
public class MockAdminController {
    private final FailureSimulator failureSimulator;

    public MockAdminController(FailureSimulator failureSimulator) {
        this.failureSimulator = failureSimulator;
    }

    @PostMapping("/failure")
    public Map<String, Object> changeFailureMode(@RequestParam("mode") String mode,
                                                 @RequestParam(value = "delayMs", required = false) Long delayMs) {
        failureSimulator.change(FailureSimulator.Mode.valueOf(mode.toUpperCase()), delayMs);
        return failureSimulator.status();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return failureSimulator.status();
    }
}
