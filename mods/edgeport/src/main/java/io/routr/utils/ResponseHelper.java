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

import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.WWWAuthenticate;
import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * Helper class with static utility methods for working with SIP responses.
 */
public class ResponseHelper {

  /**
   * Checks if a response event is transactional (has client transaction and specific method).
   * 
   * @param event The response event
   * @param response The response
   * @return true if transactional
   */
  public static boolean isTransactional(javax.sip.ResponseEvent event, Response response) {
    return (event.getClientTransaction() != null &&
        hasMethod(response, Request.INVITE, Request.MESSAGE, Request.REGISTER));
  }

  /**
   * Checks if a response is a stack job (Trying or Request Terminated).
   * 
   * @param response The response
   * @return true if it's a stack job
   */
  public static boolean isStackJob(final Response response) {
    return hasCodes(response, Response.TRYING, Response.REQUEST_TERMINATED)
        || hasMethod(response, Request.CANCEL);
  }

  /**
   * Checks if a response has one of the specified methods.
   * 
   * @param response The response
   * @param methods The methods to check
   * @return true if the response method matches one of the specified methods
   */
  public static boolean hasMethod(final Response response, final String... methods) {
    // Get method from CSeq header
    CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
    if (cseqHeader == null) {
      return false;
    }
    var method = cseqHeader.getMethod();
    return java.util.Arrays.stream(methods).anyMatch(m -> java.util.Objects.equals(m, method));
  }

  /**
   * Checks if a response has one of the specified status codes.
   * 
   * @param response The response
   * @param codes The codes to check
   * @return true if the response code matches one of the specified codes
   */
  public static boolean hasCodes(final Response response, final int... codes) {
    return java.util.Arrays.stream(codes).anyMatch(c -> c == response.getStatusCode());
  }

  /**
   * Removes headers that should be replaced from a response.
   * 
   * @param response The response to clean
   */
  public static void removeHeadersToReplace(Response response) {
    response.removeHeader(Via.NAME);
    response.removeHeader(Contact.NAME);
    response.removeHeader(Route.NAME);
    response.removeHeader(RecordRoute.NAME);
    response.removeHeader(javax.sip.header.WWWAuthenticateHeader.NAME);
  }
}

