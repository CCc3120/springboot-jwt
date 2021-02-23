package com.bingo.jwt.exception;

import java.text.MessageFormat;

import com.bingo.jwt.util.ResultCode;

public class CustomException extends RuntimeException {
	// 错误代码
	ResultCode resultCode;

	public CustomException(ResultCode resultCode) {
		super(resultCode.message());
		this.resultCode = resultCode;
	}

	public CustomException(ResultCode resultCode, Object... args) {
		super(resultCode.message());
		String message = MessageFormat.format(resultCode.message(), args);
		resultCode.setMessage(message);
		this.resultCode = resultCode;
	}

	public ResultCode getResultCode() {
		return resultCode;
	}
}
