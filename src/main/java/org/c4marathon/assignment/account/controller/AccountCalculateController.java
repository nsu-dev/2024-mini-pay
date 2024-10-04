package org.c4marathon.assignment.account.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class AccountCalculateController {
	// 각 사용자의 SSE 연결을 관리할 Map (userId를 키로 사용)
	private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

	// 사용자가 SSE를 통해 구독을 설정할 수 있도록 하는 엔드포인트
	@GetMapping("/account/subscribe")
	public SseEmitter subscribe(@RequestParam String userId) {
		SseEmitter emitter = new SseEmitter(0L); // 타임아웃 없음
		sseEmitters.put(userId, emitter);

		// SSE 연결이 완료되면 Map에서 제거
		emitter.onCompletion(() -> sseEmitters.remove(userId));
		emitter.onTimeout(() -> sseEmitters.remove(userId));

		return emitter;
	}

	// 특정 사용자에게 SSE 이벤트 전송
	public void sendEventToClient(String userId, String eventName, String data) {
		SseEmitter emitter = sseEmitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.name(eventName)
					.data(data));
			} catch (IOException e) {
				sseEmitters.remove(userId); // 전송 실패 시 Map에서 제거
			}
		}
	}
}
