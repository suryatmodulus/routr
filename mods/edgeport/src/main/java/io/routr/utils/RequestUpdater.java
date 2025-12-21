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

import gov.nist.javax.sip.header.ContentLength;
import javax.sip.header.Header;
import javax.sip.message.Request;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for updating SIP requests with new headers.
 */
public class RequestUpdater {

  /**
   * Updates a request by removing all existing headers (except Content-Length)
   * and adding the provided headers.
   * 
   * @param request The original request to update
   * @param headers The new headers to add
   * @return A new request with updated headers
   */
  public static Request updateRequest(final Request request, final List<Header> headers) {
    var requestOut = (Request) request.clone();

    Iterator<String> names = requestOut.getHeaderNames();

    while (names.hasNext()) {
      String n = names.next();
      // WARN: Perhaps we should compute this value
      if (!n.equals(ContentLength.NAME)) {
        requestOut.removeHeader(n);
      }
    }

    for (Header header : headers) {
      // Ignore special header that is used for authentication to avoid leaking authentication information
      // and Content-Length as it will be calculated by the SIP stack.
      if (header.getName().equalsIgnoreCase(ContentLength.NAME) 
        || header.getName().equalsIgnoreCase("x-gateway-auth")) {
        continue;
      }

      requestOut.addHeader(header);
    }

    return requestOut;
  }
}

