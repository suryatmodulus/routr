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

import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

/**
 * Utility class for detecting transport types in SIP messages.
 */
public class TransportDetector {

  /**
   * Checks if the request is using WebSocket or WSS transport.
   * This is determined by examining the Via header transport parameter.
   * 
   * @param request The SIP request to check
   * @return true if the transport is WS or WSS, false otherwise
   */
  public static boolean isWebSocketTransport(Request request) {
    ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
    if (viaHeader == null) {
      return false;
    }
    
    String transport = viaHeader.getTransport();
    if (transport == null) {
      return false;
    }
    
    String transportLower = transport.toLowerCase();
    return "ws".equals(transportLower) || "wss".equals(transportLower);
  }
}

