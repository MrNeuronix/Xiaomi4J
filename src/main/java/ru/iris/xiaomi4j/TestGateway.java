/*
 * Copyright 2017 Nikolay A. Viguro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.iris.xiaomi4j;

import java.net.UnknownHostException;
import java.util.List;

import ru.iris.xiaomi4j.model.GatewayModel;
import ru.iris.xiaomi4j.watchers.*;

public class TestGateway {

   public static void main(String[] ARGV) throws UnknownHostException, InterruptedException {

     Watcher watcher = new Watcher() {
       @Override
       public void onNotification(Notification notification) {
         System.out.println("Gateway got a message: " + notification.getRawMessage());
       }
     };

     Discovery discovery = new Discovery();
     discovery.startScan();

     Thread.sleep(5000L);

	   List<GatewayModel> gateways = discovery.getGatewayModels();
	   System.out.println("Gateways found: " + gateways.size());

	   discovery.stopScan();

	   Gateway gateway = new Gateway(
			   gateways.get(0).getHost(),
			   gateways.get(0).getPort(),
			   gateways.get(0).getSid(),
			   "uwhqlfglf4cuywne",
			   watcher);
   }

}
