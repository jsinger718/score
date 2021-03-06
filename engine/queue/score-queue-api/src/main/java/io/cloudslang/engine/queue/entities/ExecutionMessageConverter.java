/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.engine.queue.entities;

import org.apache.commons.io.IOUtils;
import io.cloudslang.score.facade.entities.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 14:35
 */
public class ExecutionMessageConverter {

	@Autowired(required = false)
	private SensitiveDataHandler sensitiveDataHandler;

	public <T> T extractExecution(Payload payload) {
		return objFromBytes(payload.getData());
	}

	public Payload createPayload(Execution execution) {
		return createPayload(execution, false);
	}

	public Payload createPayload(Execution execution, boolean setContainsSensitiveData) {
		Payload payload = new Payload(objToBytes(execution));
		if(setContainsSensitiveData || checkContainsSensitiveData(execution)) {
			setSensitive(payload);
		}
		return payload;
	}

	private boolean checkContainsSensitiveData(Execution execution) {
		return sensitiveDataHandler != null &&
				sensitiveDataHandler.containsSensitiveData(execution.getSystemContext(), execution.getContexts());
	}

	public boolean containsSensitiveData(Payload payload) {
		return isSensitive(payload);
	}

	private <T> T objFromBytes(byte[] bytes) {
		ObjectInputStream ois = null;
		try {
			//2 Buffers are added to increase performance
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);

			skipPayloadMetaData(is);

			BufferedInputStream bis = new BufferedInputStream(is);
			ois = new ObjectInputStream(bis);

			//noinspection unchecked
			return (T)ois.readObject();
		}
		catch(IOException | ClassNotFoundException ex) {
			throw new RuntimeException("Failed to read execution plan from byte[]. Error: ", ex);
		}
		finally {
			IOUtils.closeQuietly(ois);
		}

	}

	private byte[] objToBytes(Object obj){
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();

			initPayloadMetaData(bout);

			BufferedOutputStream bos = new BufferedOutputStream(bout);
			oos = new ObjectOutputStream(bos);

			oos.writeObject(obj);
			oos.flush();

			return bout.toByteArray();
		}
		catch(IOException ex) {
			throw new RuntimeException("Failed to serialize execution plan. Error: ", ex);
		} finally {
			IOUtils.closeQuietly(oos);
		}
	}

	/***************************************************************************************/
	//we padding payload with clean bytes which then will be used for metadata writing
	private static final byte[] PAYLOAD_META_DATA_INIT_BYTES = {0};

	//for now meta data is only one byte
	private static final int INFRA_PART_BYTE = 0;

	private static final int IS_SENSITIVE = 1;

	private void setSensitive(Payload payload) {
		payload.getData()[INFRA_PART_BYTE] = IS_SENSITIVE;
	}

	private boolean isSensitive(Payload payload) {
		return payload.getData()[INFRA_PART_BYTE] == IS_SENSITIVE;
	}

	private void skipPayloadMetaData(ByteArrayInputStream is) throws IOException {
		for(int i = 0; i < PAYLOAD_META_DATA_INIT_BYTES.length; i++) {
			is.read();
		}
	}

	private void initPayloadMetaData(ByteArrayOutputStream baos) throws IOException {
		baos.write(PAYLOAD_META_DATA_INIT_BYTES);
	}
}
