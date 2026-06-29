package com.smartexpense.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.smartexpense.model.enums.SettlementMode;

class SettlementAlgorithmTest {

	private static MemberBalance member(String userId, String userName, double contribution, double consumption) {
		return member(userId, userName, contribution, consumption, 0.0);
	}

	private static MemberBalance member(String userId, String userName, double contribution, double consumption,
			double externalPaid) {
		return MemberBalance.builder()
				.userId(userId)
				.userName(userName)
				.contribution(contribution)
				.consumption(consumption)
				.externalPaid(externalPaid)
				.build();
	}

	@Test
	void testCase1_equalParticipation_balanced() {
		List<MemberBalance> members = List.of(
				member("a", "Alice", 1000, 1000),
				member("b", "Bob", 1000, 1000),
				member("c", "Carol", 1000, 1000));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.BALANCED, result.getMode());
		assertEquals(0, result.getTotalTransfers());
		for (MemberBalance balance : result.getMemberBalances()) {
			assertEquals(0.0, balance.getFinalBalance(), 0.01);
		}
	}

	@Test
	void testCase2_simpleTwoPersonDebt() {
		List<MemberBalance> members = List.of(
				member("alice", "Alice", 1000, 500),
				member("bob", "Bob", 1000, 1500));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.BALANCED, result.getMode());
		assertEquals(1, result.getTotalTransfers());
		TransferInstruction transfer = result.getTransferInstructions().get(0);
		assertEquals("bob", transfer.getFromUserId());
		assertEquals("alice", transfer.getToUserId());
		assertEquals(500.0, transfer.getAmount(), 0.01);
	}

	@Test
	void testCase3_underspendRefundMode() {
		List<MemberBalance> members = List.of(
				member("a", "A", 3000, 2000),
				member("b", "B", 3000, 2000),
				member("c", "C", 3000, 2000));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.REFUND, result.getMode());
		assertEquals(0, result.getTotalTransfers());
		assertEquals(3000.0, result.getSurplus(), 0.01);
		for (MemberBalance balance : result.getMemberBalances()) {
			assertEquals(1000.0, balance.getFinalBalance(), 0.01);
		}
	}

	@Test
	void testCase4_overspendDeficitMode() {
		List<MemberBalance> members = List.of(
				member("a", "A", 2000, 1000),
				member("b", "B", 2000, 2000),
				member("c", "C", 2000, 4000));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.DEFICIT, result.getMode());
		assertEquals(1, result.getTotalTransfers());
		TransferInstruction transfer = result.getTransferInstructions().get(0);
		assertEquals("c", transfer.getFromUserId());
		assertEquals("a", transfer.getToUserId());
		assertEquals(1000.0, transfer.getAmount(), 0.01);
	}

	@Test
	void testCase5_selectiveParticipation() {
		List<MemberBalance> members = List.of(
				member("a", "A", 2500, 1500),
				member("b", "B", 2500, 2500),
				member("c", "C", 2500, 2000),
				member("d", "D", 2500, 2000));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.REFUND, result.getMode());
		assertEquals(0, result.getTotalTransfers());
		assertEquals(8000.0, result.getTotalConsumption(), 0.01);
		assertEquals(2000.0, result.getSurplus(), 0.01);
	}

	@Test
	void testCase6_externalPaymentAdjustment() {
		List<MemberBalance> members = List.of(
				member("alice", "Alice", 5000, 4000, 0),
				member("bob", "Bob", 5000, 4000, 2000));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.REFUND, result.getMode());
		assertEquals(0, result.getTotalTransfers());
		assertEquals(1000.0, findBalance(result, "alice"), 0.01);
		assertEquals(3000.0, findBalance(result, "bob"), 0.01);
	}

	@Test
	void testCase7_zeroConsumptionMember() {
		List<MemberBalance> members = List.of(
				member("a", "A", 3000, 4000),
				member("b", "B", 3000, 4000),
				member("c", "C", 3000, 0));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.REFUND, result.getMode());
		assertEquals(2, result.getTotalTransfers());
		assertTransfer(result, "a", "c", 1000);
		assertTransfer(result, "b", "c", 1000);
	}

	@Test
	void testCase8_multiPersonDebtChain() {
		List<MemberBalance> members = List.of(
				member("a", "A", 0, 1000),
				member("b", "B", 3000, 1000),
				member("c", "C", 0, 1500),
				member("d", "D", 1000, 500));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.BALANCED, result.getMode());
		assertEquals(3, result.getTotalTransfers());
		assertTransfer(result, "c", "b", 1500);
		assertTransfer(result, "a", "b", 500);
		assertTransfer(result, "a", "d", 500);
	}

	@Test
	void testCase9_singleMemberTrip() {
		List<MemberBalance> members = List.of(
				member("solo", "Solo", 5000, 3000));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertEquals(SettlementMode.REFUND, result.getMode());
		assertEquals(0, result.getTotalTransfers());
		assertEquals(2000.0, result.getMemberBalances().get(0).getFinalBalance(), 0.01);
	}

	@Test
	void testCase10_floatingPointPrecision() {
		List<MemberBalance> members = List.of(
				member("a", "A", 1000, 33.33),
				member("b", "B", 1000, 33.33),
				member("c", "C", 1000, 33.33));

		SettlementResult result = SettlementAlgorithm.calculate(members);

		assertNotNull(result);
		assertEquals(SettlementMode.REFUND, result.getMode());
		assertEquals(0, result.getTotalTransfers());
		for (MemberBalance balance : result.getMemberBalances()) {
			assertEquals(966.67, balance.getFinalBalance(), 0.01);
		}
	}

	private static double findBalance(SettlementResult result, String userId) {
		return result.getMemberBalances().stream()
				.filter(m -> userId.equals(m.getUserId()))
				.findFirst()
				.orElseThrow()
				.getFinalBalance();
	}

	private static void assertTransfer(SettlementResult result, String fromUserId, String toUserId, double amount) {
		boolean found = result.getTransferInstructions().stream()
				.anyMatch(t -> fromUserId.equals(t.getFromUserId())
						&& toUserId.equals(t.getToUserId())
						&& Math.abs(amount - t.getAmount()) < 0.01);
		assertEquals(true, found);
	}
}
