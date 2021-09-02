package com.unityTest.testrunner.models.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Models the response to a file upload event
 */
@Data
@ApiModel(value = "FileUpload", description = "Models an upload of a code file")
public class FileUploadEvent {

	@ApiModelProperty(value = "Upload time", required = true)
	private Date uploadTime;

	@ApiModelProperty(value = "List of files uploaded", required = true)
	private List<FileInfo> filesUploaded;

	protected FileUploadEvent(Date uploadTime) {
		this.uploadTime = uploadTime;
		this.filesUploaded = new ArrayList<>();
	}

	public FileUploadEvent() {
		this(new Date());
	}

	public void addFile(String name, long size) {
		this.filesUploaded.add(new FileInfo(name, size));
	}
}
