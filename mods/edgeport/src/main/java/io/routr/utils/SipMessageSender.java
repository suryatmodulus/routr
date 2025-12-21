/*
 * Copyright (C) 2026 by Fonoster Inc (https://fonoster.com)
 * http://github.com/fonoster/routr
 *
 * This file is part of Routr
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

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility class for sending SIP messages with timeout handling.
 * This is particularly important for WebSocket/WSS connections to prevent
 * StackOverflowError caused by recursive connection establishment loops.
 */
public class SipMessageSender {
  private static final Logger LOG = LogManager.getLogger(SipMessageSender.class);
  private static final int TIMEOUT_SECONDS = 5;

  /**
   * Sends a SIP response with a timeout wrapper to prevent blocking and handle connection issues.
   * This method is particularly important for WebSocket/WSS connections to prevent StackOverflowError
   * caused by recursive connection establishment loops.
   * 
   * @param sendOperation The operation to execute (either sipProvider.sendResponse or transaction.sendResponse)
   * @param context Context string for logging purposes
   */
  public static void sendResponseWithTimeout(Runnable sendOperation, String context) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<?> future = executor.submit(() -> {
      try {
        sendOperation.run();
      } catch (Exception e) {
        LOG.error("Exception sending SIP response via " + context, e);
      }
    });

    try {
      future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      LOG.debug("Response sent via " + context + " successfully");
    } catch (TimeoutException e) {
      LOG.warn("sendResponse() timed out via " + context + " — client likely disconnected");
      future.cancel(true);
    } catch (Exception e) {
      LOG.error("Exception during sendResponse execution via " + context, e);
    } finally {
      executor.shutdown();
    }
  }

  /**
   * Sends a response using the SIP provider.
   * 
   * @param sipProvider The SIP provider
   * @param response The response to send
   * @param isWebSocket Whether this is a WebSocket connection
   */
  public static void sendResponse(SipProvider sipProvider, javax.sip.message.Response response, 
      boolean isWebSocket) throws SipException {
    if (isWebSocket) {
      sendResponseWithTimeout(() -> {
        try {
          sipProvider.sendResponse(response);
        } catch (SipException e) {
          throw new RuntimeException(e);
        }
      }, "sipProvider");
    } else {
      sipProvider.sendResponse(response);
    }
  }

  /**
   * Sends a response using a server transaction.
   * 
   * @param transaction The server transaction
   * @param response The response to send
   * @param isWebSocket Whether this is a WebSocket connection
   */
  public static void sendResponse(Transaction transaction, javax.sip.message.Response response, 
      boolean isWebSocket) throws SipException, InvalidArgumentException {
    if (isWebSocket) {
      sendResponseWithTimeout(() -> {
        try {
          ((javax.sip.ServerTransaction) transaction).sendResponse(response);
        } catch (SipException | InvalidArgumentException e) {
          throw new RuntimeException(e);
        }
      }, "serverTransaction");
    } else {
      ((javax.sip.ServerTransaction) transaction).sendResponse(response);
    }
  }
}

