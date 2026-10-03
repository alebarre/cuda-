package br.com.cuidamais.notification;

import br.com.cuidamais.auth.OtpPurpose;

/**
 * Código OTP de 6 dígitos: confirmação de e-mail (AC-001.1, AC-005) ou redefinição de senha
 * (AC-010.1).
 */
public record ConfirmationCodeEmail(String to, String code, OtpPurpose purpose) implements EmailEvent {}
