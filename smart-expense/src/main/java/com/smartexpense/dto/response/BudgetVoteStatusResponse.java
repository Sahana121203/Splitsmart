package com.smartexpense.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetVoteStatusResponse {

	private boolean hasCurrentUserVoted;
	private int voteCount;
	private int memberCount;
	private int quorumRequired;
	private boolean quorumReached;
	private boolean voteClosed;
	private LocalDateTime deadline;
	private String message;
}
