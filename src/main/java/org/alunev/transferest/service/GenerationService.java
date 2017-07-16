package org.alunev.transferest.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.Transaction;
import org.alunev.transferest.model.TransactionRequest;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.TransactionService;
import org.alunev.transferest.service.dbstore.UserService;
import org.alunev.transferest.service.processor.TransactionProcessor;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Random;

@Slf4j
public class GenerationService {
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionProcessor transactionProcessor;
    private final TransactionService transactionService;

    @Inject
    public GenerationService(UserService userService,
                             AccountService accountService,
                             TransactionProcessor transactionProcessor,
                             TransactionService transactionService) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionProcessor = transactionProcessor;
        this.transactionService = transactionService;
    }

    public List<String> generateSomeUsersWithAccounts() throws TransferException {
        List<String> names = Lists.newArrayList("Bill", "Novella", "Mina", "Ed", "Niki",
                "Kieth", "Babette", "Edgar", "Detra", "Oliver"
        );

        names.forEach(name -> userService.create(User.withName(name)));

        for (User user : userService.getAll()) {
            Random random = new Random();

            int bound = random.nextInt(5);
            for (int value = 0; value < bound; value++) {
                accountService.create(
                        Account.builder()
                                .ownerId(user.getId())
                                .number(generateNumber())
                                .currency(generateCcy())
                                .balance(generateBalance())
                                .build());
            }
        }

        return names;
    }

    public void generateSomeTransactions() {
        List<User> users = userService.getAll();
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            User fromUser = users.get(random.nextInt(users.size()));
            List<Account> fromAccounts = accountService.getByUserId(fromUser.getId());

            if (fromAccounts.isEmpty()) {
                continue;
            }

            User toUser = users.get(random.nextInt(users.size()));
            try {
                transactionProcessor.process(TransactionRequest.builder()
                        .senderName(fromUser.getName())
                        .receiverName(toUser.getName())
                        .amount(getRandomAmount())
                        .currency(Currency.getInstance(getRandomCurrency(fromAccounts)))
                        .build());
            } catch (TransferException e) {
                // almost ignore
                log.debug("Failed to create simulation tx", e);
            }
//
//            toUser = users.get(random.nextInt(users.size()));
//            List<Account> toAccounts = accountService.getByUserId(toUser.getId());
//            if (toAccounts.isEmpty()) {
//                continue;
//            }
//
//            transactionService.save(Transaction.builder()
//                    .senderAccId(fromAccounts.get(random.nextInt(fromAccounts.size())).getId())
//                    .receiverAccId(toAccounts.get(random.nextInt(toAccounts.size())).getId())
//                    .sendAmount(getRandomAmount())
//                    .receiveAmount(getRandomAmount())
//                    .build());
        }
    }

    private String getRandomCurrency(List<Account> accounts) {
        return accounts.get(new Random().nextInt(accounts.size())).getCurrency();
    }

    private BigDecimal getRandomAmount() {
        return BigDecimal.valueOf(new Random().nextInt(10000), 2);
    }

    private BigDecimal generateBalance() {
        Random random = new Random();

        return BigDecimal.valueOf(random.nextInt(1000000), 2);
    }

    private String generateNumber() {
        Random random = new Random();
        return String.format("%05d-%05d", random.nextInt(10000), random.nextInt(10000));
    }

    private String generateCcy() {
        Random random = new Random();

        Currency[] ccys = new Currency[]{Currency.getInstance("USD"), Currency.getInstance("EUR"), Currency
                .getInstance("GBP"), Currency.getInstance("JPY"), Currency.getInstance("RUB")};

        return ccys[random.nextInt(ccys.length - 1)].getCurrencyCode();
    }
}
