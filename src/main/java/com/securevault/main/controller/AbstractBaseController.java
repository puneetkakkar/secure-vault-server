package com.securevault.main.controller;

import java.util.Arrays;

import com.securevault.main.exception.BadRequestException;
import com.securevault.main.service.MessageSourceService;

public abstract class AbstractBaseController {

	/**
	 * Sort column check
	 * 
	 * @param messageSourceService MessageSourceService
	 * @param sortColumns          String[]
	 * @param sortBy               String
	 */
	protected void sortColumnCheck(final MessageSourceService messageSourceService, final String[] sortColumns,
			final String sortBy) {
		if (sortBy != null && !Arrays.asList(sortColumns).contains(sortBy)) {
			throw new BadRequestException(messageSourceService.get("invalid_sort_column"));
		}
	}

}
