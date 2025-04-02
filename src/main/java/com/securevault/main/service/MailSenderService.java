package com.securevault.main.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.securevault.main.entity.User;
import com.securevault.main.exception.EmailSendingException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailSenderService {
	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String LOGO_URL = "logoUrl";
	private static final String EMAIL_TEMPLATE = "mail/user-email-verification";
	private static final String EMAIL_VERIFICATION_PATH = "/auth/finish-signup/";
	private static final String LOGO_IMAGE_PATH = "static/images/secure-vault-logo-1x.png";
	private static final String LOGO_CID = "logo";

	private final String appName;
	private final String appUrl;
	private final String frontendUrl;
	private final String senderAddress;
	private final MessageSourceService messageSourceService;
	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;

	/**
	 * Mail sender service constructor.
	 *
	 * @param appName              String name of the application name
	 * @param appUrl               String url of the application url
	 * @param frontendUrl          String url of the frontend url
	 * @param senderAddress        String email address of the sender
	 * @param messageSourceService MessageSourceService
	 * @param mailSender           JavaMailSender
	 * @param templateEngine       SpringTemplateEngine
	 */
	public MailSenderService(
			@Value("${spring.application.name}") String appName,
			@Value("${app.url}") String appUrl,
			@Value("${app.frontend-url}") String frontendUrl,
			@Value("${spring.mail.username}") String senderAddress,
			MessageSourceService messageSourceService,
			JavaMailSender mailSender,
			SpringTemplateEngine templateEngine) {
		this.appName = appName;
		this.appUrl = appUrl;
		this.frontendUrl = frontendUrl;
		this.senderAddress = senderAddress;
		this.messageSourceService = messageSourceService;
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	/**
	 * Send user email verification link.
	 *
	 * @param user User to send verification email to
	 * @throws EmailSendingException if email sending fails
	 */
	public void sendUserEmailVerification(User user) {
		if (user == null || user.getEmail() == null || user.getEmailVerificationToken() == null) {
			throw new IllegalArgumentException("User, email, or verification token is null");
		}

		try {
			log.info("Preparing to send verification email to: {}", user.getEmail());
			String verificationUrl = buildVerificationUrl(user.getEmailVerificationToken().getToken(), user);
			Context emailContext = createEmailContext(user.getName(), verificationUrl);
			String subject = messageSourceService.get("email_verification_subject");
			sendEmail(user, subject, emailContext);
			log.info("Verification email sent successfully to: {}", user.getEmail());
		} catch (Exception e) {
			log.error("Failed to send verification email to: {}", user.getEmail(), e);
			throw new EmailSendingException(messageSourceService.get("email_verification_failed"), e);
		}
	}

	/**
	 * Build the verification URL for the email
	 */
	private String buildVerificationUrl(String token, User user) {
		return String.format("%s%s?token=%s&email=%s",
				frontendUrl,
				EMAIL_VERIFICATION_PATH,
				token,
				user.getEmail());
	}

	/**
	 * Create context for email template
	 */
	private Context createEmailContext(String userName, String verificationUrl) {
		Context context = new Context(LocaleContextHolder.getLocale());
		context.setVariable(NAME, userName);
		context.setVariable(URL, verificationUrl);
		context.setVariable("SENDER_ADDRESS", senderAddress);
		context.setVariable("APP_NAME", appName);
		context.setVariable("APP_URL", appUrl);
		context.setVariable("FRONTEND_URL", frontendUrl);
		context.setVariable(LOGO_URL, "cid:" + LOGO_CID);
		return context;
	}

	/**
	 * Send the email
	 */
	private void sendEmail(User user, String subject, Context context)
			throws MessagingException, MailException, UnsupportedEncodingException {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		helper.setFrom(new InternetAddress(senderAddress, appName));
		helper.setTo(new InternetAddress(user.getEmail(), user.getName()));
		helper.setSubject(subject);
		helper.setText(templateEngine.process(EMAIL_TEMPLATE, context), true);

		try {
			ClassPathResource logoResource = new ClassPathResource(LOGO_IMAGE_PATH);
			helper.addInline(LOGO_CID, logoResource);
		} catch (Exception e) {
			log.error("Failed to add logo image: {}", e.getMessage());
		}

		mailSender.send(mimeMessage);
	}
}
