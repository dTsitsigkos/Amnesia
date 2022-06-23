/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

/**
 *
 * @author jimakos
 */

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "spring.http.multipart", ignoreUnknownFields = false)
public class MultipartProperties {

	/**
	 * Enable support of multi-part uploads.
	 */
	private boolean enabled = true;

	/**
	 * Intermediate location of uploaded files.
	 */
	private String location;

	/**
	 * Max file size. Values can use the suffixed "MB" or "KB" to indicate a Megabyte or
	 * Kilobyte size.
	 */
	private DataSize maxFileSize = DataSize.parse("8Mb");

	/**
	 * Max request size. Values can use the suffixed "MB" or "KB" to indicate a Megabyte
	 * or Kilobyte size.
	 */
	private DataSize maxRequestSize = DataSize.parse("10Mb");

	/**
	 * Threshold after which files will be written to disk. Values can use the suffixed
	 * "MB" or "KB" to indicate a Megabyte or Kilobyte size.
	 */
	private DataSize fileSizeThreshold = DataSize.parse("0");

	/**
	 * Whether to resolve the multipart request lazily at the time of file or parameter
	 * access.
	 */
	private boolean resolveLazily = false;

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMaxFileSize() {
		return this.maxFileSize.toString();
	}

	public void setMaxFileSize(String maxFileSize) {
		this.maxFileSize = DataSize.parse(maxFileSize);
	}

	public String getMaxRequestSize() {
		return this.maxRequestSize.toString();
	}

	public void setMaxRequestSize(String maxRequestSize) {
		this.maxRequestSize = DataSize.parse(maxRequestSize);
	}

	public String getFileSizeThreshold() {
		return this.fileSizeThreshold.toString();
	}

	public void setFileSizeThreshold(String fileSizeThreshold) {
		this.fileSizeThreshold = DataSize.parse(fileSizeThreshold);
	}

	public boolean isResolveLazily() {
		return this.resolveLazily;
	}

	public void setResolveLazily(boolean resolveLazily) {
		this.resolveLazily = resolveLazily;
	}

	/**
	 * Create a new {@link MultipartConfigElement} using the properties.
	 * @return a new {@link MultipartConfigElement} configured using there properties
	 */
	public MultipartConfigElement createMultipartConfig() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		if (StringUtils.hasText(this.fileSizeThreshold.toString())) {
			factory.setFileSizeThreshold(this.fileSizeThreshold);
		}
		if (StringUtils.hasText(this.location)) {
			factory.setLocation(this.location);
		}
		if (StringUtils.hasText(this.maxRequestSize.toString())) {
			factory.setMaxRequestSize(this.maxRequestSize);
		}
		if (StringUtils.hasText(this.maxFileSize.toString())) {
			factory.setMaxFileSize(this.maxFileSize);
		}
		return factory.createMultipartConfig();
	}

}
