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
 * internal threads (like EventScannerThread) that can occur when attempting to
 * send messages on closed WebSocket/WSS connections, particularly in edge cases
 * when receiving a REQUEST (like BYE) on a closed connection.
 */
public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
  private static final Logger LOG = LogManager.getLogger(ThreadExceptionHandler.class);

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    if (throwable instanceof StackOverflowError) {
      String threadName = thread.getName();
      LOG.error(
          "StackOverflowError in thread \"{}\" - edge case: attempting to send message on a closed WebSocket/WSS connection. " +
          "This can occur when receiving a REQUEST (like BYE) on a closed connection (perhaps due to network issues). " +
          "The thread will continue running but the operation that caused the error may have failed.",
          threadName,
          throwable
      );
    } else {
      LOG.error("Uncaught exception in thread \"{}\"", thread.getName(), throwable);
    }
  }
}

