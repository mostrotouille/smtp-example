package mx.com.mostrotouille.example.smtp.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Mail {
	@Builder
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Attachment {
		private String content;
		private String filename;
	}

	@Builder
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EmbeddedImage {
		private String content;
		private String contentType;
		private String key;
	}

	@Builder
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Sender {
		private String host;
		private String password;
		private String port;
		public Map<String, String> properties;
		private String username;
	}

	private List<Attachment> attachment;
	private List<String> bcc;
	private List<String> cc;
	private String content;
	private List<EmbeddedImage> embeddedImage;
	private Sender sender;
	private String subject;
	private List<String> to;
}