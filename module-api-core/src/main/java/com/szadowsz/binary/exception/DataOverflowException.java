/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szadowsz.binary.exception;

/**
 * Exception for overflow situation where more data is inserted/added than it is
 * allowed to handle.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class DataOverflowException extends RuntimeException {

    public DataOverflowException() {
    }

    public DataOverflowException(String message) {
        super(message);
    }

    public DataOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataOverflowException(Throwable cause) {
        super(cause);
    }

    public DataOverflowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
