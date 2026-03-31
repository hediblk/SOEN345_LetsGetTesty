package com.letsgettesty.backend.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthRepository authRepository;
    private final JwtService jwtService;

    public AuthService(AuthRepository authRepository, JwtService jwtService) {
        this.authRepository = authRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String fullName = normalize(request.fullName());
        if (fullName == null) {
            throw badRequest("Full name is required.");
        }
        if (request.password() == null) {
            throw badRequest("Password is required.");
        }

        AccountRole role = resolveRole(request.role());
        if (role != AccountRole.USER) {
            throw badRequest("Only customer accounts can be registered here.");
        }
        ResolvedContact resolvedContact = resolveRegistrationContact(request);

        if (authRepository.existsByFullName(fullName)) {
            throw conflict("An account already exists with that name.");
        }
        if (resolvedContact.email() != null && authRepository.existsByEmail(resolvedContact.email())) {
            throw conflict("An account already exists with that email.");
        }
        if (resolvedContact.phone() != null && authRepository.existsByPhone(resolvedContact.phone())) {
            throw conflict("An account already exists with that phone number.");
        }

        AccountRecord account = authRepository.createAccount(
                AccountRole.USER,
                fullName,
                request.password(),
                resolvedContact.email(),
                resolvedContact.phone());
        return AuthResponse.from(account, jwtService.generateToken(account));
    }

    public AuthResponse login(LoginRequest request) {
        return loginForRole(request, AccountRole.USER);
    }

    public AuthResponse loginAdmin(LoginRequest request) {
        return loginForRole(request, AccountRole.ADMIN);
    }

    private AuthResponse loginForRole(LoginRequest request, AccountRole role) {
        if (request.password() == null) {
            throw badRequest("Password is required.");
        }

        String loginContact = resolveLoginContact(request);
        List<AccountRecord> matches = role == AccountRole.ADMIN
                ? authRepository.findAdminsByLoginContact(loginContact)
                : authRepository.findUsersByLoginContact(loginContact);

        if (matches.isEmpty()) {
            throw unauthorized("No account found for the provided login.");
        }
        if (matches.size() > 1) {
            throw conflict("More than one account uses that login.");
        }

        AccountRecord account = matches.get(0);
        if (!Objects.equals(account.password(), request.password())) {
            throw unauthorized("Invalid password.");
        }

        return AuthResponse.from(account, jwtService.generateToken(account));
    }

    private AccountRole resolveRole(String rawRole) {
        String normalizedRole = normalize(rawRole);
        if (normalizedRole == null) {
            return AccountRole.USER;
        }

        try {
            return AccountRole.valueOf(normalizedRole.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw badRequest("Role must be USER or ADMIN.");
        }
    }

    private ResolvedContact resolveRegistrationContact(RegisterRequest request) {
        String email = normalize(request.email());
        String phone = normalize(request.phone());
        String contact = normalize(request.contact());
        String contactType = normalize(request.contactType());

        if (contact != null) {
            if (contactType == null) {
                if (email == null && phone == null) {
                    if (looksLikeEmail(contact)) {
                        email = contact;
                    } else {
                        phone = contact;
                    }
                } else {
                    throw badRequest("Contact type is required when using the generic contact field.");
                }
            } else if ("EMAIL".equals(contactType.toUpperCase(Locale.ROOT))) {
                email = merge(email, contact, true, "email");
            } else if ("PHONE".equals(contactType.toUpperCase(Locale.ROOT))) {
                phone = merge(phone, contact, false, "phone");
            } else {
                throw badRequest("Contact type must be EMAIL or PHONE.");
            }
        }

        int contactCount = 0;
        if (email != null) {
            contactCount++;
        }
        if (phone != null) {
            contactCount++;
        }

        if (contactCount != 1) {
            throw badRequest("Provide either email or phone, but not both.");
        }

        return new ResolvedContact(email, phone);
    }

    private String resolveLoginContact(LoginRequest request) {
        List<String> candidates = new ArrayList<>();
        addIfPresent(candidates, normalize(request.contact()));
        addIfPresent(candidates, normalize(request.email()));
        addIfPresent(candidates, normalize(request.phone()));

        if (candidates.isEmpty()) {
            throw badRequest("An email or phone number is required to log in.");
        }

        String firstCandidate = candidates.get(0);
        for (String candidate : candidates) {
            if (!equalsIgnoreCase(candidate, firstCandidate)) {
                throw badRequest("Use exactly one email or phone to log in.");
            }
        }

        return firstCandidate;
    }

    private void addIfPresent(List<String> values, String candidate) {
        if (candidate == null) {
            return;
        }
        for (String value : values) {
            if (equalsIgnoreCase(value, candidate)) {
                return;
            }
        }
        values.add(candidate);
    }

    private String merge(String existingValue, String incomingValue, boolean ignoreCase, String label) {
        if (existingValue == null) {
            return incomingValue;
        }

        boolean sameValue = ignoreCase
                ? existingValue.equalsIgnoreCase(incomingValue)
                : existingValue.equals(incomingValue);

        if (!sameValue) {
            throw badRequest("Conflicting " + label + " values were provided.");
        }

        return existingValue;
    }

    private boolean looksLikeEmail(String value) {
        return value.contains("@");
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left.equalsIgnoreCase(right);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private record ResolvedContact(String email, String phone) {
    }
}
