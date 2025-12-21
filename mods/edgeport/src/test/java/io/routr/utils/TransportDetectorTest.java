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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransportDetectorTest {

  @Mock
  private Request request;

  @Mock
  private ViaHeader viaHeader;

  @Test
  public void testIsWebSocketTransportWithWS() {
    when(request.getHeader(ViaHeader.NAME)).thenReturn(viaHeader);
    when(viaHeader.getTransport()).thenReturn("ws");

    assertTrue(TransportDetector.isWebSocketTransport(request));
  }

  @Test
  public void testIsWebSocketTransportWithWSS() {
    when(request.getHeader(ViaHeader.NAME)).thenReturn(viaHeader);
    when(viaHeader.getTransport()).thenReturn("wss");

    assertTrue(TransportDetector.isWebSocketTransport(request));
  }

  @Test
  public void testIsWebSocketTransportWithTCP() {
    when(request.getHeader(ViaHeader.NAME)).thenReturn(viaHeader);
    when(viaHeader.getTransport()).thenReturn("tcp");

    assertFalse(TransportDetector.isWebSocketTransport(request));
  }

  @Test
  public void testIsWebSocketTransportWithUDP() {
    when(request.getHeader(ViaHeader.NAME)).thenReturn(viaHeader);
    when(viaHeader.getTransport()).thenReturn("udp");

    assertFalse(TransportDetector.isWebSocketTransport(request));
  }

  @Test
  public void testIsWebSocketTransportWithNullViaHeader() {
    when(request.getHeader(ViaHeader.NAME)).thenReturn(null);

    assertFalse(TransportDetector.isWebSocketTransport(request));
  }

  @Test
  public void testIsWebSocketTransportWithNullTransport() {
    when(request.getHeader(ViaHeader.NAME)).thenReturn(viaHeader);
    when(viaHeader.getTransport()).thenReturn(null);

    assertFalse(TransportDetector.isWebSocketTransport(request));
  }

  @Test
  public void testIsWebSocketTransportCaseInsensitive() {
    when(request.getHeader(ViaHeader.NAME)).thenReturn(viaHeader);
    when(viaHeader.getTransport()).thenReturn("WS");

    assertTrue(TransportDetector.isWebSocketTransport(request));
  }
}

