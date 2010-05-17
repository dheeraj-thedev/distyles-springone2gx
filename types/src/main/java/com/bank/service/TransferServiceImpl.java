package com.bank.service;

import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Autowired;

import com.bank.domain.Account;
import com.bank.domain.InsufficientFundsException;
import com.bank.domain.TransferConfirmation;
import com.bank.repository.AccountRepository;


public class TransferServiceImpl implements TransferService {
	
	private final AccountRepository accountRepository;
	private final FeePolicy feePolicy;
	private double minimumTransferAmount = 1.00;
	
	@Autowired
	public TransferServiceImpl(AccountRepository accountRepository, FeePolicy feePolicy) {
		this.accountRepository = accountRepository;
		this.feePolicy = feePolicy;
	}

	@Override
	public TransferConfirmation transfer(double amount, String srcAcctId, String dstAcctId) throws InsufficientFundsException {
		if (amount < minimumTransferAmount)
			throw new IllegalArgumentException(
					format("transfer amount must be at least $%.2f USD", minimumTransferAmount));
		
		Account srcAcct = accountRepository.findById(srcAcctId);
		Account dstAcct = accountRepository.findById(dstAcctId);
		
		double fee = feePolicy.calculateFee(amount);
		if (fee > 0)
			srcAcct.debit(fee);
		
		srcAcct.debit(amount);
		dstAcct.credit(amount);
		
		accountRepository.updateBalance(srcAcct);
		accountRepository.updateBalance(dstAcct);

		return new TransferConfirmation(amount, srcAcct, dstAcct);
	}

	@Override
	public void setMinimumTransferAmount(double minimumTransferAmount) {
		this.minimumTransferAmount = minimumTransferAmount;
	}
}
