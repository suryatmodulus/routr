/*
 * Copyright (C) 2024 by Fonoster Inc (https://fonoster.com)
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

import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sip.ClientTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.Header;
import javax.sip.message.Response;
import java.util.Base64;
import java.util.List;

/**
 * Handles SIP authentication challenges.
 */
public class AuthenticationHandler {
  private static final Logger LOG = LogManager.getLogger(AuthenticationHandler.class);
  private final SipStackExt sipStack;
  private final javax.sip.header.HeaderFactory headerFactory;

  public AuthenticationHandler(SipStackExt sipStack, javax.sip.header.HeaderFactory headerFactory) {
    this.sipStack = sipStack;
    this.headerFactory = headerFactory;
  }

  /**
   * Creates an AccountManager from headers if x-gateway-auth header is present.
   * 
   * @param headers The list of headers
   * @param host The host for authentication
   * @return AccountManager if auth header found, null otherwise
   */
  public static AccountManager createAccountManager(final List<Header> headers, final String host) {
    for (Header header : headers) {
      if (header.getName().equalsIgnoreCase("x-gateway-auth")) {
        var authStr = ((ExtensionHeader) header).getValue();
        var auth = new String(Base64.getDecoder().decode(authStr));

        if (auth.split(":").length != 2) {
          throw new IllegalArgumentException(
              "invalid 'x-gateway-auth' header value; should be base64('username:password')");
        }

        var username = auth.split(":")[0];
        var password = auth.split(":")[1];
        return new AccountManagerImpl(username, password, host);
      }
    }
    return null;
  }

  /**
   * Handles an authentication challenge by creating a new authenticated request.
   * 
   * @param event The response event containing the challenge
   * @param accountManager The account manager for credentials
   * @return The new client transaction with authenticated request, or null on failure
   */
  public ClientTransaction handleAuthChallenge(javax.sip.ResponseEvent event, AccountManager accountManager) {
    var authHelper = sipStack.getAuthenticationHelper(accountManager, this.headerFactory);
    // Setting looseRouting to false will cause the issue described in
    //  https://github.com/fonoster/routr/issues/18
    try {
      var newClientTransaction = authHelper.handleChallenge(
          event.getResponse(), 
          event.getClientTransaction(),
          (SipProvider) event.getSource(), 
          5, 
          true);
      
      // Set ApplicationData on the new transaction so ACK increment logic can detect authentication
      newClientTransaction.setApplicationData(accountManager);
      newClientTransaction.sendRequest();
      
      return newClientTransaction;
    } catch (NullPointerException | SipException e) {
      var request = event.getClientTransaction().getRequest();
      var callId = (javax.sip.header.CallIdHeader) request.getHeader(javax.sip.header.CallIdHeader.NAME);
      LOG.debug("an exception occurred while handling authentication challenge for callId: {}", 
          callId != null ? callId.getCallId() : "unknown", e);
      return null;
    }
  }

  /**
   * Checks if a response requires authentication.
   * 
   * @param response The response to check
   * @return true if authentication is required
   */
  public static boolean authenticationRequired(final Response response) {
    int statusCode = response.getStatusCode();
    return statusCode == Response.PROXY_AUTHENTICATION_REQUIRED || statusCode == Response.UNAUTHORIZED;
  }
}

