/**
 * Copyright 2016, RadiantBlue Technologies, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package piazza.services.query.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.PiazzaLogger;

public class GeometryParseException extends Exception {
	private static final long serialVersionUID = 1L;
	private final static Logger LOGGER = LoggerFactory.getLogger(GeometryParseException.class);
	
	public GeometryParseException() {
		super();
	}

	public GeometryParseException(String message, Throwable cause) {
		super(message, cause);
		String message1 = String.format("%s --- %s", message, cause.getMessage());
		LOGGER.error(message1, PiazzaLogger.ERROR);
	}

	public GeometryParseException(String message) {
		super(message);
		LOGGER.error(message, PiazzaLogger.ERROR);
	}

	public GeometryParseException(Throwable cause) {
		super(cause);
		LOGGER.error(cause.getMessage(), PiazzaLogger.ERROR);
	}
}