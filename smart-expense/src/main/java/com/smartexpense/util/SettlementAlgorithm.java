package com.smartexpense.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.smartexpense.model.enums.SettlementMode;

public final class SettlementAlgorithm {

	private static final double TOLERANCE = 0.01;

	private SettlementAlgorithm() {
	}

	public static SettlementResult calculate(List<MemberBalance> members) {
		List<MemberBalance> memberBalances = new ArrayList<>();
		double totalContribution = 0.0;
		double totalConsumption = 0.0;

		for (MemberBalance member : members) {
			double contribution = nullToZero(member.getContribution());
			double consumption = nullToZero(member.getConsumption());
			double externalPaid = nullToZero(member.getExternalPaid());
			double finalBalance = round2(contribution + externalPaid - consumption);

			memberBalances.add(MemberBalance.builder()
					.userId(member.getUserId())
					.userName(member.getUserName())
					.contribution(contribution)
					.consumption(consumption)
					.externalPaid(externalPaid)
					.finalBalance(finalBalance)
					.build());

			totalContribution += contribution;
			totalConsumption += consumption;
		}

		totalContribution = round2(totalContribution);
		totalConsumption = round2(totalConsumption);
		double surplus = round2(totalContribution - totalConsumption);

		SettlementMode mode;
		if (Math.abs(surplus) <= TOLERANCE) {
			mode = SettlementMode.BALANCED;
		} else if (surplus > 0) {
			mode = SettlementMode.REFUND;
		} else {
			mode = SettlementMode.DEFICIT;
		}

		List<MemberBalance> creditors = new ArrayList<>();
		List<MemberBalance> debtors = new ArrayList<>();

		for (MemberBalance member : memberBalances) {
			if (member.getFinalBalance() > TOLERANCE) {
				creditors.add(copyMember(member));
			} else if (member.getFinalBalance() < -TOLERANCE) {
				debtors.add(copyMember(member));
			}
		}

		creditors.sort(Comparator.comparing(MemberBalance::getFinalBalance).reversed());
		debtors.sort(Comparator.comparing(MemberBalance::getFinalBalance));

		List<TransferInstruction> transferInstructions = new ArrayList<>();
		int i = 0;
		int j = 0;

		while (i < debtors.size() && j < creditors.size()) {
			MemberBalance debtor = debtors.get(i);
			MemberBalance creditor = creditors.get(j);

			double transferAmount = round2(Math.min(
					Math.abs(debtor.getFinalBalance()),
					creditor.getFinalBalance()));

			if (transferAmount > TOLERANCE) {
				transferInstructions.add(TransferInstruction.builder()
						.fromUserId(debtor.getUserId())
						.fromUserName(debtor.getUserName())
						.toUserId(creditor.getUserId())
						.toUserName(creditor.getUserName())
						.amount(transferAmount)
						.build());

				debtor.setFinalBalance(round2(debtor.getFinalBalance() + transferAmount));
				creditor.setFinalBalance(round2(creditor.getFinalBalance() - transferAmount));
			}

			if (Math.abs(debtor.getFinalBalance()) <= TOLERANCE) {
				i++;
			}
			if (Math.abs(creditor.getFinalBalance()) <= TOLERANCE) {
				j++;
			}
		}

		for (MemberBalance member : memberBalances) {
			member.setFinalBalance(round2(member.getFinalBalance()));
		}

		return SettlementResult.builder()
				.memberBalances(memberBalances)
				.transferInstructions(transferInstructions)
				.mode(mode)
				.totalContribution(totalContribution)
				.totalConsumption(totalConsumption)
				.surplus(surplus)
				.totalTransfers(transferInstructions.size())
				.build();
	}

	private static MemberBalance copyMember(MemberBalance member) {
		return MemberBalance.builder()
				.userId(member.getUserId())
				.userName(member.getUserName())
				.contribution(member.getContribution())
				.consumption(member.getConsumption())
				.externalPaid(member.getExternalPaid())
				.finalBalance(member.getFinalBalance())
				.build();
	}

	private static double nullToZero(Double value) {
		return value != null ? value : 0.0;
	}

	private static double round2(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
