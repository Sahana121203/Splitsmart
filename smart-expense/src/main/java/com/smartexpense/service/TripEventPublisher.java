package com.smartexpense.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.smartexpense.dto.websocket.EditApprovalEvent;
import com.smartexpense.dto.websocket.ExpenseEvent;
import com.smartexpense.dto.websocket.KittyUpdateEvent;
import com.smartexpense.dto.websocket.SettlementEvent;
import com.smartexpense.dto.websocket.TripStatusEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripEventPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public void publishKittyUpdate(KittyUpdateEvent event) {
		messagingTemplate.convertAndSend(
				"/topic/trips/" + event.getTripId() + "/kitty",
				event);
	}

	public void publishExpenseEvent(ExpenseEvent event) {
		messagingTemplate.convertAndSend(
				"/topic/trips/" + event.getTripId() + "/expenses",
				event);
	}

	public void publishStatusChange(TripStatusEvent event) {
		messagingTemplate.convertAndSend(
				"/topic/trips/" + event.getTripId() + "/status",
				event);
	}

	public void publishSettlementEvent(SettlementEvent event) {
		messagingTemplate.convertAndSend(
				"/topic/trips/" + event.getTripId() + "/settlement",
				event);
	}

	public void publishEditApprovalEvent(EditApprovalEvent event) {
		messagingTemplate.convertAndSend(
				"/topic/trips/" + event.getTripId() + "/edits",
				event);
	}
}
