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

package ru.iris.xiaomi4j.watchers;

import com.google.gson.JsonObject;
import ru.iris.xiaomi4j.enums.Devices;

public class Notification {
	private String command;
	private JsonObject rawMessage;
	private Devices type;
	private String sid;

	public JsonObject getRawMessage() {
		return rawMessage;
	}

	public void setRawMessage(JsonObject rawMessage) {
		this.rawMessage = rawMessage;
	}

	public Devices getType() {
		return type;
	}

	public void setType(Devices type) {
		this.type = type;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
