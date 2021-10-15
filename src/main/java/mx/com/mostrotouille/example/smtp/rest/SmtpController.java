package mx.com.mostrotouille.example.smtp.rest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import mx.com.mostrotouille.example.smtp.dto.Mail;
import mx.com.mostrotouille.example.smtp.dto.Response;

@RestController
@RequestMapping("/smtp")
@Slf4j
public class SmtpController {
	@Value(value = "${mail.from}")
	private String from;

	@Value(value = "${mail.from.password}")
	private String fromPassword;

	private Mail.Sender createTestSender() {
		final HashMap<String, String> properties = new HashMap<>();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");

		return Mail.Sender.builder().properties(properties).host("outlook.office365.com").port("587").username(from)
				.password(fromPassword).build();
	}

	@PostMapping(value = "/send", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Response> send(@RequestBody Mail mail) {
		try {
			mail.setSender(createTestSender());

			final Mail.Sender sender = mail.getSender();

			final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
			mailSender.setHost(sender.getHost());
			mailSender.setPort(Integer.valueOf(sender.getPort()));
			mailSender.setUsername(sender.getUsername());
			mailSender.setPassword(sender.getPassword());

			final Properties properties = mailSender.getJavaMailProperties();

			if (sender.getProperties() != null) {
				for (final Map.Entry<String, String> entry : sender.getProperties().entrySet()) {
					properties.put(entry.getKey(), entry.getValue());
				}
			}

			final MimeMessage message = mailSender.createMimeMessage();

			final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(sender.getUsername());
			helper.setSubject(mail.getSubject());
			helper.setTo(
					mail.getTo() != null ? ((String[]) mail.getTo().toArray(new String[mail.getTo().size()])) : null);
			helper.setText(mail.getContent(), true);

			if (mail.getCc() != null) {
				helper.setCc((String[]) mail.getCc().toArray(new String[mail.getCc().size()]));
			}

			if (mail.getBcc() != null) {
				helper.setBcc((String[]) mail.getBcc().toArray(new String[mail.getBcc().size()]));
			}

			final List<Mail.EmbeddedImage> embeddedImage = mail.getEmbeddedImage();

			if (embeddedImage != null) {
				for (Mail.EmbeddedImage item : embeddedImage) {
					helper.addInline(item.getKey(),
							new ByteArrayResource(Base64.getDecoder().decode(item.getContent())),
							item.getContentType());
				}
			}

			final List<Mail.Attachment> attachment = mail.getAttachment();

			if (attachment != null) {
				for (Mail.Attachment item : attachment) {
					helper.addAttachment(item.getFilename(),
							new ByteArrayResource(Base64.getDecoder().decode(item.getContent())));
				}
			}

			mailSender.send(message);

			final Response response = Response.builder().status(HttpStatus.OK).timestamp(LocalDateTime.now()).build();

			return new ResponseEntity<>(response, response.getStatus());
		} catch (Exception ex) {
			final String errorMessage = "Send message by SMTP failed.";

			log.error(errorMessage, ex);

			final Response response = Response.builder().status(HttpStatus.BAD_REQUEST).timestamp(LocalDateTime.now())
					.message(errorMessage).errors(Arrays.asList(ex.getMessage())).build();

			return new ResponseEntity<>(response, response.getStatus());
		}
	}
}