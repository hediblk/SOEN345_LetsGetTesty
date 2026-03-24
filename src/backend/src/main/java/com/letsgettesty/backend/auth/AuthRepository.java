package com.letsgettesty.backend.auth;

import java.util.List;

public interface AuthRepository {

    boolean existsByFullName(String fullName);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    AccountRecord createAccount(AccountRole role, String fullName, String password, String email, String phone);

    List<AccountRecord> findByLoginContact(String contact);
}
