/*
 * Copyright (C) 2026 by Fonoster Inc (https://fonoster.com)
 * http://github.com/fonoster/routr
 *
 * This file is part of Routr.
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    https://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.routr.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default uncaught exception handler for threads.
 * This is particularly important for catching StackOverflowError in JAIN-SIP
 * internal threads (like EventScannerThread) that can occur during WSS
 * connection establishment due to recursion in the JAIN-SIP library.
 */
public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
  private static final Logger LOG = LogManager.getLogger(ThreadExceptionHandler.class);

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    if (throwable instanceof StackOverflowError) {
      String threadName = thread.getName();
      LOG.error(
          "StackOverflowError in thread \"{}\" - likely caused by WSS connection recursion in JAIN-SIP library. " +
          "This may indicate a bug in the JAIN-SIP library's NioTlsWebSocketMessageChannel implementation. " +
          "The thread will continue running but the operation that caused the error may have failed.",
          threadName,
          throwable
      );
    } else {
      LOG.error("Uncaught exception in thread \"{}\"", thread.getName(), throwable);
    }
  }
}

