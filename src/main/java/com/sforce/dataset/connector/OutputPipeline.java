/*
 * Copyright (c) 2014, salesforce.com, inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *    following disclaimer.
 *  
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and 
 *    the following disclaimer in the documentation and/or other materials provided with the distribution. 
 *    
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or 
 *    promote products derived from this software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sforce.dataset.connector;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.sforce.dataset.connector.metadata.FieldType;


public class OutputPipeline  {
	private int curRowIndex = 0;
	private List<FieldType> fieldList;
	private LinkedBlockingQueue<List<Object>> queue;
	
	public OutputPipeline(LinkedBlockingQueue<List<Object>> queue, List<FieldType> fldList)
			throws IOException {
		if(fldList==null || fldList.isEmpty())		
			throw new IllegalArgumentException("Input Argument {fldList} cannot be null");
			
		this.fieldList = fldList;
		this.queue = queue;
	}

	public void setData(List<Object> data) throws ClassCastException, ClassNotFoundException, InterruptedException
	{
		if (data != null) {
			
			if(data.size() != fieldList.size())
			{
				throw new IllegalArgumentException("Row column count does not match the field count");
			}
			
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i) != null) {
					String javaDatatypeFullClassName = ((FieldType) this.fieldList.get(i)).getType().getCanonicalName();
					String dataClassCanonicalName = data.get(i).getClass().getCanonicalName();
															
						if(!dataClassCanonicalName.equals(javaDatatypeFullClassName))
						{						
							if(!ConnectorUtils.classForJavaDataTypeFullClassName(javaDatatypeFullClassName).isInstance(data.get(i)))
							{
								throw new ClassCastException("The data[" + dataClassCanonicalName
								+ "] class and the field[" + javaDatatypeFullClassName
								+ "] class should match for field ["+(this.fieldList.get(i)).getName()+"]");
							}
						}						
				}
			} //end for
			queue.put(data);
			curRowIndex++;
		}else
		{
			throw new IllegalArgumentException("Data cannot be null");
		}
	}

	public int getCurRowIndex() {
		return curRowIndex;
	}

}