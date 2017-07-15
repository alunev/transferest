package org.alunev.transferest.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.alunev.transferest.model.Account;
import org.alunev.transferest.model.User;
import org.alunev.transferest.model.error.TransferException;
import org.alunev.transferest.service.dbstore.AccountService;
import org.alunev.transferest.service.dbstore.UserService;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Random;

public class GenerationService {
    private final UserService userService;
    private final AccountService accountService;

    @Inject
    public GenerationService(UserService userService,
                             AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    public List<String> generateSomeUsersWithAccounts() throws TransferException {
        List<String> names = Lists.newArrayList("Bill", "Novella", "Mina", "Ed", "Niki",
                "Kieth", "Babette", "Edgar", "Detra", "Oliver"
        );

        names.forEach(name -> userService.save(User.withName(name)));

        for (User user : userService.getAll()) {
            Random random = new Random();

            int bound = random.nextInt(5);
            for (int value = 0; value < bound; value++) {
                accountService.save(
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
