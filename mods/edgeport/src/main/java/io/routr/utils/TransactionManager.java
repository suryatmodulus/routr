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

import javax.sip.ClientTransaction;
import javax.sip.ServerTransaction;
import javax.sip.Transaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ViaHeader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages active SIP transactions.
 */
public class TransactionManager {
  private final Map<String, Transaction> activeTransactions = new HashMap<>();

  /**
   * Stores a client and server transaction pair for a call.
   * 
   * @param callId The call ID
   * @param clientTransaction The client transaction
   * @param serverTransaction The server transaction
   */
  public void putTransactions(String callId, ClientTransaction clientTransaction, 
      ServerTransaction serverTransaction) {
    activeTransactions.put(callId + "_client", clientTransaction);
    activeTransactions.put(callId + "_server", serverTransaction);
  }

  /**
   * Gets the client transaction for a call.
   * 
   * @param callId The call ID
   * @return The client transaction, or null if not found
   */
  public ClientTransaction getClientTransaction(String callId) {
    return (ClientTransaction) activeTransactions.get(callId + "_client");
  }

  /**
   * Gets the server transaction for a call.
   * 
   * @param callId The call ID
   * @return The server transaction, or null if not found
   */
  public ServerTransaction getServerTransaction(String callId) {
    return (ServerTransaction) activeTransactions.get(callId + "_server");
  }

  /**
   * Removes transactions for a call.
   * 
   * @param callId The call ID
   */
  public void removeTransactions(String callId) {
    activeTransactions.remove(callId + "_client");
    activeTransactions.remove(callId + "_server");
  }

  /**
   * Updates the client transaction for a call (used during authentication).
   * 
   * @param callId The call ID
   * @param clientTransaction The new client transaction
   */
  public void updateClientTransaction(String callId, ClientTransaction clientTransaction) {
    activeTransactions.put(callId + "_client", clientTransaction);
  }

  /**
   * Removes all transactions matching a specific transport, host, and port.
   * Used when cleaning up after transport errors.
   * 
   * @param transport The transport protocol
   * @param host The host address
   * @param port The port number
   * @return List of removed transactions
   */
  public List<Transaction> removeTransactionsByTransport(String transport, String host, int port) {
    List<String> toRemove = new ArrayList<>();
    List<Transaction> removedTransactions = new ArrayList<>();
    
    for (Map.Entry<String, Transaction> entry : activeTransactions.entrySet()) {
      Transaction tx = entry.getValue();
      if (tx != null && tx.getRequest() != null) {
        ViaHeader via = (ViaHeader) tx.getRequest().getHeader(ViaHeader.NAME);
        if (via != null &&
            transport.equalsIgnoreCase(via.getTransport()) &&
            host.equals(via.getHost()) &&
            port == via.getPort()) {
          toRemove.add(entry.getKey());
          removedTransactions.add(tx);
        }
      }
    }

    for (String key : toRemove) {
      activeTransactions.remove(key);
    }

    return removedTransactions;
  }

  /**
   * Gets all active transaction keys.
   * 
   * @return Set of transaction keys
   */
  public java.util.Set<String> getActiveTransactionKeys() {
    return activeTransactions.keySet();
  }
}

